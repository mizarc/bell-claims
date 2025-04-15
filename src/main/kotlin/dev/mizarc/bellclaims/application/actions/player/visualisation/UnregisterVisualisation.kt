package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.visualisation.UnregisterVisualisationResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class UnregisterVisualisation(private val claimRepository: ClaimRepository,
                            private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID, claimId: UUID): UnregisterVisualisationResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return UnregisterVisualisationResult.ClaimNotFound

        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Clear visualised block positions
        playerState.visualisedBlockPositions.remove(claimId)
        playerStateRepository.update(playerState)
        return UnregisterVisualisationResult.Success
    }
}