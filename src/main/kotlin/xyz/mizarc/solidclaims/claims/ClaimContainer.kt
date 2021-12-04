package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import xyz.mizarc.solidclaims.DatabaseStorage
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
     * Converts the block coordinates to chunk coordinates.
     * @param position The integer pair defining a block's X and Z coordinates.
     * @return An integer pair defining a chunk's X and Z coordinates.
     */
    fun getChunkLocation(position: Pair<Int, Int>) : Pair<Int, Int> {
        return Pair(position.first shr 4, position.second shr 4)
    }

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
        val firstChunk = getChunkLocation(firstLocation)
        val secondChunk = getChunkLocation(secondLocation)

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
    fun getClaimAtLocation(location: Location) : Claim? {
        val claimsInChunk = getClaimPartitionsAtChunk(
            getChunkLocation(getPositionFromLocation(location))) ?: return null

        for (claimPartition in claimsInChunk) {
            if (claimPartition.isLocationInClaim(location)) {
                return claimPartition.claim
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
        database.addClaim(claim.worldId, claim.owner.uniqueId)
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
            if (claimPartition.firstPosition == claimPartition.firstPosition &&
                claimPartition.secondPosition == claimPartition.secondPosition) {
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
        database.addClaimPartition(claimPartition.claim.id, claimPartition.firstPosition, claimPartition.secondPosition)
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
}