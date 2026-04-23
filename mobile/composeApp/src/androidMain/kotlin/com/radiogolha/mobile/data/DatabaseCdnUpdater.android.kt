package com.radiogolha.mobile.data

import android.net.Uri
import com.radiogolha.mobile.AndroidAppContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val DATABASE_FILE_NAME = "golha_database.db"
private const val DATABASE_VERSION_FILE_NAME = "golha_db_version"
private const val DEFAULT_MANIFEST_URL = "https://storage.iran.liara.space/espitman/golha/db/database_manifest.json"
private const val DEFAULT_LATEST_TIMESTAMP_URL = "https://storage.iran.liara.space/espitman/golha/db/latest.txt"
private const val CONNECT_TIMEOUT_MS = 15_000
private const val READ_TIMEOUT_MS = 60_000
private const val CURRENT_DB_VERSION = 2
private const val PREFS_NAME = "database_update_state"
private const val PREF_LAST_APPLIED_RELEASE_AT = "last_applied_release_at"

private data class DatabaseManifest(
    val fileName: String,
    val sha256: String,
    val sizeBytes: Int,
    val releasedAt: String?,
)

actual suspend fun updateArchiveDatabaseFromCdn(
    forceDownload: Boolean,
    onProgress: (Float) -> Unit,
): DatabaseCdnUpdateResult = withContext(Dispatchers.IO) {
    runCatching {
        val context = AndroidAppContext.require()
        val remoteReleasedAt = loadLatestReleasedAt(appendCacheBuster(resolveLatestTimestampUrl()))
        val manifestUrl = appendCacheBuster(resolveManifestUrl())
        val manifest = loadManifest(manifestUrl)

        val destinationFile = File(context.filesDir, DATABASE_FILE_NAME)
        val localReleasedAt = context
            .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString(PREF_LAST_APPLIED_RELEASE_AT, null)

        if (!forceDownload && destinationFile.exists() && remoteReleasedAt.isNotBlank()) {
            if (localReleasedAt == remoteReleasedAt) {
                return@withContext DatabaseCdnUpdateResult(
                    success = true,
                    didUpdate = false,
                    message = "دیتابیس همین حالا به‌روز است.",
                )
            }
        }

        val databaseUrl = appendCacheBuster(resolveDatabaseUrl(manifestUrl, manifest.fileName))
        val payload = downloadBytes(databaseUrl, manifest.sizeBytes, onProgress)

        if (payload.size != manifest.sizeBytes) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "اندازه فایل دیتابیس معتبر نیست.",
            )
        }

        val hash = hashBytesSha256(payload)
        if (!hash.equals(manifest.sha256, ignoreCase = true)) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "هش دیتابیس معتبر نیست.",
            )
        }

        val sqliteHeader = "SQLite format 3".encodeToByteArray()
        if (payload.size < sqliteHeader.size || !payload.copyOfRange(0, sqliteHeader.size).contentEquals(sqliteHeader)) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "فایل دریافت‌شده دیتابیس SQLite معتبر نیست.",
            )
        }

        val tempFile = File(context.cacheDir, "$DATABASE_FILE_NAME.download")
        tempFile.writeBytes(payload)

        if (destinationFile.exists() && !destinationFile.delete()) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "جایگزینی دیتابیس ناموفق بود.",
            )
        }
        if (!tempFile.renameTo(destinationFile)) {
            tempFile.copyTo(destinationFile, overwrite = true)
            tempFile.delete()
        }

        ensureArchiveDbIndexes(destinationFile.absolutePath)
        File(context.filesDir, DATABASE_VERSION_FILE_NAME).writeText(CURRENT_DB_VERSION.toString())
        val appliedReleasedAt = remoteReleasedAt.ifBlank { manifest.releasedAt.orEmpty() }
        if (appliedReleasedAt.isNotBlank()) {
            context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_LAST_APPLIED_RELEASE_AT, appliedReleasedAt)
                .apply()
        }

        DatabaseCdnUpdateResult(
            success = true,
            didUpdate = true,
            message = "دیتابیس جدید با موفقیت دریافت شد.",
        )
    }.getOrElse {
        DatabaseCdnUpdateResult(
            success = false,
            didUpdate = false,
            message = "دریافت دیتابیس از CDN ناموفق بود.",
        )
    }
}

