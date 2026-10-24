package agency.lacosta.venceya.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import agency.lacosta.venceya.R

@OptIn(ExperimentalTextApi::class)
private fun lexend(weight: Int) =
    Font(R.font.lexend, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

/** Lexend para todo: redondeada y muy legible, también en números chicos. */
val Lexend = FontFamily(lexend(400), lexend(500), lexend(600), lexend(700))

private val base = Typography()

private fun TextStyle.lexend(weight: FontWeight) = copy(fontFamily = Lexend, fontWeight = weight)

val VenceTypography = Typography(
    displayLarge = base.displayLarge.lexend(FontWeight.Bold),
    displayMedium = base.displayMedium.lexend(FontWeight.Bold),
    displaySmall = base.displaySmall.lexend(FontWeight.Bold),
    headlineLarge = base.headlineLarge.lexend(FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.lexend(FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.lexend(FontWeight.SemiBold),
    titleLarge = base.titleLarge.lexend(FontWeight.SemiBold),
    titleMedium = base.titleMedium.lexend(FontWeight.SemiBold),
    titleSmall = base.titleSmall.lexend(FontWeight.Medium),
    bodyLarge = base.bodyLarge.lexend(FontWeight.Normal),
    bodyMedium = base.bodyMedium.lexend(FontWeight.Normal),
    bodySmall = base.bodySmall.lexend(FontWeight.Normal),
    labelLarge = base.labelLarge.lexend(FontWeight.Medium),
    labelMedium = base.labelMedium.lexend(FontWeight.Medium),
    labelSmall = base.labelSmall.lexend(FontWeight.Medium),
)
