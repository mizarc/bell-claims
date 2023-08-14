package dev.mizarc.bellclaims.domain.players

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.Config
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D

class PlayerState(val player: OfflinePlayer, val config: Config, val metadata: Chat) {
    var claimOverride = false
    var claimToolMode = 0
    var isHoldingClaimTool = false
    var isVisualisingClaims = false
    var visualisedBlockPositions: MutableMap<Claim, MutableSet<Position3D>> = mutableMapOf()

    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }

    fun getClaimLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "bellclaims.claim_limit", config.claimLimit)
            .takeIf { it > -1 } ?: -1

    fun getClaimBlockLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "bellclaims.claim_block_limit", config.claimBlockLimit)
            .takeIf { it > -1 } ?: -1
}