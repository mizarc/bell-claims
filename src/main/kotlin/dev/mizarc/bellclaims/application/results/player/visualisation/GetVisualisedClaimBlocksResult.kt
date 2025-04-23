package dev.mizarc.bellclaims.application.results.player.visualisation

import dev.mizarc.bellclaims.domain.values.Position3D

sealed class GetVisualisedClaimBlocksResult {
    data class Success(val blockPositions: Set<Position3D>): GetVisualisedClaimBlocksResult()
    object NotVisualising: GetVisualisedClaimBlocksResult()
    object ClaimNotFound: GetVisualisedClaimBlocksResult()
    object StorageError: GetVisualisedClaimBlocksResult()
}