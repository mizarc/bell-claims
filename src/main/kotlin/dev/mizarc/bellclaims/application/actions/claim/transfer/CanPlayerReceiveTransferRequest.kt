package dev.mizarc.bellclaims.application.actions.claim.transfer

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.transfer.CanPlayerReceiveTransferRequestResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import java.util.UUID

class CanPlayerReceiveTransferRequest(private val claimRepository: ClaimRepository,
                                      private val partitionRepository: PartitionRepository,
                                      private val playerMetadataService: PlayerMetadataService) {
    fun execute(claimId: UUID, playerId: UUID): CanPlayerReceiveTransferRequestResult {
        val claim = claimRepository.getById(claimId) ?: return CanPlayerReceiveTransferRequestResult.ClaimNotFound

        // Check if the player already owns the claim
        if (claim.playerId == playerId) {
            return CanPlayerReceiveTransferRequestResult.PlayerOwnsClaim
        }

        // Check if the player has reached their claim limit
        val playerClaimLimit = playerMetadataService.getPlayerClaimLimit(playerId)
        val playerClaimCount = claimRepository.getByPlayer(playerId).count()
        if (playerClaimCount >= playerClaimLimit) {
            return CanPlayerReceiveTransferRequestResult.ClaimLimitExceeded
        }

        // Check if the player has reached their claim block limit
        val playerBlockLimit = playerMetadataService.getPlayerClaimBlockLimit(playerId)
        val playerBlockCount = claimRepository.getByPlayer(playerId).flatMap { playerClaim ->
            partitionRepository.getByClaim(playerClaim.id)
        }.sumOf { partition ->
            partition.getBlockCount()
        }
        val claimBlockCount = partitionRepository.getByClaim(claim.id).sumOf { partition ->
            partition.getBlockCount()
        }
        if (playerBlockCount + claimBlockCount > playerBlockLimit) {
            return CanPlayerReceiveTransferRequestResult.BlockLimitExceeded
        }

        // If all checks pass, the player can receive the transfer request
        return CanPlayerReceiveTransferRequestResult.Success
    }
}