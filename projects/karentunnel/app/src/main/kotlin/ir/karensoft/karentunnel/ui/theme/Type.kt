package ir.karensoft.karentunnel.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Persian-first typography.
 *
 * Notes that matter for Persian rendering:
 *  * `lineHeightStyle` trimming avoids clipped diacritics and descenders, which
 *    is the usual cause of "cut off" Persian text in Compose.
 *  * `includeFontPadding = false` with an explicit line height keeps digits and
 *    Persian letters on the same baseline in mixed lines.
 *  * The system font family is used on purpose: it is the one that carries a
 *    correct Persian shaping table on every OEM ROM. Bundling a Latin-only font
 *    would break Persian rendering on devices without proper fallback.
 */
private val PersianTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

val KarenTypography = Typography(
    displaySmall = PersianTextStyle.copy(fontSize = 36.sp, lineHeight = 52.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = PersianTextStyle.copy(fontSize = 28.sp, lineHeight = 42.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = PersianTextStyle.copy(fontSize = 22.sp, lineHeight = 34.sp, fontWeight = FontWeight.Medium),
    titleMedium = PersianTextStyle.copy(fontSize = 17.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    bodyLarge = PersianTextStyle.copy(fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium = PersianTextStyle.copy(fontSize = 14.sp, lineHeight = 24.sp),
    labelLarge = PersianTextStyle.copy(fontSize = 15.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    labelSmall = PersianTextStyle.copy(fontSize = 12.sp, lineHeight = 18.sp),
)
