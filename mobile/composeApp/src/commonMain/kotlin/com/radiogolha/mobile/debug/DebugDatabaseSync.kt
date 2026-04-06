package com.radiogolha.mobile.debug

data class DebugDatabaseImportResult(
    val success: Boolean,
    val message: String,
)

expect fun isDebugDatabaseToolsEnabled(): Boolean

expect fun debugDatabaseExternalPath(): String?

expect fun importDebugDatabase(): DebugDatabaseImportResult
