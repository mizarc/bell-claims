package xyz.mizarc.solidclaims.players

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.Config

class PlayerState(val player: OfflinePlayer, val config: Config, val metadata: Chat) {
    var claimOverride = false

    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }

    fun getClaimLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_limit", config.claimLimit)
            .takeIf { it > -1 } ?: -1

    fun getClaimBlockLimit(): Int =
        metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_block_limit", config.claimBlockLimit)
            .takeIf { it > -1 } ?: -1
}