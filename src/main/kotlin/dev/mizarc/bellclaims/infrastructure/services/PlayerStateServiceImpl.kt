package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.domain.claims.ClaimRepository
import dev.mizarc.bellclaims.domain.partitions.PartitionRepository
import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

class PlayerStateServiceImpl(private val config: Config, private val metadata: Chat,
                             private val playerStateRepo: PlayerStateRepository,
                             private val claimRepo: ClaimRepository,
                             private val partitionRepo: PartitionRepository): PlayerStateService {
    override fun getAllOnline(): Set<PlayerState> {
        return playerStateRepo.getAll()
    }

    override fun getById(id: UUID): PlayerState? {
        val player = Bukkit.getOfflinePlayer(id)
        return playerStateRepo.get(player)
    }

    override fun getByPlayer(player: OfflinePlayer): PlayerState? {
        return playerStateRepo.get(player)
    }

    override fun getTotalClaimCount(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(Bukkit.getPlayer(player.uniqueId), "bellclaims.claim_limit",
            config.claimLimit).takeIf { it > -1 } ?: -1
    }

    override fun getTotalClaimBlockCount(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(Bukkit.getPlayer(player.uniqueId), "bellclaims.claim_block_limit",
            config.claimBlockLimit).takeIf { it > -1 } ?: -1
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