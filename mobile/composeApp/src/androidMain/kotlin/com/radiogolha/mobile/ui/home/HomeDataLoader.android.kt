package com.radiogolha.mobile.ui.home

import android.database.sqlite.SQLiteDatabase
import com.radiogolha.mobile.AndroidAppContext
import java.io.File

private const val CURRENT_DB_VERSION = 2
private const val DB_OPTIMIZATION_VERSION = 1

actual fun requireArchiveDbPath(): String {
    val context = AndroidAppContext.require()
    val dbFile = File(context.filesDir, "golha_database.db")
    val versionFile = File(context.filesDir, "golha_db_version")
    val optimizationFile = File(context.filesDir, "golha_db_opt_version")

    val installedVersion = if (versionFile.exists()) versionFile.readText().trim().toIntOrNull() ?: 0 else 0
    val installedOptVersion = if (optimizationFile.exists()) optimizationFile.readText().trim().toIntOrNull() ?: 0 else 0
    
    val shouldRefresh = !dbFile.exists() || installedVersion < CURRENT_DB_VERSION

    if (shouldRefresh) {
        context.assets.open("golha_database.db").use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        versionFile.writeText(CURRENT_DB_VERSION.toString())
    }

    if (shouldRefresh || installedOptVersion < DB_OPTIMIZATION_VERSION) {
        ensureArchiveDbIndexes(dbFile.absolutePath)
        optimizationFile.writeText(DB_OPTIMIZATION_VERSION.toString())
    }

    return dbFile.absolutePath
}

private fun ensureArchiveDbIndexes(dbPath: String) {
    runCatching {
        SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
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

actual fun requireUserDbPath(): String {
    val context = AndroidAppContext.require()
    return File(context.filesDir, "user_data.db").absolutePath
}
