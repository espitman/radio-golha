package com.radiogolha.mobile.ui.home

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.radiogolha.mobile.theme.GolhaColors

@Composable
actual fun ArtistAvatar(
    name: String,
    imageUrl: String?,
    tint: Color,
    modifier: Modifier,
) {
    val normalizedUrl = imageUrl
        ?.trim()
        ?.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
    if (normalizedUrl == null) {
        AvatarPlaceholder(
            name = name,
            tint = tint,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val avatarSizePx = with(LocalDensity.current) { 96.dp.roundToPx() }
    val imageLoader = rememberGolhaImageLoader(context)
    val imageRequest = remember(normalizedUrl, avatarSizePx, context) {
        ImageRequest.Builder(context)
            .data(normalizedUrl)
            .size(Size(avatarSizePx, avatarSizePx))
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(normalizedUrl)
            .diskCacheKey(normalizedUrl)
            .build()
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            imageLoader = imageLoader,
            contentDescription = name,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            loading = {
                AvatarPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.matchParentSize(),
                )
            },
            error = {
                AvatarPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.matchParentSize(),
                )
            },
        )
    }
}

@Composable
private fun rememberGolhaImageLoader(context: Context): ImageLoader = remember(context) {
    ImageLoader.Builder(context)
        .crossfade(true)
        .respectCacheHeaders(false)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("golha_image_cache"))
                .maxSizePercent(0.02)
                .build()
        }
        .build()
}
