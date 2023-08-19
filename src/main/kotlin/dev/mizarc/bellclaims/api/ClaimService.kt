package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimRule
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import java.util.UUID

interface ClaimService {
    fun getById(id: UUID): Claim?
    fun getByPlayer(player: OfflinePlayer): Claim?
    fun getByLocation(location: Location): Claim?
    fun getBlockCount(claim: Claim): Int
    fun getOuterBorders(claim: Claim): Int
    fun getPartitionedBorders(claim: Claim): Int
    fun getClaimRules(claim: Claim): Set<ClaimRule>
    fun addClaim(claim: Claim)
    fun removeClaim(claim: Claim)
}