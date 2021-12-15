package xyz.mizarc.solidclaims

import java.util.*
import kotlin.collections.ArrayList

/**
 * Holds a collection of every player on the server.
 */
class PlayerContainer {
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
}