package dev.mizarc.bellclaims.application.actions.player

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.ToggleVisualiserModeResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class ToggleVisualiserMode(private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID): ToggleVisualiserModeResult {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Toggle override and persist to storage
        playerState.claimToolMode = 1 - playerState.claimToolMode
        playerStateRepository.update(playerState)
        return ToggleVisualiserModeResult.Success(playerState.claimToolMode)
    }
}