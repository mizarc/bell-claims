package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.PlayerLocaleService
import org.bukkit.Bukkit
import java.util.*

class PlayerLocaleServicePaper: PlayerLocaleService {
    override fun getLocale(playerId: UUID): String {
        val player = Bukkit.getPlayer(playerId) ?: return ""
        return player.locale().toString()
    }
}