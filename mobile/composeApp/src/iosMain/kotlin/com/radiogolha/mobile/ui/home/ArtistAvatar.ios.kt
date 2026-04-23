package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
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

    var imageBitmap by remember(normalizedUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(normalizedUrl) { mutableStateOf(normalizedUrl != null) }

    if (normalizedUrl != null && imageBitmap == null) {
        LaunchedEffect(normalizedUrl) {
            imageBitmap = loadCachedRemoteImageBitmap(normalizedUrl)
            isLoading = false
        }
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = GolhaColors.Surface,
        shadowElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.95f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .clip(CircleShape),
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
