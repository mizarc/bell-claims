package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.players.PlayerState
import java.util.UUID

/**
 * A repository that handles the persistence of player state.
 */
interface PlayerStateRepository {
    /**
     * Gets all registered player states.
     *
     * @return The set of player states.
     */
    fun getAll(): Set<PlayerState>

    /**
     * Gets a player state by its id.
     * @param id The unique id of the player.
     * @return A player's player state, or null if not found.
     */
    fun get(id: UUID): PlayerState?

    /**
     * Adds a new player state.
     *
     * @param playerState The player state to add.
     */
    fun add(playerState: PlayerState)

    /**
     * Removes an existing player state.
     *
     * @param playerState The player state to remove.
     */
    fun remove(playerState: PlayerState)
}