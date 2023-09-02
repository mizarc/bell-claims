package dev.mizarc.bellclaims.infrastructure.persistence.players

import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.domain.players.PlayerStateRepository
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * Holds a collection of every player on the server.
 */
class PlayerStateRepositoryMemory: PlayerStateRepository {
    var playerStates: MutableMap<UUID, PlayerState> = mutableMapOf()

    override fun getAll() : Set<PlayerState> {
        return playerStates.values.toSet()
    }

    override fun get(player: OfflinePlayer) : PlayerState? {
        return playerStates[player.uniqueId]
    }

    override fun add(playerState: PlayerState) {
        playerStates[playerState.player.uniqueId] = playerState
    }

    override fun remove(playerState: PlayerState){
        playerStates.remove(playerState.player.uniqueId)
    }
}