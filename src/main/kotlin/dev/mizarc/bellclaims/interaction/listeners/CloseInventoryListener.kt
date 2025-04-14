package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.UnregisterClaimMenuOpening
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class CloseInventoryListener(private val unregisterClaimMenuOpening: UnregisterClaimMenuOpening) : Listener {
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.player !is Player) return
        if (event.reason == InventoryCloseEvent.Reason.OPEN_NEW) return
        if (event.reason == InventoryCloseEvent.Reason.UNKNOWN) return
        unregisterClaimMenuOpening.execute(event.player.uniqueId)
    }
}