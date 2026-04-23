package com.radiogolha.mobile.ui.home

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import platform.Foundation.*
import platform.posix.memcpy

private const val IMAGE_CACHE_FOLDER = "golha_image_cache"

private object IosImageMemoryCache {
    val cache = NSCache()
}

@OptIn(ExperimentalForeignApi::class)
internal suspend fun loadCachedRemoteImageBitmap(url: String): ImageBitmap? = withContext(Dispatchers.Default) {
    val key = url as NSString

    (IosImageMemoryCache.cache.objectForKey(key) as? NSData)?.let { data ->
        return@withContext data.toComposeBitmapOrNull()
    }

    val diskUrl = imageCacheFileUrl(url) ?: return@withContext downloadAndCacheFromNetwork(url, key)
    val fileManager = NSFileManager.defaultManager
    val diskPath = diskUrl.path

    if (diskPath != null && fileManager.fileExistsAtPath(diskPath)) {
        val diskData = NSData.dataWithContentsOfURL(diskUrl)
        if (diskData != null) {
            IosImageMemoryCache.cache.setObject(diskData, key)
            return@withContext diskData.toComposeBitmapOrNull()
        }
    }

    return@withContext downloadAndCacheFromNetwork(url, key)
}

@OptIn(ExperimentalForeignApi::class)
private fun downloadAndCacheFromNetwork(url: String, key: NSString): ImageBitmap? {
    val nsUrl = NSURL(string = url) ?: return null
    val data = NSData.dataWithContentsOfURL(nsUrl) ?: return null

    IosImageMemoryCache.cache.setObject(data, key)

    val diskUrl = imageCacheFileUrl(url)
    if (diskUrl != null) {
        runCatching {
            data.writeToURL(diskUrl, atomically = true)
        }
    }

    return data.toComposeBitmapOrNull()
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toComposeBitmapOrNull(): ImageBitmap? {
    return runCatching {
        val bytes = ByteArray(length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), this@toComposeBitmapOrNull.bytes, this@toComposeBitmapOrNull.length)
            }
        }
        SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    }.getOrNull()
}

@OptIn(ExperimentalForeignApi::class)
private fun imageCacheFileUrl(url: String): NSURL? {
    val fileManager = NSFileManager.defaultManager
    val cachesPath = fileManager.URLForDirectory(
        NSCachesDirectory,
        NSUserDomainMask,
        null,
        true,
        null
    )?.path ?: return null

    val cacheRootPath = "$cachesPath/$IMAGE_CACHE_FOLDER"
    if (!fileManager.fileExistsAtPath(cacheRootPath)) {
        fileManager.createDirectoryAtPath(cacheRootPath, true, null, null)
    }

    val safeName = "${url.hashCode()}_${url.length}.img"
    return NSURL.fileURLWithPath("$cacheRootPath/$safeName")
}
