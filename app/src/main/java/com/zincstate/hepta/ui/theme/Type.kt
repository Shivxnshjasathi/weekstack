package com.zincstate.hepta.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zincstate.hepta.R

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = Poppins),
    displayMedium = baseline.displayMedium.copy(fontFamily = Poppins),
    displaySmall = baseline.displaySmall.copy(fontFamily = Poppins),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = Poppins, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = Poppins, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = Poppins),
    titleLarge = baseline.titleLarge.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium, letterSpacing = 0.sp),
    titleMedium = baseline.titleMedium.copy(fontFamily = Poppins),
    titleSmall = baseline.titleSmall.copy(fontFamily = Poppins),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = Poppins, letterSpacing = 0.5.sp),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = baseline.bodySmall.copy(fontFamily = Poppins),
    labelLarge = baseline.labelLarge.copy(fontFamily = Poppins),
    labelMedium = baseline.labelMedium.copy(fontFamily = Poppins),
    labelSmall = baseline.labelSmall.copy(fontFamily = Poppins, fontWeight = FontWeight.Medium)
)