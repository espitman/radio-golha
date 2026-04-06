package com.radiogolha.mobile.debug

import android.content.pm.ApplicationInfo
import com.radiogolha.mobile.AndroidAppContext
import java.io.File

private const val DATABASE_FILE_NAME = "golha_database.db"

actual fun isDebugDatabaseToolsEnabled(): Boolean {
    val context = AndroidAppContext.require()
    return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}

actual fun debugDatabaseExternalPath(): String? {
    if (!isDebugDatabaseToolsEnabled()) return null
    val context = AndroidAppContext.require()
    val externalDir = context.getExternalFilesDir(null) ?: return null
    return File(externalDir, DATABASE_FILE_NAME).absolutePath
}

actual fun importDebugDatabase(): DebugDatabaseImportResult {
    if (!isDebugDatabaseToolsEnabled()) {
        return DebugDatabaseImportResult(
            success = false,
            message = "این قابلیت فقط در نسخه دیباگ فعال است.",
        )
    }

    val context = AndroidAppContext.require()
    val externalPath = debugDatabaseExternalPath()
        ?: return DebugDatabaseImportResult(
            success = false,
            message = "مسیر فایل دیباگ در دسترس نیست.",
        )

    val sourceFile = File(externalPath)
    if (!sourceFile.exists()) {
        return DebugDatabaseImportResult(
            success = false,
            message = "فایل جدید دیتابیس پیدا نشد.",
        )
    }
    if (sourceFile.length() <= 0L) {
        return DebugDatabaseImportResult(
            success = false,
            message = "فایل دیتابیس خالی است.",
        )
    }

    val targetFile = File(context.filesDir, DATABASE_FILE_NAME)
    val tempFile = File(context.cacheDir, "$DATABASE_FILE_NAME.import")

    return runCatching {
        sourceFile.inputStream().use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (targetFile.exists() && !targetFile.delete()) {
            error("replace target failed")
        }
        if (!tempFile.renameTo(targetFile)) {
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
        }

        DebugDatabaseImportResult(
            success = true,
            message = "دیتابیس جدید با موفقیت وارد شد.",
        )
    }.getOrElse {
        tempFile.delete()
        DebugDatabaseImportResult(
            success = false,
            message = "جایگزینی دیتابیس ناموفق بود.",
        )
    }
}
