package dev.mizarc.bellclaims.interaction.listeners

import net.milkbowl.vault.chat.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository

class PlayerRegistrationListener(val config: Config, val metadata: Chat,
                                 val playerStateRepo: PlayerStateRepository) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerState = PlayerState(event.player, config, metadata)
        playerStateRepo.add(playerState)
    }
}