package dev.mizarc.bellclaims.application.actions.claim.transfer

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.transfer.AcceptTransferRequestResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import java.util.UUID

class AcceptTransferRequest(private val claimRepository: ClaimRepository,
                            private val playerMetadataService: PlayerMetadataService,
                            private val partitionRepository: PartitionRepository) {
    fun execute(claimId: UUID, playerId: UUID, newName: String): AcceptTransferRequestResult {
        val claim = claimRepository.getById(claimId) ?: return AcceptTransferRequestResult.ClaimNotFound

        // Check if the player already owns the claim
        if (claim.playerId == playerId) return AcceptTransferRequestResult.PlayerOwnsClaim

        // Check for active transfer request
        if (!claim.transferRequests.containsKey(playerId)) return AcceptTransferRequestResult.NoActiveTransferRequest

        // Check if the player has reached their claim limit
        val playerClaimLimit = playerMetadataService.getPlayerClaimLimit(playerId)
        val playerClaimCount = claimRepository.getByPlayer(playerId).count()
        if (playerClaimCount >= playerClaimLimit) return AcceptTransferRequestResult.ClaimLimitExceeded

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
        if (playerBlockCount + claimBlockCount > playerBlockLimit) return AcceptTransferRequestResult.BlockLimitExceeded

        // Check if name already exists in player's list of claims
        if (claimRepository.getByName(claim.playerId, newName) != null)
            return AcceptTransferRequestResult.NameAlreadyExists

        // If all checks pass, the player can accept the transfer request
        claim.playerId = playerId
        claimRepository.update(claim)
        return AcceptTransferRequestResult.Success
    }
}