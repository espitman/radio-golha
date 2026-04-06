package com.radiogolha.mobile.debug

actual fun isDebugDatabaseToolsEnabled(): Boolean = false

actual fun debugDatabaseExternalPath(): String? = null

actual fun importDebugDatabase(): DebugDatabaseImportResult = DebugDatabaseImportResult(
    success = false,
    message = "این قابلیت فعلاً روی این پلتفرم فعال نیست.",
)
