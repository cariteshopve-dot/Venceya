package agency.lacosta.venceya

import android.app.Application
import agency.lacosta.venceya.data.ItemRepository
import agency.lacosta.venceya.data.db.VenceYaDatabase
import agency.lacosta.venceya.data.settings.SettingsRepository
import agency.lacosta.venceya.reminders.ReminderWorker

class VenceYaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderWorker.schedule(this)
    }
}

/** Dependencias de la app, creadas una sola vez. */
class AppContainer(app: Application) {
    private val database = VenceYaDatabase.create(app)
    val items = ItemRepository(database.itemDao())
    val settings = SettingsRepository(app)
}
