package xyz.mizarc.solidclaims

import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

/**
 * Holds a collection of every player on the server.
 */
class PlayerContainer(var database: DatabaseStorage) {
    lateinit var playerStates: ArrayList<PlayerState>

    /**
     * Gets a specific player state.
     * @param playerId The unique identifier for a player.
     * @return A PlayerState object of the player. May return null.
     */
    fun getPlayer(playerId: UUID) : PlayerState? {
        for (playerState in playerStates) {
            if (playerState.id == playerId) {
                return playerState
            }
        }

        return null
    }

    fun addPlayer(playerState: PlayerState) : Boolean {
        for (existingPlayerState in playerStates) {
            if (existingPlayerState.id == playerState.id) {
                return false
            }
        }

        playerStates.add(playerState)
        return true
    }

    fun addNewPlayer(playerState: PlayerState) : Boolean {
        if (addPlayer(playerState)) {
            database.addPlayerState(playerState)
            return true
        }
        return false
    }

    fun removePlayer(playerState: PlayerState) : Boolean {
        return playerStates.remove(playerState)
    }

    fun removePersistentPlayer(playerState: PlayerState) : Boolean {
        if (removePlayer(playerState)) {
            database.removePlayerState(playerState)
            return true
        }
        return false
    }
}