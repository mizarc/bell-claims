package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

class ClaimContainer {
    lateinit var claims: ArrayList<ClaimPartition>
    lateinit var chunkClaims: Map<Pair<Int, Int>, ArrayList<ClaimPartition>>

    fun getChunkLocation(location: Location) : Pair<Int, Int> {
        return Pair(location.chunk.x, location.chunk.z)
    }

    fun getClaimsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<ClaimPartition>? {
        return chunkClaims[chunkLocation]
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
}