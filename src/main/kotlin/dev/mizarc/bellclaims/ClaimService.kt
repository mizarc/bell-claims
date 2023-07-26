package dev.mizarc.bellclaims

import org.bukkit.Location
import org.bukkit.OfflinePlayer
import dev.mizarc.bellclaims.claims.*
import dev.mizarc.bellclaims.listeners.ClaimRule
import dev.mizarc.bellclaims.partitions.PartitionRepository
import dev.mizarc.bellclaims.partitions.Position3D
import dev.mizarc.bellclaims.players.PlayerStateRepository
import java.util.UUID

class ClaimService(private val claimRepo: ClaimRepository,
                   private val partitionRepo: PartitionRepository,
                   private val claimRuleRepo: ClaimRuleRepository,
                   private val claimPermissionRepo: ClaimPermissionRepository,
                   private val playerAccessRepo: PlayerAccessRepository,
                   private val playerStateRepo: PlayerStateRepository) {

    fun getById(id: UUID): Claim? {
        return claimRepo.getById(id)
    }

    fun getByPlayer(player: OfflinePlayer): Set<Claim> {
        return claimRepo.getByPlayer(player)
    }

    fun getByLocation(location: Location): Claim? {
        return claimRepo.getByPosition(Position3D(location))
    }

    fun getBlockCount(claim: Claim): Int {
        val claimPartitions = partitionRepo.getByClaim(claim)
        var count = 0
        for (partition in claimPartitions) {
            count += partition.area.getBlockCount()
        }
        return count
    }

    fun getUsedClaimCount(player: OfflinePlayer): Int {
        var count = 0
        val playerClaims = claimRepo.getByPlayer(player)
        for (claim in playerClaims) {
            count += 1
        }
        return count
    }

    fun getRemainingClaimCount(player: OfflinePlayer): Int? {
        val playerState = playerStateRepo.get(player) ?: return null
        return playerState.getClaimLimit() - getUsedClaimCount(player)
    }

    fun getRemainingClaimBlockCount(player: OfflinePlayer): Int? {
        val playerState = playerStateRepo.get(player) ?: return null
        return playerState.getClaimBlockLimit() - getUsedClaimBlockCount(player)
    }

    fun getUsedClaimBlockCount(player: OfflinePlayer): Int {
        var count = 0
        val playerClaims = getByPlayer(player)
        for (claim in playerClaims) {
            count += getBlockCount(claim)
        }
        return count
    }

    fun getClaimRules(claim: Claim): Set<ClaimRule> {
        return claimRuleRepo.getByClaim(claim)
    }

    fun removeClaim(claim: Claim) {
        claimRuleRepo.removeClaim(claim)
        claimPermissionRepo.removeClaim(claim)
        playerAccessRepo.removeClaim(claim)
        partitionRepo.removeByClaim(claim)
        claimRepo.remove(claim)
    }
}