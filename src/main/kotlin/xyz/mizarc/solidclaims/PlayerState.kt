package xyz.mizarc.solidclaims

import net.milkbowl.vault.chat.Chat
import org.bukkit.OfflinePlayer

class PlayerState(val metadata: Chat) {
    fun getClaimLimit(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(player.player, "solidclaims.claim_limit", -1)
    }

    fun getClaimBlockLimit(player: OfflinePlayer): Int {
        return metadata.getPlayerInfoInteger(player.player, "solidclaims.claim_block_limit", -1)
    }
}