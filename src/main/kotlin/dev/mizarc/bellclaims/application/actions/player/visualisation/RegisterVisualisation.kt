package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.visualisation.RegisterVisualisationResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class RegisterVisualisation(private val claimRepository: ClaimRepository,
                            private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID, claimId: UUID, blockPositions: Set<Position3D>): RegisterVisualisationResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return RegisterVisualisationResult.ClaimNotFound

        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Set visualised block positions
        playerState.visualisedBlockPositions[claimId] = blockPositions
        playerStateRepository.update(playerState)
        return RegisterVisualisationResult.Success
    }
}