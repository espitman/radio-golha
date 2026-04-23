package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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
    var imageBitmap by remember(normalizedUrl) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    if (normalizedUrl != null && imageBitmap == null) {
        LaunchedEffect(normalizedUrl) {
            imageBitmap = loadCachedRemoteImageBitmap(normalizedUrl)
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = GolhaColors.SoftSand,
        shadowElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GolhaColors.SoftSand),
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = imageBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
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
