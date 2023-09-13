package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.PlayerLimitService
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PlayerLimitServiceImpl(private val config: Config, private val metadata: Chat,
                             private val claimRepo: ClaimRepository,
                             private val partitionRepo: PartitionRepository): PlayerLimitService {
    override fun getTotalClaimCount(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, player,
            "bellclaims.claim_limit", config.claimLimit).takeIf { it > 0 } ?: 0
    }

    override fun getTotalClaimBlockCount(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, player,
            "bellclaims.claim_block_limit", config.claimBlockLimit).takeIf { it > 0 } ?: 0
    }

    override fun getUsedClaimsCount(player: OfflinePlayer): Int {
        return claimRepo.getByPlayer(player).count()
    }

    override fun getUsedClaimBlockCount(player: OfflinePlayer): Int {
        val claims = claimRepo.getByPlayer(player)
        var count = 0
        for (claim in claims) {
            val partitions = partitionRepo.getByClaim(claim)
            for (partition in partitions) {
                count += partition.getBlockCount()
            }
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