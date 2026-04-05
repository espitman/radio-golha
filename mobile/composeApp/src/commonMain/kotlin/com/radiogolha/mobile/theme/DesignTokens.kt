package com.radiogolha.mobile.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GolhaColors {
    val ScreenBackground = Color(0xFFF7F7FA)
    val Surface = Color(0xFFFFFFFF)
    val PrimaryAccent = Color(0xFF0A84FF)
    val PrimaryText = Color(0xFF1C1C1E)
    val SecondaryText = Color(0xFF8E8E93)
    val Border = Color(0xFFE5E5EA)
    val BadgeBackground = Color(0xFFF2F2F7)
    val BannerBackground = Color(0xFFFBF4E6)
    val BannerDetail = Color(0xFFD6C29A)
    val BannerShadow = Color(0x1A876B2F)
    val SoftBlue = Color(0xFFEAF4FF)
    val SoftRose = Color(0xFFF7EEF3)
    val SoftSand = Color(0xFFF6F1E8)
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
    val Small = 12.dp
    val Card = 18.dp
    val Banner = 24.dp
    val Pill = 999.dp
}

object GolhaElevation {
    val Card = 2.dp
    val Banner = 6.dp
}

object GolhaTypographyTokens {
    val AppTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
    val SectionTitle = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )
    val BannerTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )
    val Body = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val SecondaryBody = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    val Tiny = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
}
