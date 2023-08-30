package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.api.PlayerStateService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerRegistrationListener(private val playerStateService: PlayerStateService) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerStateService.registerPlayer(event.player)
    }
}