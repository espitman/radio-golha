package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
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

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp) // Maintain the thick white frame
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val context = LocalContext.current
            val avatarSizePx = with(LocalDensity.current) { 96.dp.roundToPx() }
            
            if (normalizedUrl != null) {
                val imageRequest = remember(normalizedUrl, avatarSizePx, context) {
                    ImageRequest.Builder(context)
                        .data(normalizedUrl)
                        .size(coil.size.Size(avatarSizePx, avatarSizePx))
                        .crossfade(false)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .networkCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCacheKey(normalizedUrl)
                        .diskCacheKey(normalizedUrl)
                        .build()
                }
                val painter = rememberAsyncImagePainter(
                    model = imageRequest,
                    imageLoader = AndroidAppContext.imageLoader(),
                )

                when (painter.state) {
                    is AsyncImagePainter.State.Success -> {
                        Image(
                            painter = painter,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    else -> {
                        AvatarPlaceholder(
                            name = name,
                            tint = tint,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            } else {
                AvatarPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
