package dev.mizarc.bellclaims.application.results.claim.transfer

sealed class WithdrawPlayerTransferRequestResult {
    object Success: WithdrawPlayerTransferRequestResult()
    object ClaimNotFound: WithdrawPlayerTransferRequestResult()
    object NoPendingRequest: WithdrawPlayerTransferRequestResult()
    object StorageError: WithdrawPlayerTransferRequestResult()
}