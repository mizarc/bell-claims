package xyz.mizarc.solidclaims.claims

import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import kotlin.collections.ArrayList

class ClaimContainer {
    lateinit var claims: ArrayList<Claim>
    lateinit var claimPartitions: ArrayList<ClaimPartition>
    lateinit var chunkClaimPartitions: Map<Pair<Int, Int>, ArrayList<ClaimPartition>>

    fun getChunkLocation(location: Location) : Pair<Int, Int> {
        return Pair(location.chunk.x, location.chunk.z)
    }

    fun getClaimsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<ClaimPartition>? {
        return chunkClaimPartitions[chunkLocation]
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

    fun addClaim(world: World, owner: OfflinePlayer) {
        claims.add(Claim(world, owner))
    }
}