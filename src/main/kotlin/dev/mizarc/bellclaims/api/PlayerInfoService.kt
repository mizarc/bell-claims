package dev.mizarc.bellclaims.api

import org.bukkit.OfflinePlayer
import java.util.UUID

interface PlayerInfoService {
    fun getAllOnline()
    fun getById(id: UUID)
    fun getRemainingClaims(player: OfflinePlayer)
    fun getRemainingClaimBlocks(player: OfflinePlayer)
}