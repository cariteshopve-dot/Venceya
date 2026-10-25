package agency.lacosta.venceya.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ItemEntity::class], version = 2, exportSchema = true)
abstract class VenceYaDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        fun create(context: Context): VenceYaDatabase =
            Room.databaseBuilder(context, VenceYaDatabase::class.java, "venceya.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

/**
 * v1 guardaba un solo aviso (remindDaysBefore) y siempre avisaba también el mismo día.
 * v2 guarda la lista de días; se conserva lo que ya avisaba cada vencimiento.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE items_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                kind TEXT NOT NULL,
                firstDueEpochDay INTEGER NOT NULL,
                repeat TEXT NOT NULL,
                remindDays TEXT NOT NULL,
                amount TEXT NOT NULL,
                note TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO items_new (id, name, kind, firstDueEpochDay, repeat, remindDays, amount, note, createdAt)
            SELECT id, name, kind, firstDueEpochDay, repeat,
                CASE WHEN remindDaysBefore > 0 THEN remindDaysBefore || ',0' ELSE '0' END,
                amount, note, createdAt
            FROM items
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE items")
        db.execSQL("ALTER TABLE items_new RENAME TO items")
    }
}
