package dev.mizarc.bellclaims.api.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.listeners.ClaimRule
import org.bukkit.Location

interface ClaimService {
    fun getById()
    fun getByPlayer()
    fun getByLocation(location: Location): Claim?
    fun getBlockCount(claim: Claim): Int
    fun getOuterBorders(claim: Claim): Int
    fun getPartitionedBorders(claim: Claim): Int
    fun getClaimRules(claim: Claim): Set<ClaimRule>
    fun addClaim(claim: Claim)
    fun removeClaim(claim: Claim)
}