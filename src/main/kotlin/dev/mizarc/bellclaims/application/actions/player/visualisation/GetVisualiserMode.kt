package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.visualisation.GetVisualiserModeResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class GetVisualiserMode(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): GetVisualiserModeResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Toggle override and persist to storage
        return GetVisualiserModeResult.Success(playerState.claimToolMode)
    }
}