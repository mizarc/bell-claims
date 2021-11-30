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

    fun getChunkLocation(location: Location) : Pair<Int, Int> {
        return Pair(location.chunk.x, location.chunk.z)
    }

    fun getChunkLocation(claim: Claim, positionX: Int, positionZ: Int) {
        getChunkLocation(Location(claim.getWorld(), positionX.toDouble(), 0.0, positionZ.toDouble()))
    }

    fun getClaimsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<ClaimPartition>? {
        return chunkClaimPartitions[chunkLocation]
    }

    fun getClaimChunks(firstLocation: Location, secondLocation: Location) : ArrayList<Pair<Int, Int>> {
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

    fun getClaimChunks(world: World, firstPositionX: Int, firstPositionZ: Int,
                       secondPositionX: Int, secondPositionZ: Int) : ArrayList<Pair<Int, Int>> {
        return getClaimChunks(Location(world, firstPositionX.toDouble(), 0.0, firstPositionZ.toDouble()),
            Location(world, secondPositionX.toDouble(), 0.0, secondPositionZ.toDouble()))
    }

    fun getClaimAtLocation(location: Location) : Claim? {
        val claimsInChunk = getClaimsAtChunk(getChunkLocation(location)) ?: return null

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

    fun addClaimPartition(claim: Claim, firstLocation: Location, secondLocation: Location) : Boolean {
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

    fun addClaimPartition(claim: Claim, firstPositionX: Int, firstPositionZ: Int,
                          secondPositionX: Int, secondPositionZ: Int) {
        addClaimPartition(claim,
            Location(claim.getWorld(), firstPositionX.toDouble(), 0.0, firstPositionZ.toDouble()),
            Location(claim.getWorld(), secondPositionX.toDouble(), 0.0, secondPositionZ.toDouble()))
    }
}