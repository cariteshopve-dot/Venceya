package agency.lacosta.venceya.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Pago único hecho. Por ahora se activa a mano; luego lo pondrá Google Play Billing. */
    val isPro: Boolean = false,
    val sound: AlertSound = AlertSound.Default,
)

/** El tono de los avisos: el del teléfono, uno elegido de la lista del sistema, o ninguno. */
sealed interface AlertSound {
    data object Default : AlertSound
    data object Silent : AlertSound
    data class Tone(val uri: String) : AlertSound

    /** Texto estable para guardar y para nombrar el canal de avisos. */
    val key: String
        get() = when (this) {
            Default -> "default"
            Silent -> "silent"
            is Tone -> uri
        }

    companion object {
        fun fromKey(key: String?): AlertSound = when (key) {
            null, "", "default" -> Default
            "silent" -> Silent
            else -> Tone(key)
        }
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val store = context.dataStore

    val settings: Flow<Settings> = store.data.map { prefs ->
        Settings(
            themeMode = prefs[THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            isPro = prefs[PRO] ?: false,
            sound = AlertSound.fromKey(prefs[SOUND]),
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME] = mode.name }
    }

    suspend fun setSound(sound: AlertSound) {
        store.edit { it[SOUND] = sound.key }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val PRO = booleanPreferencesKey("is_pro")
        val SOUND = stringPreferencesKey("alert_sound")
    }
}
