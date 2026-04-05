package com.radiogolha.mobile.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val GolhaLightColors = lightColorScheme(
    primary = GolhaColors.PrimaryAccent,
    onPrimary = GolhaColors.Surface,
    background = GolhaColors.ScreenBackground,
    onBackground = GolhaColors.PrimaryText,
    surface = GolhaColors.Surface,
    onSurface = GolhaColors.PrimaryText,
    surfaceVariant = GolhaColors.BadgeBackground,
    onSurfaceVariant = GolhaColors.SecondaryText,
    outline = GolhaColors.Border,
)

private val GolhaTypography = Typography(
    displaySmall = GolhaTypographyTokens.BannerTitle,
    headlineLarge = GolhaTypographyTokens.AppTitle,
    headlineMedium = GolhaTypographyTokens.SectionTitle,
    titleLarge = GolhaTypographyTokens.SectionTitle,
    bodyLarge = GolhaTypographyTokens.Body,
    bodyMedium = GolhaTypographyTokens.Body,
    bodySmall = GolhaTypographyTokens.SecondaryBody,
    labelLarge = TextStyle.Default.copy(
        fontFamily = GolhaTypographyTokens.Vazirmatn,
        fontWeight = GolhaTypographyTokens.SectionTitle.fontWeight,
        fontSize = 14.sp,
    ),
    labelMedium = GolhaTypographyTokens.SecondaryBody,
    labelSmall = GolhaTypographyTokens.Tiny,
)

@Composable
fun GolhaAppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GolhaLightColors,
        typography = GolhaTypography,
        content = content,
    )
}
