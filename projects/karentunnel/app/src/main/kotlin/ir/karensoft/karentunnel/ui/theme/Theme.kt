package ir.karensoft.karentunnel.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Restrained, professional palette.
 *
 * Colour carries meaning, so the palette is deliberately narrow:
 *  * neutral/blue for idle,
 *  * amber for "in progress / degraded" (never green),
 *  * green only for *verified* connected,
 *  * red only for terminal error.
 *
 * Dynamic colour is available from Android 12 but is OFF by default: a VPN
 * status must not be tinted by the device wallpaper, because "green" has to keep
 * meaning exactly one thing.
 */
private val Amber = Color(0xFFB26A00)
private val AmberLight = Color(0xFFFFDDA6)
private val TrustBlue = Color(0xFF1F6FEB)
private val TrustBlueDark = Color(0xFF9CC4FF)
private val VerifiedGreen = Color(0xFF1B7F4B)
private val VerifiedGreenDark = Color(0xFF7FE0A8)
private val DangerRed = Color(0xFFB3261E)
private val DangerRedDark = Color(0xFFFFB4AB)

private val LightColors = lightColorScheme(
    primary = TrustBlue,
    onPrimary = Color.White,
    secondary = Amber,
    onSecondary = Color.White,
    tertiary = VerifiedGreen,
    error = DangerRed,
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEFF3),
)

private val DarkColors = darkColorScheme(
    primary = TrustBlueDark,
    onPrimary = Color(0xFF06121F),
    secondary = AmberLight,
    onSecondary = Color(0xFF1F1300),
    tertiary = VerifiedGreenDark,
    error = DangerRedDark,
    background = Color(0xFF0D1117),
    surface = Color(0xFF151B23),
    surfaceVariant = Color(0xFF1E2530),
)

/** Semantic colours the connection UI needs beyond the Material roles. */
object TunnelColors {
    val connectedLight = VerifiedGreen
    val connectedDark = VerifiedGreenDark
    val degradedLight = Amber
    val degradedDark = AmberLight
}

@Composable
fun KarenTunnelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    /** Kept for accessibility testing; off by default by design. */
    allowDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        allowDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = KarenTypography, content = content)
}
