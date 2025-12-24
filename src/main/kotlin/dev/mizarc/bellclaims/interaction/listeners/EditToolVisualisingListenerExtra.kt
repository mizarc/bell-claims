package dev.mizarc.bellclaims.interaction.listeners

import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent

class EditToolVisualisingListenerExtra(private val mainListener: EditToolVisualisingListener): Listener, KoinComponent {

    /**
     * Triggers when the player joins the server.
     */
    @EventHandler
    fun onPlayerWorldSwitch(event: PlayerClientLoadedWorldEvent) {
        val player = event.player
        mainListener.handleAutoVisualisation(player)
        mainListener.initialisingPlayers.remove(player.uniqueId)
    }
}