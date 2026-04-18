@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.radiogolha.mobile.ui.home

import platform.Foundation.*

actual fun requireArchiveDbPath(): String {
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
    println("iOS DB PATH: $dbPath")
    
    // For debugging, always refresh from bundle
    if (fileManager.fileExistsAtPath(dbPath)) {
        fileManager.removeItemAtPath(dbPath, null)
    }
    
    val success = fileManager.copyItemAtPath(bundlePath, dbPath, null)
    if (!success) {
        println("ERROR: Failed to copy database from $bundlePath to $dbPath")
    } else {
        println("SUCCESS: Database refreshed at $dbPath")
    }
    
    return dbPath
}
