package com.radiogolha.mobile

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

// Global cache for DB path to avoid repeated NSBundle calls if they are unstable
private var cachedDbPath: String? = null

fun MainViewController(bundlePath: String): UIViewController {
    return ComposeUIViewController {
        var errorText by remember { mutableStateOf<String?>(null) }
        
        LaunchedEffect(Unit) {
            try {
                if (cachedDbPath == null) {
                    com.radiogolha.mobile.ui.home.initDatabase(bundlePath)
                }
            } catch (e: Exception) {
                errorText = "DB ERROR: ${e.message}"
            }
        }
        
        MaterialTheme {
            if (errorText != null) {
                Text(errorText!!)
            } else {
                App()
            }
        }
    }
}
