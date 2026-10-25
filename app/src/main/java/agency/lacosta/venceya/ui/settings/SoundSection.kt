package agency.lacosta.venceya.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import agency.lacosta.venceya.R
import agency.lacosta.venceya.data.settings.AlertSound
import agency.lacosta.venceya.reminders.Notifier

/** Elegir el tono de los avisos, escucharlo y mandar un aviso de prueba. */
@Composable
fun SoundSection(sound: AlertSound, onChange: (AlertSound) -> Unit) {
    val context = LocalContext.current
    var playing by remember { mutableStateOf<Ringtone?>(null) }
    DisposableEffect(Unit) { onDispose { playing?.stop() } }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val picked = result.data?.pickedUri()
        onChange(
            when {
                picked == null -> AlertSound.Silent
                RingtoneManager.isDefault(picked) -> AlertSound.Default
                else -> AlertSound.Tone(picked.toString())
            },
        )
    }
    var pendingTest by remember { mutableStateOf(false) }
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingTest) sendTest(context, sound)
        pendingTest = false
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clickable { picker.launch(pickerIntent(context, sound)) }
            .padding(vertical = 8.dp),
    ) {
        Text(stringResource(R.string.sound_tone), style = MaterialTheme.typography.bodyLarge)
        Text(
            soundTitle(context, sound),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = {
                playing?.stop()
                playing = soundUri(sound)?.let { RingtoneManager.getRingtone(context, it) }?.also { it.play() }
            },
            enabled = sound != AlertSound.Silent,
        ) {
            Icon(Icons.Outlined.PlayArrow, null)
            Text(stringResource(R.string.sound_listen), Modifier.padding(start = 6.dp))
        }
        OutlinedButton(onClick = {
            playing?.stop()
            if (Notifier.canNotify(context)) {
                sendTest(context, sound)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pendingTest = true
                askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }) {
            Icon(Icons.Outlined.Notifications, null)
            Text(stringResource(R.string.sound_test), Modifier.padding(start = 6.dp))
        }
    }
    Text(
        stringResource(R.string.sound_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

private fun soundUri(sound: AlertSound): Uri? = when (sound) {
    AlertSound.Default -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    AlertSound.Silent -> null
    is AlertSound.Tone -> Uri.parse(sound.uri)
}

private fun soundTitle(context: Context, sound: AlertSound): String = when (sound) {
    AlertSound.Default -> context.getString(R.string.sound_default)
    AlertSound.Silent -> context.getString(R.string.sound_silent)
    is AlertSound.Tone -> runCatching { RingtoneManager.getRingtone(context, Uri.parse(sound.uri))?.getTitle(context) }
        .getOrNull() ?: context.getString(R.string.sound_default)
}

private fun pickerIntent(context: Context, sound: AlertSound) = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.sound_tone))
    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
    putExtra(
        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
        when (sound) {
            AlertSound.Default -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            AlertSound.Silent -> null
            is AlertSound.Tone -> Uri.parse(sound.uri)
        },
    )
}

@Suppress("DEPRECATION")
private fun Intent.pickedUri(): Uri? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
    } else {
        getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
    }

private fun sendTest(context: Context, sound: AlertSound) {
    Notifier.show(
        context,
        sound,
        Notifier.TEST_ID,
        context.getString(R.string.sound_test_title),
        context.getString(R.string.sound_test_body),
    )
}
