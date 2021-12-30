package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.DatabaseStorage
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains and provides features for navigating claims and claim partitions.
 */
class ClaimContainer(var database: DatabaseStorage) {
    var claims: ArrayList<Claim> = ArrayList()
    var partitions: ArrayList<Partition> = ArrayList()
    var chunkPartitions: MutableMap<Position, ArrayList<Partition>> = mutableMapOf()

    fun getCornerPartition(position: Position, world: World): Partition? {
        val chunk = getChunkLocation(position)
        val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: return null

        for (partition in partitionsAtChunk) {
            if (partition.isPositionInCorner(position, world)) {
                return partition
            }
        }

        return null
    }

    fun getPartitionAdjacent(area: Area, world: World): Partition? {
        val chunks = getClaimChunks(area)

        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isAreaAdjacent(area, world)) {
                return partition
            }
        }

        return null
    }

    fun isPositionOverlap(position: Position, world: World): Boolean {
        val chunk = getChunkLocation(position)
        val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: return false

        for (partition in partitionsAtChunk) {
            if (partition.isPositionInPartition(position, world)) {
                return true
            }
        }

        return false
    }

    fun isAreaOverlap(area: Area, world: World): Boolean {
        val chunks = getClaimChunks(area)

        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (partition in existingPartitions) {
            if (partition.isAreaOverlap(area, world)) {
                return true
            }
        }

        return false
    }

    fun isPartitionOverlap(partition: Partition): Boolean {
        val chunks = getClaimChunks(partition.area)

        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: continue
            existingPartitions.addAll(partitionsAtChunk)
        }

        for (existingPartition in existingPartitions) {
            if (partition == existingPartition) {
                continue
            }
            if (partition.isAreaOverlap(partition.area, partition.claim.getWorld()!!)) {
                return true
            }
        }

        return false
    }

    /**
     * Gets all the claim partitions that exist in a given chunk.
     * @param chunkLocation The integer pair defining a chunk's X and Z coordinates.
     * @return An array of claim partitions that exist in that claim. May return null.
     */
    fun getClaimPartitionsAtChunk(chunkLocation: Position) : ArrayList<Partition>? {
        return chunkPartitions[chunkLocation]
    }

    /**
     * Gets a list of all the chunks that a rectangle of two locations overlap.
     * @param firstLocation The integer pair defining a location.
     * @param secondLocation The integer pair defining a second location.
     * @return An array of integer pairs defining chunk locations.
     */
    fun getClaimChunks(area: Area) : ArrayList<Position> {
        val firstChunk = getChunkLocation(area.lowerPosition)
        val secondChunk = getChunkLocation(area.upperPosition)

        val chunks: ArrayList<Position> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position(x, z))
            }
        }

        return chunks
    }

    /**
     * Gets the claim at the specified location.
     * @param location The location object defining the position in the world.
     * @return A claim at the current position if available. May return null.
     */
    fun getClaimPartitionAtLocation(location: Location) : Partition? {
        val claimsInChunk = getClaimPartitionsAtChunk(getChunkLocation(Position(location))) ?: return null
        for (claimPartition in claimsInChunk) {
            if (claimPartition.isPositionInPartition(Position(location), location.world!!)) {
                return claimPartition
            }
        }
        return null
    }

    /**
     * Creates a new claim.
     * @param worldId The unique identifier for the world the claim is to be created in.
     * @param owner The player who should own the world.
     */
    fun addClaim(claim: Claim) {
        claims.add(claim)
    }

    /**
     * Creates a new claim and adds it to the database.
     * @param worldId The unique identifier for the world the claim is to be created in.
     * @param owner The player who should own the world.
     */
    fun addNewClaim(claim: Claim) {
        addClaim(claim)
        database.addClaim(claim)
    }

    /**
     * Creates a new claim partition for a claim.
     * @param claim The Claim object to be linked to.
     * @param firstLocation The integer pair defining the first position.
     * @param secondLocation The integer pair defining the second position.
     * @return True if a claim partition has been created and not overlapping any other partition.
     */
    fun addClaimPartition(partition: Partition) : Boolean {
        // Check if partition in defined location already exists
        for (existingClaimPartition in partitions) {
            if (partition.area.lowerPosition == existingClaimPartition.area.lowerPosition &&
                partition.area.upperPosition == existingClaimPartition.area.lowerPosition) {
                return false
            }
        }

        // Add partition to both flat array and chunk map
        partitions.add(partition)
        val claimChunks = getClaimChunks(partition.area)
        for (chunk in claimChunks) {
            if (chunkPartitions[chunk] == null) {
                chunkPartitions[chunk] = ArrayList()
            }
            chunkPartitions[chunk]?.add(partition)
        }

        // Add partition to claim object
        if (!partition.claim.partitions.contains(partition)) {
            partition.claim.partitions.add(partition)
        }
        return true
    }

    /**
     * Creates a new claim partition for a claim and adds it to the database.
     * @param claim The Claim object to be linked to.
     * @param firstLocation The integer pair defining the first position.
     * @param secondLocation The integer pair defining the second position.
     * @return True if a claim partition has been created and not overlapping any other partition.
     */
    fun addNewClaimPartition(partition: Partition) {
        addClaimPartition(partition)
        database.addClaimPartition(partition)
    }

    /**
     * Removes a claim from memory.
     * @param claim The instance of the claim.
     */
    fun removeClaim(claim: Claim) {
        claims.remove(claim)
    }

    /**
     * Removes a claim from memory and database.
     * @param claim The instance of the claim.
     */
    fun removePersistentClaim(claim: Claim) : Boolean {
        removeClaim(claim)
        database.removeClaim(claim.id)
        return true
    }

    /**
     * Removes a claim partition from memory.
     * @param partition The instance of the claim.
     */
    fun removeClaimPartition(partition: Partition) : Boolean {
        partitions.remove(partition)

        val chunks = getClaimChunks(partition.area)
        for (chunk in chunks) {
            val savedChunk = chunkPartitions[chunk] ?: return false
            savedChunk.remove(partition)
        }

        partition.claim.partitions.remove(partition)
        return true
    }

    /**
     * Removes a claim partition from memory and database.
     * @param partition The instance of the claim.
     */
    fun removePersistentClaimPartition(partition: Partition) : Boolean {
        removeClaimPartition(partition)
        database.removeClaimPartition(partition)
        return true
    }

    fun modifyClaimPartition(oldPartition: Partition, newPartition: Partition) : Boolean {
        return removeClaimPartition(oldPartition) && addClaimPartition(newPartition)
    }

    fun modifyPersistentClaimPartition(oldPartition: Partition, newPartition: Partition) : Boolean {
        return modifyClaimPartition(oldPartition, newPartition) &&
                database.modifyClaimPartitionLocation(oldPartition, newPartition)
    }

    fun modifyMainPartition(claim: Claim, partition: Partition) {
        database.modifyMainPartition(claim.mainPartition!!, partition)
        claim.mainPartition = partition
    }

    fun addClaimPermission(claim: Claim, playerAccess: PlayerAccess) : Boolean {
        for (claimPlayer in claim.playerAccesses) {
            if (claimPlayer.id == playerAccess.id) {
                return false
            }
        }
        claim.playerAccesses.add(playerAccess)
        return true
    }

    fun addClaimPermission(claim: Claim, player: Player, permission: ClaimPermission) : Boolean {
        for (claimPlayer in claim.playerAccesses) {
            if (claimPlayer.id == player.uniqueId) {
                if (permission in claimPlayer.claimPermissions) {
                    return false
                }
                claimPlayer.claimPermissions.add(permission)
                return true
            }
        }
        val playerAccess = PlayerAccess(player.uniqueId)
        playerAccess.claimPermissions.add(permission)
        claim.playerAccesses.add(playerAccess)
        return true
    }

    fun addNewClaimPermission(claim: Claim, player: Player, permission: ClaimPermission) : Boolean {
        database.addPlayerClaimPermission(player.uniqueId, claim.id, permission)
        return addClaimPermission(claim, player, permission)
    }

    companion object {
        /**
         * Converts the block coordinates to chunk coordinates.
         * @param position The integer pair defining a block's X and Z coordinates.
         * @return An integer pair defining a chunk's X and Z coordinates.
         */
        fun getChunkLocation(position: Position) : Position {
            return Position(position.x shr 4, position.z shr 4)
        }
    }
}