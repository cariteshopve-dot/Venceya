package agency.lacosta.venceya.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import agency.lacosta.venceya.R
import agency.lacosta.venceya.VenceYaApp
import agency.lacosta.venceya.domain.Reminder
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * Revisa una vez al día, a eso de las 9 de la mañana, qué vence y avisa.
 * Todo pasa en el teléfono, sin servidor.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as VenceYaApp
        val today = LocalDate.now()
        val sound = app.container.settings.settings.first().sound
        app.container.items.all().forEach { item ->
            val reminder = item.reminderToday(today) ?: return@forEach
            val text = when (reminder) {
                Reminder.TODAY -> applicationContext.getString(R.string.notif_today, item.name)
                Reminder.BEFORE -> applicationContext.resources.getQuantityString(
                    R.plurals.notif_before, item.remindDaysBefore, item.name, item.remindDaysBefore,
                )
            }
            Notifier.show(applicationContext, sound, item.id.toInt(), item.name, text)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily-reminders"
        private val RUN_AT: LocalTime = LocalTime.of(9, 0)

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
