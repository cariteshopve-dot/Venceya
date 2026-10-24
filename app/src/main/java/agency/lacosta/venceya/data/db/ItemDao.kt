package agency.lacosta.venceya.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items")
    suspend fun getAll(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Long): ItemEntity?

    @Query("SELECT COUNT(*) FROM items")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(item: ItemEntity): Long

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun delete(id: Long)
}
