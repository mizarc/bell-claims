package dev.mizarc.bellclaims.infrastructure.players

import dev.mizarc.bellclaims.domain.players.PlayerState
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * Holds a collection of every player on the server.
 */
class PlayerStateRepository {
    var playerStates: MutableMap<UUID, PlayerState> = mutableMapOf()

    /**
     * Gets a specific player state.
     * @param player The player to fetch.
     * @return A PlayerState object of the player. May return null.
     */
    fun getAll() : Set<PlayerState> {
        return playerStates.values.toSet()
    }


    fun get(player: OfflinePlayer) : PlayerState? {
        return playerStates[player.uniqueId]
    }

    fun add(playerState: PlayerState) {
        playerStates[playerState.player.uniqueId] = playerState
    }

    fun removePlayer(playerState: PlayerState){
        playerStates.remove(playerState.player.uniqueId)
    }
}