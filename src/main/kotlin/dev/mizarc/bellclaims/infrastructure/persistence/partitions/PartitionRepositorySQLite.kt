package dev.mizarc.bellclaims.infrastructure.persistence.partitions

import co.aikar.idb.Database
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.infrastructure.persistence.storage.Storage
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PartitionRepositorySQLite(private val storage: Storage<Database>): PartitionRepository {
    var partitions: MutableMap<UUID, Partition> = mutableMapOf()
    var chunkPartitions: MutableMap<Position2D, ArrayList<UUID>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    override fun getAll(): Set<Partition> {
        return partitions.values.toSet()
    }

    override fun getById(id: UUID): Partition? {
        return partitions[id]
    }

    override fun getByClaim(claim: Claim): Set<Partition> {
        val foundPartitions = ArrayList<Partition>()
        for (partition in partitions.values) {
            if (partition.claimId == claim.id) {
                foundPartitions.add(partition)
            }
        }
        return foundPartitions.toSet()
    }

    override fun getByChunk(position: Position): Set<Partition> {
        val foundPartitions = mutableSetOf<Partition>()
        val localChunkPartitions = chunkPartitions[position] ?: return setOf()

        for (id in localChunkPartitions) {
            foundPartitions.add(partitions[id] ?: continue)
        }

        return foundPartitions
    }

    override fun getByPosition(position: Position): Set<Partition> {
        val partitionsInChunk = getByChunk(position)
        val partitionsInPosition = ArrayList<Partition>()
        for (partition in partitionsInChunk) {
            if (partition.isPositionInPartition(position)) {
                partitionsInPosition.add(partition)
            }
        }
        return partitionsInPosition.toSet()
    }

    override fun add(partition: Partition) {
        addToMemory(partition)
        try {
            storage.connection.executeUpdate("INSERT INTO claimPartitions (id, claimId, lowerPositionX, " +
                    "lowerPositionZ, upperPositionX, upperPositionZ) VALUES (?,?,?,?,?,?);",
                partition.id, partition.claimId, partition.area.lowerPosition2D.x, partition.area.lowerPosition2D.z,
                partition.area.upperPosition2D.x, partition.area.upperPosition2D.z)
            return
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun update(partition: Partition) {
        removeFromMemory(partition)
        addToMemory(partition)
        try {
            storage.connection.executeUpdate("UPDATE claimPartitions SET claimId=?, lowerPositionX=?, " +
                    "lowerPositionZ=?, upperPositionX=?, upperPositionZ=? WHERE id=?;", partition.claimId,
                partition.area.lowerPosition2D.x, partition.area.lowerPosition2D.z, partition.area.upperPosition2D.x,
                partition.area.upperPosition2D.z, partition.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(partition: Partition) {
        removeFromMemory(partition)
        try {
            storage.connection.executeUpdate("DELETE FROM claimPartitions WHERE id=?;", partition.id)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return
    }

    override fun removeByClaim(claim: Claim) {
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