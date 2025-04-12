package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.services.old.PlayerStateService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class PlayerStateListener(private val playerStateService: PlayerStateService) : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.player !is Player) return
        if (event.reason == InventoryCloseEvent.Reason.OPEN_NEW) return
        if (event.reason == InventoryCloseEvent.Reason.UNKNOWN) return

        val playerState = playerStateService.getByPlayer((event.player as Player))

        if (playerState?.isInClaimMenu != null) {
            playerState.isInClaimMenu = null
        }
    }
}