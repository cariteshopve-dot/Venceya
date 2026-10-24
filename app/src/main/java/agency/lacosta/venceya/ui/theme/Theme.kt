package agency.lacosta.venceya.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import agency.lacosta.venceya.data.settings.ThemeMode
import agency.lacosta.venceya.domain.Urgency

// Tema provisional y neutro: se reemplaza por el estilo que elija la persona (Boleto, Semáforo o Cuenta atrás).

private val Light = lightColorScheme(
    primary = Color(0xFF1C2321),
    onPrimary = Color.White,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF4F6F5),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFE6EAE8),
    onPrimary = Color(0xFF111413),
    background = Color(0xFF111413),
    surface = Color(0xFF111413),
    surfaceContainer = Color(0xFF1B1F1E),
)

/** Color de cada nivel de urgencia. */
fun Urgency.color(dark: Boolean): Color = when (this) {
    Urgency.OVERDUE, Urgency.THIS_WEEK -> if (dark) Color(0xFFFF7B72) else Color(0xFFD2352B)
    Urgency.THIS_MONTH -> if (dark) Color(0xFFFFC14D) else Color(0xFFB97800)
    Urgency.LATER -> if (dark) Color(0xFF5FD49A) else Color(0xFF17865A)
}

@Composable
fun isDark(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun VenceYaTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isDark(themeMode)) Dark else Light, content = content)
}
