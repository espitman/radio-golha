package com.radiogolha.mobile.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
expect fun ArtistAvatar(
    name: String,
    imageUrl: String?,
    tint: Color,
    modifier: Modifier = Modifier,
)
