package xyz.mizarc.solidclaims.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.mizarc.solidclaims.PlayerContainer
import xyz.mizarc.solidclaims.PlayerState

class PlayerRegistrationListener(val playerContainer: PlayerContainer) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerState = PlayerState(event.player.uniqueId, 5000, 5000)
        playerContainer.addNewPlayer(playerState)
    }
}