private fun resolveLatestTimestampUrl(): String {
    val env = System.getenv("RADIOGOLHA_DB_LATEST_URL")?.trim().orEmpty()
    return env.ifBlank { DEFAULT_LATEST_TIMESTAMP_URL }
}

private fun resolveManifestUrl(): String {
    val env = System.getenv("RADIOGOLHA_DB_MANIFEST_URL")?.trim().orEmpty()
    return env.ifBlank { DEFAULT_MANIFEST_URL }
}

private fun resolveDatabaseUrl(manifestUrl: String, fileName: String): String {
    val override = System.getenv("RADIOGOLHA_DB_FILE_URL")?.trim().orEmpty()
    if (override.isNotBlank()) return override
    val base = manifestUrl.substringBeforeLast('/')
    return "$base/$fileName"
}

private fun appendCacheBuster(rawUrl: String): String {
    val uri = Uri.parse(rawUrl)
    val builder = uri.buildUpon().clearQuery()
    uri.queryParameterNames.filterNot { it == "_ts" }.forEach { key ->
        uri.getQueryParameters(key).forEach { value -> builder.appendQueryParameter(key, value) }
    }
    builder.appendQueryParameter("_ts", System.currentTimeMillis().toString())
    return builder.build().toString()
}

private fun loadManifest(url: String): DatabaseManifest {
    val payload = readUrlBytes(url)
    val json = JSONObject(payload.decodeToString())
    return DatabaseManifest(
        fileName = json.getString("fileName"),
        sha256 = json.getString("sha256"),
        sizeBytes = json.getInt("sizeBytes"),
        releasedAt = json.optString("releasedAt").ifBlank { null },
    )
}

private fun loadLatestReleasedAt(url: String): String {
    return readUrlBytes(url).decodeToString().trim()
}

private fun downloadBytes(url: String, expectedSize: Int, onProgress: (Float) -> Unit): ByteArray {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        setRequestProperty("Cache-Control", "no-cache")
        setRequestProperty("Pragma", "no-cache")
        useCaches = false
    }

    connection.connect()
    try {
        if (connection.responseCode !in 200..299) {
            error("http ${connection.responseCode}")
        }

        val contentLength = connection.contentLength.takeIf { it > 0 } ?: expectedSize
        val total = maxOf(contentLength, expectedSize, 1)
        var received = 0
        var nextEmitAt = 0
        onProgress(0f)

        val output = java.io.ByteArrayOutputStream(maxOf(expectedSize, 32 * 1024))
        connection.inputStream.use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
                received += read
                if (received >= nextEmitAt) {
                    onProgress((received.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                    nextEmitAt = received + 64 * 1024
                }
            }
        }
        onProgress(1f)
        return output.toByteArray()
    } finally {
        connection.disconnect()
    }
}

private fun readUrlBytes(url: String): ByteArray {
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = CONNECT_TIMEOUT_MS
        readTimeout = READ_TIMEOUT_MS
        setRequestProperty("Cache-Control", "no-cache")
        setRequestProperty("Pragma", "no-cache")
        useCaches = false
    }

    connection.connect()
    try {
        if (connection.responseCode !in 200..299) {
            error("http ${connection.responseCode}")
        }
        return connection.inputStream.use { it.readBytes() }
    } finally {
        connection.disconnect()
    }
}

private fun hashBytesSha256(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString(separator = "") { "%02x".format(it) }
}

private fun ensureArchiveDbIndexes(dbPath: String) {
    runCatching {
        android.database.sqlite.SQLiteDatabase.openDatabase(
            dbPath,
            null,
            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
        ).use { db ->
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_singers_program_id ON program_singers(program_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_singers_singer_id ON program_singers(singer_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_singer_artist_id ON singer(artist_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_performers_program_id ON program_performers(program_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_performers_performer_id ON program_performers(performer_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_performer_artist_id ON performer(artist_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_modes_program_id ON program_modes(program_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_program_timeline_program_id ON program_timeline(program_id)")
        }
    }
}
