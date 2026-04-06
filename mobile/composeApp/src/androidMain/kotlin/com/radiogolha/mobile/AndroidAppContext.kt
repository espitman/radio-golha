package com.radiogolha.mobile

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

object AndroidAppContext {
    private var appContext: Context? = null
    private var imageLoader: ImageLoader? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun require(): Context {
        return checkNotNull(appContext) { "Android application context is not initialized" }
    }

    fun imageLoader(): ImageLoader {
        imageLoader?.let { return it }

        val context = require()
        return ImageLoader.Builder(context)
            .crossfade(false)
            .respectCacheHeaders(false)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("golha_image_cache"))
                    .maxSizePercent(0.03)
                    .build()
            }
            .build()
            .also { imageLoader = it }
    }
}
