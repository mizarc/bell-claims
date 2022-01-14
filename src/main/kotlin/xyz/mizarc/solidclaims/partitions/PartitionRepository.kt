package xyz.mizarc.solidclaims.partitions

import co.aikar.idb.Database
import org.bukkit.World
import xyz.mizarc.solidclaims.Repository
import xyz.mizarc.solidclaims.storage.Storage
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PartitionRepository(private val storage: Storage<Database>): Repository<Partition> {
    var partitions: ArrayList<Partition> = ArrayList()
    var chunkPartitions: MutableMap<Position, ArrayList<Partition>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claimPartitions")
            for (result in results) {
                val area = Area(
                    Position(result.getInt("lowerPositionX"), result.getInt("lowerPositionZ")),
                    Position(result.getInt("upperPositionX"), result.getInt("upperPositionZ"))
                )
                val partition = Partition(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("claimId")), area)
                partitions.add(partition)

                for (chunk in area.getChunks()) {
                    if (chunkPartitions[chunk] == null) {
                        chunkPartitions[chunk] = ArrayList()
                    }
                    chunkPartitions[chunk]!!.add(partition)
                }
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun getAll(): ArrayList<Partition> {
        return partitions
    }

    override fun getById(id: UUID): Partition? {
        for (partition in partitions) {
            if (partition.id == id) {
                return partition
            }
        }
        return null
    }

    fun getByClaim(claimId: UUID): ArrayList<Partition> {
        val foundPartitions = ArrayList<Partition>()
        for (partition in partitions) {
            if (partition.claimId == claimId) {
                foundPartitions.add(partition)
            }
        }
        return foundPartitions
    }

    fun getByChunk(position: Position, world: World): ArrayList<Partition> {
        if (chunkPartitions[position.toChunk()] == null) {
            return ArrayList()
        }
        return chunkPartitions[position.toChunk()]!!
    }

    fun getByPosition(position: Position, world: World): Partition? {
        val partitionsInChunk = getByChunk(position, world)
        for (partition in partitionsInChunk) {
            if (partition.isPositionInPartition(position)) {
                return partition
            }
        }
        return null
    }

    override fun add(entity: Partition) {
        addToMemory(entity)
        try {
            storage.connection.executeUpdate("INSERT INTO claimPartitions (claimId, lowerPositionX, " +
                    "lowerPositionZ, upperPositionX, upperPositionZ, main) VALUES (?,?,?,?,?,?);",
                entity.claimId, entity.area.lowerPosition.x, entity.area.lowerPosition.z, entity.area.upperPosition.x,
                entity.area.upperPosition.z)
            return
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun update(entity: Partition) {
        removeFromMemory(entity)
        addToMemory(entity)
        try {
            storage.connection.executeUpdate("UPDATE claimPartitions SET lowerPositionX=?, lowerPositionZ=?, " +
                    "upperPositionX=?, upperPositionZ=? WHERE id=?;", entity.area.lowerPosition.x,
                entity.area.lowerPosition.z, entity.area.upperPosition.x, entity.area.upperPosition.z, entity.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(entity: Partition) {
        removeFromMemory(entity)
        try {
            storage.connection.executeUpdate("DELETE FROM claimPartitions WHERE id=?;", entity.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return
    }

    private fun addToMemory(entity: Partition) {
        partitions.add(entity)
        val claimChunks = entity.getChunks()
        for (chunk in claimChunks) {
            if (chunkPartitions[chunk] == null) {
                chunkPartitions[chunk] = ArrayList()
            }
            chunkPartitions[chunk]?.add(entity)
        }
    }

    private fun removeFromMemory(entity: Partition) {
        partitions.remove(entity)
        for (chunk in entity.area.getChunks()) {
            val savedChunk = chunkPartitions[chunk] ?: return
            savedChunk.remove(entity)
        }
    }

    /**
     * Creates a new table to store claim partition data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claimPartitions " +
                    "(id TEXT, claimId TEXT, lowerPositionX INTEGER NOT NULL, lowerPositionZ INTEGER NOT NULL, " +
                    "upperPositionX INTEGER NOT NULL, upperPositionZ INTEGER NOT NULL);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}