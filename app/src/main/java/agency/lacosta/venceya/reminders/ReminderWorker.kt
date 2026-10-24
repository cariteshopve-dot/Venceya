package agency.lacosta.venceya.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import agency.lacosta.venceya.MainActivity
import agency.lacosta.venceya.R
import agency.lacosta.venceya.VenceYaApp
import agency.lacosta.venceya.domain.Reminder
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Revisa una vez al día, a eso de las 9 de la mañana, qué vence y avisa.
 * Todo pasa en el teléfono, sin servidor.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as VenceYaApp
        val today = LocalDate.now()
        app.container.items.all().forEach { item ->
            val reminder = item.reminderToday(today) ?: return@forEach
            val text = when (reminder) {
                Reminder.TODAY -> applicationContext.getString(R.string.notif_today, item.name)
                Reminder.BEFORE -> applicationContext.resources.getQuantityString(
                    R.plurals.notif_before, item.remindDaysBefore, item.name, item.remindDaysBefore,
                )
            }
            notify(item.id.toInt(), item.name, text)
        }
        return Result.success()
    }

    private fun notify(id: Int, title: String, text: String) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "reminders"
        private const val WORK_NAME = "daily-reminders"
        private val RUN_AT: LocalTime = LocalTime.of(9, 0)

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID, context.getString(R.string.notif_channel), NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        /** Programa la revisión diaria. Llamarlo varias veces no duplica nada. */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(RUN_AT)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
