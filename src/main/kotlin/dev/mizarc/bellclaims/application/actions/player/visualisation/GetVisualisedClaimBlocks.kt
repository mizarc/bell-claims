package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.results.player.visualisation.GetVisualisedClaimBlocksResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class GetVisualisedClaimBlocks(private val claimRepository: ClaimRepository,
                               private val playerStateRepository: PlayerStateRepository) {
    fun execute(playerId: UUID, claimId: UUID): GetVisualisedClaimBlocksResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GetVisualisedClaimBlocksResult.ClaimNotFound

        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Toggle override and persist to storage
        val blockPositions = playerState.visualisedBlockPositions[claimId]
        if (blockPositions != null) {
            return GetVisualisedClaimBlocksResult.Success(blockPositions.toSet())
        }
        return GetVisualisedClaimBlocksResult.NotVisualising
    }
}