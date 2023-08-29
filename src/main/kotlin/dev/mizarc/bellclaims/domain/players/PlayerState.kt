package dev.mizarc.bellclaims.domain.players

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D

class PlayerState(private val player: OfflinePlayer, private val config: Config, private val metadata: Chat) {
    var claimOverride = false
    var claimToolMode = 0
    var isHoldingClaimTool = false
    var isVisualisingClaims = false
    var visualisedBlockPositions: MutableMap<Claim, MutableSet<Position3D>> = mutableMapOf()

    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }
}