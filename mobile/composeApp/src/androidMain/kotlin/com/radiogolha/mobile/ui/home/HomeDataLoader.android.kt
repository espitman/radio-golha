package com.radiogolha.mobile.ui.home

import com.radiogolha.mobile.AndroidAppContext
import java.io.File

private const val CURRENT_DB_VERSION = 2

actual fun requireArchiveDbPath(): String {
    val context = AndroidAppContext.require()
    val dbFile = File(context.filesDir, "golha_database.db")
    val versionFile = File(context.filesDir, "golha_db_version")

    val installedVersion = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0
    
    var shouldRefresh = !dbFile.exists() || installedVersion < CURRENT_DB_VERSION
    
    if (!shouldRefresh && dbFile.exists()) {
        runCatching {
            context.assets.openFd("golha_database.db").use { fd ->
                if (fd.length != dbFile.length()) {
                    shouldRefresh = true
                }
            }
        }.onFailure { }
    }

    if (shouldRefresh) {
        context.assets.open("golha_database.db").use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        versionFile.writeText(CURRENT_DB_VERSION.toString())
    }

    return dbFile.absolutePath
}
