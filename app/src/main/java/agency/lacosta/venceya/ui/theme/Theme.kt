package agency.lacosta.venceya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import agency.lacosta.venceya.data.settings.ThemeMode
import agency.lacosta.venceya.domain.Urgency

/** Un color de semáforo: el trazo del anillo, el fondo suave y el texto sobre ese fondo. */
@Immutable
data class Signal(val stroke: Color, val soft: Color, val text: Color)

@Immutable
data class SemaforoColors(
    val red: Signal,
    val amber: Signal,
    val green: Signal,
    val row: Color,
    val track: Color,
) {
    fun of(urgency: Urgency): Signal = when (urgency) {
        Urgency.OVERDUE, Urgency.THIS_WEEK -> red
        Urgency.THIS_MONTH -> amber
        Urgency.LATER -> green
    }
}

private val LightSemaforo = SemaforoColors(
    red = Signal(Red, RedSoft, RedText),
    amber = Signal(Amber, AmberSoft, AmberText),
    green = Signal(Green, GreenSoft, GreenText),
    row = RowBg,
    track = Track,
)

private val DarkSemaforo = SemaforoColors(
    red = Signal(RedNight, RedNightSoft, RedNight),
    amber = Signal(AmberNight, AmberNightSoft, AmberNight),
    green = Signal(GreenNight, GreenNightSoft, GreenNight),
    row = NightRow,
    track = NightTrack,
)

private val LightScheme = lightColorScheme(
    primary = Ink,
    onPrimary = White,
    primaryContainer = RowBg,
    onPrimaryContainer = Ink,
    secondary = Green,
    onSecondary = White,
    secondaryContainer = GreenSoft,
    onSecondaryContainer = GreenText,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = RowBg,
    onSurfaceVariant = Stone,
    surfaceContainerLowest = White,
    surfaceContainerLow = RowBg,
    surfaceContainer = RowBg,
    surfaceContainerHigh = White,
    surfaceContainerHighest = Track,
    outline = Track,
    outlineVariant = Line,
    error = Red,
    onError = White,
)

private val DarkScheme = darkColorScheme(
    primary = Snow,
    onPrimary = Night,
    primaryContainer = NightRow,
    onPrimaryContainer = Snow,
    secondary = GreenNight,
    onSecondary = Night,
    secondaryContainer = GreenNightSoft,
    onSecondaryContainer = GreenNight,
    background = Night,
    onBackground = Snow,
    surface = Night,
    onSurface = Snow,
    surfaceVariant = NightRow,
    onSurfaceVariant = Fog,
    surfaceContainerLowest = Night,
    surfaceContainerLow = NightRow,
    surfaceContainer = NightRow,
    surfaceContainerHigh = NightRow,
    surfaceContainerHighest = NightTrack,
    outline = NightTrack,
    outlineVariant = NightLine,
    error = RedNight,
    onError = Night,
)

val LocalSemaforo = staticCompositionLocalOf { LightSemaforo }

object Semaforo {
    val colors: SemaforoColors
        @Composable @ReadOnlyComposable get() = LocalSemaforo.current
}

@Composable
fun isDark(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun VenceYaTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = isDark(themeMode)
    CompositionLocalProvider(LocalSemaforo provides if (dark) DarkSemaforo else LightSemaforo) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = VenceTypography,
            content = content,
        )
    }
}
