package dev.mizarc.bellclaims.infrastructure.services.playerlimit

import dev.mizarc.bellclaims.api.PlayerLimitService
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import org.bukkit.OfflinePlayer

class SimplePlayerLimitServiceImpl(private val config: Config, private val claimRepo: ClaimRepository,
                                   private val partitionRepo: PartitionRepository): PlayerLimitService {
    override fun getTotalClaimCount(player: OfflinePlayer): Int {
        return config.claimLimit
    }

    override fun getTotalClaimBlockCount(player: OfflinePlayer): Int {
        return config.claimBlockLimit
    }

    override fun getUsedClaimsCount(player: OfflinePlayer): Int {
        return claimRepo.getByPlayer(player).count()
    }

    override fun getUsedClaimBlockCount(player: OfflinePlayer): Int {
        val claims = claimRepo.getByPlayer(player)
        val count = claims.sumOf { claim ->
            val partitions = partitionRepo.getByClaim(claim)
            partitions.sumOf { partition -> partition.getBlockCount() }
        }
        return count
    }

    override fun getRemainingClaimCount(player: OfflinePlayer): Int {
        return getTotalClaimCount(player) - getUsedClaimsCount(player)
    }

    override fun getRemainingClaimBlockCount(player: OfflinePlayer): Int {
        return getTotalClaimBlockCount(player) - getUsedClaimBlockCount(player)
    }
}