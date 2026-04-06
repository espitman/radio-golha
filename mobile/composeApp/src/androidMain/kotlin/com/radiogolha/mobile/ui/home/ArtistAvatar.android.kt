package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Image
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.radiogolha.mobile.AndroidAppContext
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
    val imageRequest = remember(normalizedUrl, avatarSizePx, context) {
        ImageRequest.Builder(context)
            .data(normalizedUrl)
            .size(Size(avatarSizePx, avatarSizePx))
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(normalizedUrl)
            .diskCacheKey(normalizedUrl)
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        imageLoader = AndroidAppContext.imageLoader(),
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = name,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            is AsyncImagePainter.State.Error -> {
                AvatarPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.matchParentSize(),
                )
            }

            else -> {
                AvatarPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}
