package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.DatabaseStorage
import xyz.mizarc.solidclaims.events.ClaimPermission
import xyz.mizarc.solidclaims.events.ClaimRule
import java.util.*
import kotlin.collections.ArrayList

/**
 * Contains and provides features for navigating claims and claim partitions.
 */
class ClaimContainer(var database: DatabaseStorage) {
    var claims: ArrayList<Claim> = ArrayList()
    var claimPartitions: ArrayList<ClaimPartition> = ArrayList()
    var chunkClaimPartitions: MutableMap<Pair<Int, Int>, ArrayList<ClaimPartition>> = mutableMapOf()

    /**
     * Gets all the claim partitions that exist in a given chunk.
     * @param chunkLocation The integer pair defining a chunk's X and Z coordinates.
     * @return An array of claim partitions that exist in that claim. May return null.
     */
    fun getClaimPartitionsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<ClaimPartition>? {
        return chunkClaimPartitions[chunkLocation]
    }

    /**
     * Gets a list of all the chunks that a rectangle of two locations overlap.
     * @param firstLocation The integer pair defining a location.
     * @param secondLocation The integer pair defining a second location.
     * @return An array of integer pairs defining chunk locations.
     */
    fun getClaimChunks(firstLocation: Pair<Int, Int>, secondLocation: Pair<Int, Int>) : ArrayList<Pair<Int, Int>> {
        val newLocations = sortPositionSizes(firstLocation, secondLocation)
        val firstChunk = getChunkLocation(newLocations.first)
        val secondChunk = getChunkLocation(newLocations.second)

        val chunks: ArrayList<Pair<Int, Int>> = ArrayList()
        for (x in firstChunk.first..secondChunk.first) {
            for (z in firstChunk.second..secondChunk.second) {
                chunks.add(Pair(x, z))
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
        val claimsInChunk = getClaimPartitionsAtChunk(
            getChunkLocation(getPositionFromLocation(location))) ?: return null

        for (claimPartition in claimsInChunk) {
            if (claimPartition.isLocationInClaim(location)) {
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
            if (claimPartition.firstPosition == existingClaimPartition.firstPosition &&
                claimPartition.secondPosition == existingClaimPartition.firstPosition) {
                return false
            }
        }

        // Add partition to both flat array and chunk map
        claimPartitions.add(claimPartition)
        val claimChunks = getClaimChunks(claimPartition.firstPosition, claimPartition.secondPosition)
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

        val chunks = getClaimChunks(claimPartition.firstPosition, claimPartition.secondPosition)
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
        database.removeClaimPartition(claimPartition.firstPosition, claimPartition.secondPosition)
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

    fun addClaimRule(claim: Claim, rule: ClaimRule) : Boolean {
        return if (rule in claim.rules) {
            false
        } else {
            claim.rules.add(rule)
            true
        }
    }

    fun addNewClaimRule(claim: Claim, rule: ClaimRule) : Boolean {
        database.addClaimRule(claim.id, rule)
        return addClaimRule(claim, rule)
    }

    fun removeClaimRule(claim: Claim, rule: ClaimRule) : Boolean {
        return if (rule !in claim.rules) {
            false
        } else {
            claim.rules.remove(rule)
            database.removeClaimRule(claim.id, rule)
            true
        }
    }

    companion object {
        /**
         * Converts the block coordinates to chunk coordinates.
         * @param position The integer pair defining a block's X and Z coordinates.
         * @return An integer pair defining a chunk's X and Z coordinates.
         */
        fun getChunkLocation(position: Pair<Int, Int>) : Pair<Int, Int> {
            return Pair(position.first shr 4, position.second shr 4)
        }

        /**
         * Converts a position of integer pairs to a location object.
         * @param claim The claim as a reference.
         * @param position The integer pair defining the position in world.
         * @return A location object based on the specified world and position.
         */
        fun getLocationFromPosition(claim: Claim, position: Pair<Int, Int>) : Location {
            return Location(claim.getWorld(), position.first.toDouble(), 0.0, position.second.toDouble())
        }

        /**
         * Convert a location object to an integer pair of X and Z.
         * @param location The location object to convert.
         * @return An integer pair of X and Z.
         */
        fun getPositionFromLocation(location: Location) : Pair<Int, Int> {
            return Pair(location.x.toInt(), location.z.toInt())
        }

        fun sortPositionSizes(firstPosition: Pair<Int, Int>, secondPosition: Pair<Int, Int>) :
                Pair<Pair<Int, Int>, Pair<Int, Int>> {
            var newFirstPosition = Pair(firstPosition.first, firstPosition.second)
            var newSecondPosition = Pair(secondPosition.first, secondPosition.second)

            if (firstPosition.first > secondPosition.first) {
                newFirstPosition = Pair(secondPosition.first, firstPosition.second)
                newSecondPosition = Pair(firstPosition.first, secondPosition.second)
            }

            if (firstPosition.second > secondPosition.second) {
                newFirstPosition = Pair(newFirstPosition.first, secondPosition.second)
                newSecondPosition = Pair(newSecondPosition.first, firstPosition.second)
            }

            return Pair(newFirstPosition, newSecondPosition)
        }
    }
}