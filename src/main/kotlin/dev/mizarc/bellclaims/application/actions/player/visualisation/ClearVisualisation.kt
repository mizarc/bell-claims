package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID
import kotlin.collections.flatten

class ClearVisualisation(
    private val playerStateRepository: PlayerStateRepository,
    private val visualisationService: VisualisationService
) {
    /**
     * Clears the claim visualisation for the target player
     */
    fun execute(playerId: UUID) {
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Get all the blocks to unvisualise, including partition-based and currently selected
        val claimBlocksToUnvisualise = playerState.visualisedClaims.values.flatten().toMutableSet()
        claimBlocksToUnvisualise.addAll(playerState.visualisedPartitions.values.flatMap { innerMap -> innerMap.values }
            .flatten())

        // Unvisualise
        visualisationService.clear(playerId, claimBlocksToUnvisualise)

        // Nullify visualizations in the player state
        playerState.visualisedClaims.clear()
        playerState.visualisedPartitions.clear()
        playerState.isVisualisingClaims = false
        playerStateRepository.update(playerState)
        return
    }
}