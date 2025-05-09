package dev.mizarc.bellclaims.infrastructure.persistence.partitions

import co.aikar.idb.Database
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.*
import dev.mizarc.bellclaims.infrastructure.persistence.storage.Storage
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PartitionRepositorySQLite(private val storage: Storage<Database>): PartitionRepository {
    private var partitions: MutableMap<UUID, Partition> = mutableMapOf()
    private var chunkPartitions: MutableMap<Position2D, ArrayList<UUID>> = mutableMapOf()

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

    override fun getByClaim(claimId: UUID): Set<Partition> {
        val foundPartitions = ArrayList<Partition>()
        for (partition in partitions.values) {
            if (partition.claimId == claimId) {
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
        val partitionsInChunk = getByChunk(position.getChunk())
        val partitionsInPosition = ArrayList<Partition>()
        for (partition in partitionsInChunk) {
            if (partition.isPositionInPartition(position)) {
                partitionsInPosition.add(partition)
            }
        }
        return partitionsInPosition.toSet()
    }

    override fun add(partition: Partition): Boolean {
        addToMemory(partition)
        try {
            val rowsAffected = storage.connection.executeUpdate("INSERT INTO claim_partitions (id, claim_id, " +
                    "lower_position_x, lower_position_z, upper_position_x, upper_position_z) VALUES (?,?,?,?,?,?);",
                partition.id, partition.claimId, partition.area.lowerPosition2D.x, partition.area.lowerPosition2D.z,
                partition.area.upperPosition2D.x, partition.area.upperPosition2D.z)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add partition '${partition.id}' to the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun update(partition: Partition): Boolean {
        removeFromMemory(partition)
        addToMemory(partition)
        try {
            val rowsAffected = storage.connection.executeUpdate("UPDATE claim_partitions SET claim_id=?, " +
                    "lower_position_x=?, lower_position_z=?, upper_position_x=?, upper_position_z=? WHERE id=?;",
                partition.claimId, partition.area.lowerPosition2D.x, partition.area.lowerPosition2D.z,
                partition.area.upperPosition2D.x, partition.area.upperPosition2D.z, partition.id)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to add update partition_id '${partition.id}' in the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun remove(partitionId: UUID): Boolean {
        val partition = getById(partitionId) ?: return false
        removeFromMemory(partition)
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claim_partitions WHERE id=?;",
                partitionId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove partition '${partitionId}' from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    override fun removeByClaim(claimId: UUID): Boolean {
        val partitions = getByClaim(claimId)
        for (partition in partitions) {
            removeFromMemory(partition)
        }
        try {
            val rowsAffected = storage.connection.executeUpdate("DELETE FROM claim_partitions WHERE claim_id=?;",
                claimId)
            return rowsAffected > 0
        } catch (error: SQLException) {
            throw DatabaseOperationException("Failed to remove partitions for claim '${claimId}' from the database. " +
                    "Cause: ${error.message}", error)
        }
    }

    private fun addToMemory(partition: Partition) {
        partitions[partition.id] = partition
        val claimChunks = partition.getChunks()
        for (chunk in claimChunks) {
            if (chunkPartitions[chunk] == null) {
                chunkPartitions[chunk] = ArrayList()
            }
            chunkPartitions[chunk]?.add(partition.id)
        }
    }

    private fun removeFromMemory(partition: Partition) {
        partitions.remove(partition.id)
        for (chunk in partition.area.getChunks()) {
            val savedChunk = chunkPartitions[chunk] ?: return
            savedChunk.remove(partition.id)
        }
    }

    /**
     * Creates a new table to store claim partition data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS claim_partitions " +
                    "(id TEXT, claim_id TEXT, lower_position_x INTEGER NOT NULL, lower_position_z INTEGER NOT NULL, " +
                    "upper_position_x INTEGER NOT NULL, upper_position_z INTEGER NOT NULL);")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all partitions from the database and saves it to memory.
     */
    private fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claim_partitions")
            for (result in results) {
                val area = Area(
                    Position2D(result.getInt("lower_position_x"), result.getInt("lower_position_z")),
                    Position2D(result.getInt("upper_position_x"), result.getInt("upper_position_z"))
                )
                val partition = Partition(
                    UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("claim_id")), area
                )
                partitions[partition.id] = partition

                for (chunk in area.getChunks()) {
                    if (chunkPartitions[chunk] == null) {
                        chunkPartitions[chunk] = ArrayList()
                    }
                    chunkPartitions[chunk]?.add(partition.id)
                }
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}