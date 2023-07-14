package xyz.mizarc.solidclaims.partitions

import co.aikar.idb.Database
import org.bukkit.Bukkit
import xyz.mizarc.solidclaims.Repository
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.storage.Storage
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PartitionRepository(private val storage: Storage<Database>): Repository<Partition> {
    var partitions: ArrayList<Partition> = ArrayList()
    var chunkPartitions: MutableMap<Position2D, ArrayList<Partition>> = mutableMapOf()

    init {
        createTable()
        preload()
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

    fun getByClaim(claim: Claim): ArrayList<Partition> {
        val foundPartitions = ArrayList<Partition>()
        for (partition in partitions) {
            if (partition.claimId == claim.id) {
                foundPartitions.add(partition)
            }
        }
        return foundPartitions
    }

    fun getByChunk(position2D: Position2D): ArrayList<Partition> {
        if (chunkPartitions[position2D] == null) {
            return ArrayList()
        }

        return chunkPartitions[position2D]!!
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

    override fun add(entity: Partition) {
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

    override fun update(entity: Partition) {
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

    fun preload() {
        try {
            val results = storage.connection.getResults("SELECT * FROM claimPartitions")
            Bukkit.getLogger().info("Ah")
            for (result in results) {
                val area = Area(
                    Position2D(result.getInt("lowerPositionX"), result.getInt("lowerPositionZ")),
                    Position2D(result.getInt("upperPositionX"), result.getInt("upperPositionZ"))
                )
                val partition = Partition(UUID.fromString(result.getString("id")),
                    UUID.fromString(result.getString("claimId")), area)
                partitions.add(partition)
                Bukkit.getLogger().info("Bruh")

                for (chunk in area.getChunks()) {
                    if (chunkPartitions[chunk] == null) {
                        chunkPartitions[chunk] = ArrayList()
                    }
                    chunkPartitions[chunk]!!.add(partition)
                    Bukkit.getLogger().info("${chunk.x}, ${chunk.z}")
                    Bukkit.getLogger().info("$partition")
                }
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}