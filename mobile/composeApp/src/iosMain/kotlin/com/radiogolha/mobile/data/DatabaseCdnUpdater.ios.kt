@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.radiogolha.mobile.data

import com.radiogolha.mobile.ui.home.requireArchiveDbPath
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.posix.memcpy

private const val DEFAULT_MANIFEST_URL = "https://storage.iran.liara.space/espitman/golha/db/database_manifest.json"
private const val DEFAULT_LATEST_TIMESTAMP_URL = "https://storage.iran.liara.space/espitman/golha/db/latest.txt"
private const val LAST_APPLIED_RELEASE_KEY = "radiogolha.db.lastAppliedReleaseAt"

@Serializable
private data class DatabaseManifest(
    val fileName: String,
    val sha256: String,
    val sizeBytes: Int,
    val releasedAt: String? = null,
)

actual suspend fun updateArchiveDatabaseFromCdn(
    forceDownload: Boolean,
    onProgress: (Float) -> Unit,
): DatabaseCdnUpdateResult = withContext(Dispatchers.Default) {
    runCatching {
        val remoteReleasedAt = readTextFromUrl(appendCacheBuster(resolveLatestTimestampUrl())).trim()
        val manifestJson = readTextFromUrl(appendCacheBuster(resolveManifestUrl()))
        val manifest = Json { ignoreUnknownKeys = true }.decodeFromString<DatabaseManifest>(manifestJson)

        val dbPath = requireArchiveDbPath()
        val userDefaults = NSUserDefaults.standardUserDefaults
        val localReleasedAt = userDefaults.stringForKey(LAST_APPLIED_RELEASE_KEY)

        if (!forceDownload && remoteReleasedAt.isNotBlank() && localReleasedAt == remoteReleasedAt) {
            return@withContext DatabaseCdnUpdateResult(
                success = true,
                didUpdate = false,
                message = "دیتابیس همین حالا به‌روز است.",
            )
        }

        val databaseUrl = appendCacheBuster(resolveDatabaseUrl(resolveManifestUrl(), manifest.fileName))
        onProgress(0f)
        val downloadedData = readDataFromUrl(databaseUrl)
        onProgress(0.85f)

        if (downloadedData.length.toInt() != manifest.sizeBytes) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "اندازه فایل دیتابیس معتبر نیست.",
            )
        }

        val sqliteHeader = "SQLite format 3".encodeToByteArray()
        val headerBytes = ByteArray(sqliteHeader.size)
        headerBytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), downloadedData.bytes, sqliteHeader.size.toULong())
        }
        if (!headerBytes.contentEquals(sqliteHeader)) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "فایل دریافت‌شده دیتابیس SQLite معتبر نیست.",
            )
        }

        val tempPath = "$dbPath.download"
        val writeOk = NSFileManager.defaultManager.createFileAtPath(
            path = tempPath,
            contents = downloadedData,
            attributes = null
        )
        if (!writeOk) {
            return@withContext DatabaseCdnUpdateResult(
                success = false,
                didUpdate = false,
                message = "ذخیره دیتابیس دانلودشده ناموفق بود.",
            )
        }
        onProgress(0.95f)

        val fileManager = NSFileManager.defaultManager
        if (fileManager.fileExistsAtPath(dbPath)) {
            fileManager.removeItemAtPath(dbPath, null)
        }
        fileManager.moveItemAtPath(tempPath, dbPath, null)

        val appliedReleasedAt = remoteReleasedAt.ifBlank { manifest.releasedAt.orEmpty() }
        if (appliedReleasedAt.isNotBlank()) {
            userDefaults.setObject(appliedReleasedAt, forKey = LAST_APPLIED_RELEASE_KEY)
            userDefaults.synchronize()
        }
        onProgress(1f)

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

private fun resolveManifestUrl(): String {
    return platform.Foundation.NSProcessInfo.processInfo.environment["RADIOGOLHA_DB_MANIFEST_URL"] as? String
        ?: DEFAULT_MANIFEST_URL
}

private fun resolveLatestTimestampUrl(): String {
    return platform.Foundation.NSProcessInfo.processInfo.environment["RADIOGOLHA_DB_LATEST_URL"] as? String
        ?: DEFAULT_LATEST_TIMESTAMP_URL
}

private fun resolveDatabaseUrl(manifestUrl: String, fileName: String): String {
    val override = platform.Foundation.NSProcessInfo.processInfo.environment["RADIOGOLHA_DB_FILE_URL"] as? String
    if (!override.isNullOrBlank()) return override
    val base = manifestUrl.substringBeforeLast('/')
    return "$base/$fileName"
}

private fun appendCacheBuster(url: String): String {
    val separator = if (url.contains('?')) "&" else "?"
    return "$url${separator}_ts=${NSProcessInfo.processInfo.systemUptime}"
}

private fun readTextFromUrl(url: String): String {
    val data = readDataFromUrl(url)
    return data.toByteArray().decodeToString()
}

private fun readDataFromUrl(url: String): NSData {
    val nsUrl = NSURL(string = url) ?: error("invalid url")
    return NSData.dataWithContentsOfURL(nsUrl) ?: error("download failed")
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}
