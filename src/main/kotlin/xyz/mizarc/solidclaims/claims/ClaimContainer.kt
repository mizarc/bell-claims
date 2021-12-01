package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.*
import kotlin.collections.ArrayList

class ClaimContainer {
    lateinit var claims: ArrayList<Claim>
    lateinit var claimPartitions: ArrayList<ClaimPartition>
    lateinit var chunkClaimPartitions: MutableMap<Pair<Int, Int>, ArrayList<ClaimPartition>>

    fun getChunkLocation(position: Pair<Int, Int>) : Pair<Int, Int> {
        return Pair(position.first shr 4, position.second shr 4)
    }

    fun getClaimsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<ClaimPartition>? {
        return chunkClaimPartitions[chunkLocation]
    }

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

    fun getClaimAtLocation(location: Location) : Claim? {
        val claimsInChunk = getClaimsAtChunk(getChunkLocation(getPositionFromLocation(location))) ?: return null

        for (claimPartition in claimsInChunk) {
            if (claimPartition.isLocationInClaim(location)) {
                return claimPartition.claim
            }
        }

        return null
    }

    fun addClaim(worldId: UUID, owner: OfflinePlayer) {
        claims.add(Claim(worldId, owner))
    }

    fun addClaimPartition(claim: Claim, firstLocation: Pair<Int, Int>, secondLocation: Pair<Int, Int>) : Boolean {
        // Check if partition in defined location already exists
        for (claimPartition in claimPartitions) {
            if (claimPartition.firstPosition == firstLocation && claimPartition.secondPosition == secondLocation) {
                return false
            }
        }

        // Add partition to both flat array and chunk map
        val claimPartition = ClaimPartition(claim, firstLocation, secondLocation)
        claimPartitions.add(claimPartition)
        val claimChunks = getClaimChunks(firstLocation, secondLocation)
        for (chunk in claimChunks) {
            if (chunkClaimPartitions[chunk] == null) {
                chunkClaimPartitions[chunk] = ArrayList()
            }
            chunkClaimPartitions[chunk]?.add(claimPartition)
        }

        return true
    }

    fun getLocationFromPosition(claim: Claim, position: Pair<Int, Int>) : Location {
        return Location(claim.getWorld(), position.first.toDouble(), 0.0, position.second.toDouble())
    }

    fun getPositionFromLocation(location: Location) : Pair<Int, Int> {
        return Pair(location.x.toInt(), location.z.toInt())
    }
}