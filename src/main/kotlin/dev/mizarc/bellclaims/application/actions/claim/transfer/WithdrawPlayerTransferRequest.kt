package dev.mizarc.bellclaims.application.actions.claim.transfer

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.transfer.WithdrawPlayerTransferRequestResult
import java.util.UUID

class WithdrawPlayerTransferRequest(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, playerId: UUID): WithdrawPlayerTransferRequestResult {
        val claim = claimRepository.getById(claimId) ?: return WithdrawPlayerTransferRequestResult.ClaimNotFound
        if (playerId in claim.transferRequests.keys) return WithdrawPlayerTransferRequestResult.NoPendingRequest

        claim.transferRequests.remove(playerId)
        return WithdrawPlayerTransferRequestResult.Success
    }
}