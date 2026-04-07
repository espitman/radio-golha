package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.request.ImageRequest
import com.radiogolha.mobile.AndroidAppContext
import com.radiogolha.mobile.theme.GolhaColors

@Composable
actual fun FeaturedArtwork(
    name: String,
    imageUrl: String?,
    tint: Color,
    modifier: Modifier,
) {
    val normalizedUrl = imageUrl
        ?.trim()
        ?.takeUnless { it.isEmpty() || it.equals("null", ignoreCase = true) }
    val context = LocalContext.current
    val artworkSizePx = with(LocalDensity.current) { 116.dp.roundToPx() }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = GolhaColors.SoftSand,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(GolhaColors.SoftSand),
            contentAlignment = Alignment.Center,
        ) {
            if (normalizedUrl != null) {
                val imageRequest = remember(normalizedUrl, artworkSizePx, context) {
                    ImageRequest.Builder(context)
                        .data(normalizedUrl)
                        .size(artworkSizePx, artworkSizePx)
                        .crossfade(false)
                        .memoryCacheKey("featured:$normalizedUrl")
                        .diskCacheKey("featured:$normalizedUrl")
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
                        FeaturedPlaceholder(
                            name = name,
                            tint = tint,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            } else {
                FeaturedPlaceholder(
                    name = name,
                    tint = tint,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun FeaturedPlaceholder(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(GolhaColors.SoftSand),
        contentAlignment = Alignment.Center,
    ) {
        AvatarPlaceholder(
            name = name,
            tint = tint,
            modifier = Modifier.fillMaxSize(0.48f),
        )
    }
}
