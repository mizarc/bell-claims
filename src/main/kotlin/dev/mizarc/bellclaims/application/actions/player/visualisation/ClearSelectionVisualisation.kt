package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.domain.entities.PlayerState
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class ClearSelectionVisualisation(
    private val playerStateRepository: PlayerStateRepository,
    private val visualisationService: VisualisationService
) {
    fun execute(playerId: UUID, position: Position3D) {
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Visualise the block if currently visualised
        val selectedBlock = playerState.selectedBlock
        if (selectedBlock == null) return
        visualisationService.clear(playerId, setOf(selectedBlock))

        // Set visualization in the player state
        playerState.selectedBlock = position
        playerStateRepository.update(playerState)
    }
}