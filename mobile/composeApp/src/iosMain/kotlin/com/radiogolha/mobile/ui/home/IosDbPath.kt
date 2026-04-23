@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.radiogolha.mobile.ui.home

import platform.Foundation.*
private var cachedDbPath: String? = null

actual fun requireArchiveDbPath(): String {
    return cachedDbPath ?: throw IllegalStateException("Database path not initialized")
}

actual fun requireUserDbPath(): String {
    val fileManager = NSFileManager.defaultManager
    val appSupportDir = fileManager.URLForDirectory(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        null,
        true,
        null
    )?.path ?: throw IllegalStateException("Could not find Application Support directory")
    
    return "$appSupportDir/user_data.db"
}

fun initDatabase(bundlePath: String): String {
    val fileManager = NSFileManager.defaultManager
    val appSupportDir = fileManager.URLForDirectory(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
        null,
        true,
        null
    )?.path ?: throw IllegalStateException("Could not find Application Support directory")

    if (!fileManager.fileExistsAtPath(appSupportDir)) {
        fileManager.createDirectoryAtPath(appSupportDir, true, null, null)
    }

    val dbPath = "$appSupportDir/golha_database.db"

    // Keep existing database (including CDN-updated file) and only bootstrap once.
    if (!fileManager.fileExistsAtPath(dbPath)) {
        fileManager.copyItemAtPath(bundlePath, dbPath, null)
    }
    cachedDbPath = dbPath
    return dbPath
}
