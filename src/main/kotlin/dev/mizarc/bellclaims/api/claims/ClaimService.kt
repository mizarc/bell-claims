package dev.mizarc.bellclaims.api.claims

import dev.mizarc.bellclaims.claims.Claim
import dev.mizarc.bellclaims.listeners.ClaimRule
import org.bukkit.Location

interface ClaimService {
    fun getById()
    fun getByPlayer()
    fun getByLocation(location: Location): Claim?
    fun getBlockCount(claim: Claim): Int
    fun getClaimRules(claim: Claim): Set<ClaimRule>
    fun addClaim(claim: Claim)
    fun removeClaim(claim: Claim)
}