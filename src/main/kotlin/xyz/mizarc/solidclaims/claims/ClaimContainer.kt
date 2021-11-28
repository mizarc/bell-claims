package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

class ClaimContainer {
    lateinit var claims: ArrayList<Claim>
    lateinit var chunkClaims: Map<Pair<Int, Int>, ArrayList<Claim>>

    fun getChunkLocation(location: Location) : Pair<Int, Int> {
        return Pair(location.chunk.x, location.chunk.z)
    }

    fun getClaimsAtChunk(chunkLocation: Pair<Int, Int>) : ArrayList<Claim>? {
        return chunkClaims[chunkLocation]
    }
}