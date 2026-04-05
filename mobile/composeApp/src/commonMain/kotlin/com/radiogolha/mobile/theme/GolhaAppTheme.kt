package com.radiogolha.mobile.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import radiogolha_mobile.composeapp.generated.resources.Res
import radiogolha_mobile.composeapp.generated.resources.vazirmatn_bold
import radiogolha_mobile.composeapp.generated.resources.vazirmatn_medium
import radiogolha_mobile.composeapp.generated.resources.vazirmatn_regular

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

@Composable
private fun golhaFontFamily(): FontFamily = FontFamily(
    Font(
        resource = Res.font.vazirmatn_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        resource = Res.font.vazirmatn_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        resource = Res.font.vazirmatn_bold,
        weight = FontWeight.Bold,
    ),
)

@Composable
private fun golhaTypography(): Typography {
    val vazirmatn = golhaFontFamily()

    return Typography(
        displaySmall = GolhaTypographyTokens.BannerTitle.copy(fontFamily = vazirmatn),
        headlineLarge = GolhaTypographyTokens.AppTitle.copy(fontFamily = vazirmatn),
        headlineMedium = GolhaTypographyTokens.SectionTitle.copy(fontFamily = vazirmatn),
        titleLarge = GolhaTypographyTokens.SectionTitle.copy(fontFamily = vazirmatn),
        bodyLarge = GolhaTypographyTokens.Body.copy(fontFamily = vazirmatn),
        bodyMedium = GolhaTypographyTokens.Body.copy(fontFamily = vazirmatn),
        bodySmall = GolhaTypographyTokens.SecondaryBody.copy(fontFamily = vazirmatn),
        labelLarge = TextStyle.Default.copy(
            fontFamily = vazirmatn,
            fontWeight = GolhaTypographyTokens.SectionTitle.fontWeight,
            fontSize = 14.sp,
        ),
        labelMedium = GolhaTypographyTokens.SecondaryBody.copy(fontFamily = vazirmatn),
        labelSmall = GolhaTypographyTokens.Tiny.copy(fontFamily = vazirmatn),
    )
}

@Composable
fun GolhaAppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GolhaLightColors,
        typography = golhaTypography(),
        content = content,
    )
}
