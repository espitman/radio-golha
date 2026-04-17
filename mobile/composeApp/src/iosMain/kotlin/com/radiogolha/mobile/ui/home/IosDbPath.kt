package com.radiogolha.mobile.ui.home

import platform.Foundation.*

internal actual fun requireArchiveDbPath(): String {
    val fileManager = NSFileManager.defaultManager
    val bundlePath = NSBundle.mainBundle.pathForResource("golha_database", "db")
        ?: throw IllegalStateException("Database file not found in bundle")

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
    
    // Copy if not exists or if we need a refresh (same logic as Android)
    if (!fileManager.fileExistsAtPath(dbPath)) {
        fileManager.copyItemAtPath(bundlePath, dbPath, null)
    }
    
    return dbPath
}
