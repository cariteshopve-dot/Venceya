package agency.lacosta.venceya.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import agency.lacosta.venceya.MainActivity
import agency.lacosta.venceya.R
import agency.lacosta.venceya.data.settings.AlertSound

/**
 * Muestra los avisos. Android fija el sonido de un canal al crearlo, así que cada tono
 * tiene su propio canal y al cambiar de tono se borra el anterior.
 */
object Notifier {
    private const val CHANNEL_PREFIX = "reminders_"
    const val TEST_ID = -1

    fun channelId(sound: AlertSound) = CHANNEL_PREFIX + sound.key.hashCode().toUInt().toString(16)

    /** Deja creado solo el canal del tono actual y devuelve su id. */
    fun ensureChannel(context: Context, sound: AlertSound): String {
        val manager = context.getSystemService(NotificationManager::class.java)
        val id = channelId(sound)
        manager.notificationChannels
            .filter { (it.id.startsWith(CHANNEL_PREFIX) || it.id == "reminders") && it.id != id }
            .forEach { manager.deleteNotificationChannel(it.id) }
        if (manager.getNotificationChannel(id) == null) {
            val channel = NotificationChannel(id, context.getString(R.string.notif_channel), NotificationManager.IMPORTANCE_DEFAULT)
            when (sound) {
                AlertSound.Default -> Unit
                AlertSound.Silent -> channel.setSound(null, null)
                is AlertSound.Tone -> channel.setSound(Uri.parse(sound.uri), audioAttributes)
            }
            manager.createNotificationChannel(channel)
        }
        return id
    }

    fun canNotify(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun show(context: Context, sound: AlertSound, id: Int, title: String, text: String) {
        if (!canNotify(context)) return
        val channel = ensureChannel(context, sound)
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
}
