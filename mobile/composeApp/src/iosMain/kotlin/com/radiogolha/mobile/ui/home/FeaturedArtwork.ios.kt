package com.radiogolha.mobile.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun FeaturedArtwork(
    name: String,
    imageUrl: String?,
    tint: Color,
    modifier: Modifier,
) {
    AvatarPlaceholder(
        name = name,
        tint = tint,
        modifier = modifier,
    )
}
