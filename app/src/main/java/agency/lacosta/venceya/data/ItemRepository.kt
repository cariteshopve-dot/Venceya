package agency.lacosta.venceya.data

import agency.lacosta.venceya.data.db.ItemDao
import agency.lacosta.venceya.data.db.toDomain
import agency.lacosta.venceya.data.db.toEntity
import agency.lacosta.venceya.domain.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepository(private val dao: ItemDao) {

    val items: Flow<List<Item>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun all(): List<Item> = dao.getAll().map { it.toDomain() }

    suspend fun get(id: Long): Item? = dao.getById(id)?.toDomain()

    suspend fun count(): Int = dao.count()

    suspend fun save(item: Item): Long {
        // Al editar, conservar la fecha de creación original.
        val createdAt = if (item.id != 0L) dao.getById(item.id)?.createdAt else null
        return dao.upsert(item.toEntity(createdAt ?: System.currentTimeMillis()))
    }

    suspend fun delete(id: Long) = dao.delete(id)
}
