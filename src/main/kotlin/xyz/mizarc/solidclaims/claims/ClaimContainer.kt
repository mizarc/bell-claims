package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.Area
import xyz.mizarc.solidclaims.DatabaseStorage
import xyz.mizarc.solidclaims.Position
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains and provides features for navigating claims and claim partitions.
 */
class ClaimContainer(var database: DatabaseStorage) {
    var claims: ArrayList<Claim> = ArrayList()
    var claimPartitions: ArrayList<ClaimPartition> = ArrayList()
    var chunkClaimPartitions: MutableMap<Position, ArrayList<ClaimPartition>> = mutableMapOf()

    fun getCornerPartition(position: Position, world: World): ClaimPartition? {
        val chunk = getChunkLocation(position)
        val partitionsAtChunk = getClaimPartitionsAtChunk(chunk) ?: return null

        for (partition in partitionsAtChunk) {
            if (partition.isPositionInCorner(position, world)) {
                return partition
            }
        }

        return null
    }

    fun getPartitionAdjacent(area: Area, world: World): ClaimPartition? {
        val chunks = getClaimChunks(area)

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
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

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
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

    fun isPartitionOverlap(partition: ClaimPartition): Boolean {
        val chunks = getClaimChunks(partition.area)

        val existingPartitions: MutableSet<ClaimPartition> = mutableSetOf()
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
    fun getClaimPartitionsAtChunk(chunkLocation: Position) : ArrayList<ClaimPartition>? {
        return chunkClaimPartitions[chunkLocation]
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
    fun getClaimPartitionAtLocation(location: Location) : ClaimPartition? {
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
    fun addClaimPartition(claimPartition: ClaimPartition) : Boolean {
        // Check if partition in defined location already exists
        for (existingClaimPartition in claimPartitions) {
            if (claimPartition.area.lowerPosition == existingClaimPartition.area.lowerPosition &&
                claimPartition.area.upperPosition == existingClaimPartition.area.lowerPosition) {
                return false
            }
        }

        // Add partition to both flat array and chunk map
        claimPartitions.add(claimPartition)
        val claimChunks = getClaimChunks(claimPartition.area)
        for (chunk in claimChunks) {
            if (chunkClaimPartitions[chunk] == null) {
                chunkClaimPartitions[chunk] = ArrayList()
            }
            chunkClaimPartitions[chunk]?.add(claimPartition)
        }

        // Add partition to claim object
        if (!claimPartition.claim.claimPartitions.contains(claimPartition)) {
            claimPartition.claim.claimPartitions.add(claimPartition)
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
    fun addNewClaimPartition(claimPartition: ClaimPartition) {
        addClaimPartition(claimPartition)
        database.addClaimPartition(claimPartition)
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
     * @param claimPartition The instance of the claim.
     */
    fun removeClaimPartition(claimPartition: ClaimPartition) : Boolean {
        claimPartitions.remove(claimPartition)

        val chunks = getClaimChunks(claimPartition.area)
        for (chunk in chunks) {
            val savedChunk = chunkClaimPartitions[chunk] ?: return false
            savedChunk.remove(claimPartition)
        }

        claimPartition.claim.claimPartitions.remove(claimPartition)
        return true
    }

    /**
     * Removes a claim partition from memory and database.
     * @param claimPartition The instance of the claim.
     */
    fun removePersistentClaimPartition(claimPartition: ClaimPartition) : Boolean {
        removeClaimPartition(claimPartition)
        database.removeClaimPartition(claimPartition)
        return true
    }

    fun modifyClaimPartition(oldClaimPartition: ClaimPartition, newClaimPartition: ClaimPartition) : Boolean {
        return removeClaimPartition(oldClaimPartition) && addClaimPartition(newClaimPartition)
    }

    fun modifyPersistentClaimPartition(oldClaimPartition: ClaimPartition, newClaimPartition: ClaimPartition) : Boolean {
        return modifyClaimPartition(oldClaimPartition, newClaimPartition) &&
                database.modifyClaimPartitionLocation(oldClaimPartition, newClaimPartition)
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