package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.config.MainConfig
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import java.util.UUID

class PlayerMetadataServiceVault(private val metadata: Chat,
                                 private val config: MainConfig): PlayerMetadataService {
    override fun getPlayerClaimLimit(playerId: UUID): Int? {
        return metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getOfflinePlayer(playerId),
            "bellclaims.claim_limit", config.claimLimit).takeIf { it > 0 }
    }

    override fun getPlayerClaimBlockLimit(playerId: UUID): Int? {
        return metadata.getPlayerInfoInteger(
            Bukkit.getServer().worlds[0].name, Bukkit.getOfflinePlayer(playerId),
            "bellclaims.claim_block_limit", config.claimLimit).takeIf { it > 0 }
    }
}