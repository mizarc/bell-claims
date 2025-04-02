package dev.mizarc.bellclaims.infrastructure.persistence.players

import dev.mizarc.bellclaims.domain.entities.PlayerState
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import java.util.*

/**
 * Holds a collection of every player on the server.
 */
class PlayerStateRepositoryMemory: PlayerStateRepository {
    private var playerStates: MutableMap<UUID, PlayerState> = mutableMapOf()

    override fun getAll() : Set<PlayerState> {
        return playerStates.values.toSet()
    }

    override fun get(id: UUID) : PlayerState? {
        return playerStates[id]
    }

    override fun add(playerState: PlayerState) {
        playerStates[playerState.player.uniqueId] = playerState
    }

    override fun remove(playerState: PlayerState){
        playerStates.remove(playerState.player.uniqueId)
    }
}