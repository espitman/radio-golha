package com.radiogolha.mobile

import android.content.Context

object AndroidAppContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context {
        return checkNotNull(appContext) { "Android application context is not initialized" }
    }
}
