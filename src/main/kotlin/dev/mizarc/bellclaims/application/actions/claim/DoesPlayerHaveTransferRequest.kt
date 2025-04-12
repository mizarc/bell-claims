package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.DoesPlayerHaveTransferRequestResult
import java.util.UUID

class DoesPlayerHaveTransferRequest(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, playerId: UUID): DoesPlayerHaveTransferRequestResult {
        val claim = claimRepository.getById(claimId) ?: return DoesPlayerHaveTransferRequestResult.ClaimNotFound
        if (playerId in claim.transferRequests.keys) return DoesPlayerHaveTransferRequestResult.Success(true)
        return DoesPlayerHaveTransferRequestResult.Success(false)
    }
}