package xyz.mizarc.solidclaims.partitions

import co.aikar.idb.Database
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.storage.Storage
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PartitionRepository(private val storage: Storage<Database>) {
    var partitions: MutableMap<UUID, Partition> = mutableMapOf()
    var chunkPartitions: MutableMap<Position2D, ArrayList<UUID>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun getAll(): Set<Partition> {
        return partitions.values.toSet()
    }

    fun getById(id: UUID): Partition? {
        return partitions[id]
    }

    fun getByClaim(claim: Claim): ArrayList<Partition> {
        val foundPartitions = ArrayList<Partition>()
        for (partition in partitions.values) {
            if (partition.claimId == claim.id) {
                foundPartitions.add(partition)
            }
        }
        return foundPartitions
    }

    fun getByChunk(position2D: Position2D): Set<Partition> {
        val foundPartitions = mutableSetOf<Partition>()
        val localChunkPartitions = chunkPartitions[position2D] ?: return setOf()

        for (id in localChunkPartitions) {
            foundPartitions.add(partitions[id] ?: continue)
        }

        return foundPartitions
    }

    fun getByPosition(position2D: Position2D): ArrayList<Partition> {
        val partitionsInChunk = getByChunk(position2D.toChunk())
        val partitionsInPosition = ArrayList<Partition>()
        for (partition in partitionsInChunk) {
            if (partition.isPositionInPartition(position2D)) {
                partitionsInPosition.add(partition)
            }
        }
        return partitionsInPosition
    }

    fun add(entity: Partition) {
        addToMemory(entity)
        try {
            storage.connection.executeUpdate("INSERT INTO claimPartitions (id, claimId, lowerPositionX, " +
                    "lowerPositionZ, upperPositionX, upperPositionZ) VALUES (?,?,?,?,?,?);",
                entity.id, entity.claimId, entity.area.lowerPosition2D.x, entity.area.lowerPosition2D.z,
                entity.area.upperPosition2D.x, entity.area.upperPosition2D.z)
            return
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun update(entity: Partition) {
        removeFromMemory(entity)
        addToMemory(entity)
        try {
            storage.connection.executeUpdate("UPDATE claimPartitions SET claimId=?, lowerPositionX=?, " +
                    "lowerPositionZ=?, upperPositionX=?, upperPositionZ=? WHERE id=?;", entity.claimId,
                entity.area.lowerPosition2D.x,  entity.area.lowerPosition2D.z, entity.area.upperPosition2D.x,
                entity.area.upperPosition2D.z, entity.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun remove(entity: Partition) {
        removeFromMemory(entity)
        try {
            storage.connection.executeUpdate("DELETE FROM claimPartitions WHERE id=?;", entity.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return
    }

    fun removeByClaim(claim: Claim) {
        val partitions = getByClaim(claim)
        for (partition in partitions) {
            removeFromMemory(partition)
        }
        try {
            storage.connection.executeUpdate("DELETE FROM claimPartitions WHERE claimId=?;", claim.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return

    }

    private fun addToMemory(entity: Partition) {
        partitions[entity.id] = entity
        val claimChunks = entity.getChunks()
        for (chunk in claimChunks) {
            if (chunkPartitions[chunk] == null) {
                chunkPartitions[chunk] = ArrayList()
            }
            chunkPartitions[chunk]?.add(entity.id)
        }
    }

    private fun removeFromMemory(entity: Partition) {
        partitions.remove(entity.id)
        for (chunk in entity.area.getChunks()) {
            val savedChunk = chunkPartitions[chunk] ?: return
            savedChunk.remove(entity.id)
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

    fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claimPartitions")
            for (result in results) {
                val area = Area(
                    Position2D(result.getInt("lowerPositionX"), result.getInt("lowerPositionZ")),
                    Position2D(result.getInt("upperPositionX"), result.getInt("upperPositionZ"))
                )
                val partition = Partition(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("claimId")), area)
                partitions[partition.id] = partition

                for (chunk in area.getChunks()) {
                    if (chunkPartitions[chunk] == null) {
                        chunkPartitions[chunk] = ArrayList()
                    }
                    chunkPartitions[chunk]!!.add(partition.id)
                }
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}