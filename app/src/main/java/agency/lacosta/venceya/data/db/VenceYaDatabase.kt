package agency.lacosta.venceya.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemEntity::class], version = 1, exportSchema = true)
abstract class VenceYaDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        fun create(context: Context): VenceYaDatabase =
            Room.databaseBuilder(context, VenceYaDatabase::class.java, "venceya.db").build()
    }
}
