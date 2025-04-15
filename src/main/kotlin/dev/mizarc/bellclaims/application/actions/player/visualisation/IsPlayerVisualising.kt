package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.visualisation.IsPlayerVisualisingResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class IsPlayerVisualising(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): IsPlayerVisualisingResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        if (playerState.isVisualisingClaims) {
            return IsPlayerVisualisingResult.Success(true)
        }
        return IsPlayerVisualisingResult.Success(false)
    }
}