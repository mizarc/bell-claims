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
        // Get or create the player state if it doesn't exist
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
                "RED_GLAZED_TERRACOTTA", "RED_CARPET", "BLACK_GLAZED_TERRACOTTA", "BLACK_CARPET")
            playerState.visualisedClaims[claimId] = newPositions
            return
        }

        // For player's own claims, change the refresh type based on player's current visualisation mode
        if (playerState.claimToolMode == 1) {
            val partition = partitionRepository.getById(partitionId)
            if (partition == null) {
                val blocksToClear = playerState.visualisedPartitions.computeIfAbsent(claim.id) { mutableMapOf() }.remove(partitionId) ?: return
                visualisationService.clear(playerId, blocksToClear)
                return
            }

            // Display partitioned
            playerState.visualisedPartitions[claimId]?.get(partitionId)
            val visualisedPositions = playerState.visualisedPartitions[claimId]?.get(partitionId)
            if (visualisedPositions == null) {
                val newPositions =visualisationService.refreshPartitioned(playerId, emptySet(), setOf(partition.area),
                    "LIGHT_GRAY_GLAZED_TERRACOTTA", "LIGHT_GRAY_CARPET",
                    "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_BLUE_CARPET")
                playerState.visualisedPartitions.computeIfAbsent(claim.id) { mutableMapOf() }[partition.id] = newPositions
            } else {
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
                playerState.visualisedPartitions.computeIfAbsent(claim.id) { mutableMapOf() }[partition.id] = newPositions
            }
        }
        else {
            // Display complete
            val visualisedPositions = playerState.visualisedClaims[claimId] ?: return
            val partitions = partitionRepository.getByClaim(claim.id)
            val areas = partitions.map { it.area }.toMutableSet()
            val newPositions = visualisationService.refreshComplete(playerId, visualisedPositions, areas,
                "LIGHT_BLUE_GLAZED_TERRACOTTA", "LIGHT_BLUE_CARPET", "BLUE_GLAZED_TERRACOTTA", "BLUE_CARPET")
            playerState.visualisedClaims[claimId] = newPositions
            return
        }
    }
}