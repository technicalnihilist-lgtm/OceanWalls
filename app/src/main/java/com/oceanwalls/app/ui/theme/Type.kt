package com.oceanwalls.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oceanwalls.app.R

val Manrope = FontFamily(
    Font(R.font.manrope, FontWeight.Light),
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold),
    Font(R.font.manrope, FontWeight.ExtraBold)
)

val OceanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1.2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Bold,
        fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope, fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp, lineHeight = 13.sp, letterSpacing = 0.8.sp
    )
)
