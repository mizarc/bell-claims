package xyz.mizarc.solidclaims.events

import net.milkbowl.vault.chat.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.mizarc.solidclaims.Config
import xyz.mizarc.solidclaims.players.PlayerStateRepository
import xyz.mizarc.solidclaims.players.PlayerState

class PlayerRegistrationListener(val config: Config, val metadata: Chat,
                                 val playerStateRepository: PlayerStateRepository) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerState = PlayerState(event.player, config, metadata)
        playerStateRepository.add(playerState)
    }
}