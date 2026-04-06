package com.radiogolha.mobile.debug

import android.widget.Toast
import com.radiogolha.mobile.AndroidAppContext

actual fun showDebugToast(message: String) {
    Toast.makeText(AndroidAppContext.require(), message, Toast.LENGTH_SHORT).show()
}
