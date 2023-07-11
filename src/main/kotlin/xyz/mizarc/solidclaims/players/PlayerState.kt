package xyz.mizarc.solidclaims.players

import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.Config
import xyz.mizarc.solidclaims.claims.PlayerAccess
import java.util.*
import kotlin.collections.ArrayList

class PlayerState(val player: OfflinePlayer, val config: Config, val metadata: Chat) {
    var claimOverride = false

    fun getOnlinePlayer(): Player? {
        return Bukkit.getPlayer(player.uniqueId)
    }

    fun getClaimLimit(): Int {
        return metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_limit", -1)
    }

    fun getClaimBlockLimit(): Int {
        return metadata.getPlayerInfoInteger(getOnlinePlayer(), "solidclaims.claim_block_limit", -1)
    }
}