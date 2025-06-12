package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.domain.entities.PlayerState
import java.util.UUID

class ClearVisualisation(private val playerStateRepository: PlayerStateRepository,
                         private val visualisationService: VisualisationService) {
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
        val blocksToUnvisualise = playerState.visualisedClaims.values.flatten().toMutableSet()

        // Unvisualise
        visualisationService.clear(playerId, blocksToUnvisualise)

        // Nullify visualizations in the player state
        playerState.visualisedClaims.clear()
        playerState.isVisualisingClaims = false
        playerStateRepository.update(playerState)
        return
    }
}