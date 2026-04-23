package com.radiogolha.mobile.data

data class DatabaseCdnUpdateResult(
    val success: Boolean,
    val didUpdate: Boolean,
    val message: String,
)

expect suspend fun updateArchiveDatabaseFromCdn(
    forceDownload: Boolean = false,
    onProgress: (Float) -> Unit = {},
): DatabaseCdnUpdateResult
