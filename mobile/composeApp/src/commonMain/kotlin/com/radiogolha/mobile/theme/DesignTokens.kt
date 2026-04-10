package com.radiogolha.mobile.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GolhaColors {
    val ScreenBackground = Color(0xFFF7F1E1)
    val Surface = Color(0xFFFFFBF2)
    val PrimaryAccent = Color(0xFFE3BF55)
    val OnAccent = Color(0xFF0B2161)
    val PrimaryText = Color(0xFF0B2161)
    val SecondaryText = Color(0xFF5D6B94)
    val Border = Color(0xFFE4D4A7)
    val BadgeBackground = Color(0xFFF3E6BF)
    val BannerBackground = Color(0xFF0B2161)
    val BannerDetail = Color(0xFFE3BF55)
    val BannerShadow = Color(0x330B2161)
    val SoftBlue = Color(0xFFDCE5F6)
    val SoftRose = Color(0xFFF3DE9A)
    val SoftSand = Color(0xFFF8EFD3)
}

object GolhaSpacing {
    val ScreenHorizontal = 16.dp
    val SectionGap = 24.dp
    val CardGap = 12.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XL = 20.dp
    val XXL = 28.dp
}

object GolhaRadius {
    val Small = 8.dp
    val Card = 18.dp
    val Banner = 0.dp
    val Pill = 999.dp
}

object GolhaElevation {
    val Card = 2.dp
    val Banner = 6.dp
}

object GolhaTypographyTokens {
    val AppTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    )
    val SectionTitle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val BannerTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
    val Body = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    val SecondaryBody = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    )
    val Tiny = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    )
}
