package dev.mizarc.bellclaims.application.actions.player.visualisation

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.domain.entities.PlayerState
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class RefreshVisualisation(private val playerStateRepository: PlayerStateRepository,
                           private val claimRepository: ClaimRepository,
                           private val partitionRepository: PartitionRepository,
                           private val visualisationService: VisualisationService) {
    fun execute(playerId: UUID, claimId: UUID, partitionId: UUID) {
        // Get or create player state if it doesn't exist
        var playerState = playerStateRepository.get(playerId)
        if (playerState == null) {
            playerState = PlayerState(playerId)
            playerStateRepository.add(playerState)
        }

        // Always display complete for claims the player doesn't own
        val claim = claimRepository.getById(claimId) ?: return
        if (claim.playerId != playerId) {
            val visualisedPositions = playerState.visualisedClaims[claimId] ?: return
            val partitions = partitionRepository.getByClaim(claim.id)
            val areas = partitions.map { it.area }.toMutableSet()
            val newPositions = visualisationService.refreshComplete(playerId, visualisedPositions, areas,
                "RED_GLAZED_TERRACOTTA", "LIGHT_GRAY_CARPET")
            playerState.visualisedClaims[claimId] = newPositions
            return
        }

        // For player's own claims, change refresh type based on player's current visualisation mode
        val borders: MutableMap<UUID, Set<Position3D>> = mutableMapOf()
        if (playerState.claimToolMode == 1) {
            // Display partitioned
            val visualisedPositions = playerState.visualisedPartitions[claimId]?.get(partitionId) ?: return
            val partition = partitionRepository.getById(claim.id) ?: return
            val newPositions = if (partition.area.isPositionInArea(claim.position)) {
                // Main partition
                visualisationService.refreshPartitioned(playerId, visualisedPositions, setOf(partition.area),
                    "CYAN_GLAZED_TERRACOTTA", "CYAN_CARPET",
                    "BLUE_GLAZED_TERRACOTTA", "BLUE_CARPET")
            } else {
                // Attached partitions
                visualisationService.refreshPartitioned(playerId, visualisedPositions, setOf(partition.area),
                    "LIGHT_GRAY_GLAZED_TERRACOTTA", "LIGHT_GRAY_CARPET",
                    "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_BLUE_CARPET")
            }
            playerState.visualisedPartitions[claimId]?.set(partitionId, newPositions)
        }
        else {
            // Display complete
            val visualisedPositions = playerState.visualisedClaims[claimId] ?: return
            val partitions = partitionRepository.getByClaim(claim.id)
            val areas = partitions.map { it.area }.toMutableSet()
            val newPositions = visualisationService.refreshComplete(playerId, visualisedPositions, areas,
                "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_GRAY_CARPET")
            playerState.visualisedClaims[claimId] = newPositions
            return
        }
    }
}