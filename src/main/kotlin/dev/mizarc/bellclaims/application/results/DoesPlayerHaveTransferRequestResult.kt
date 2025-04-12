package dev.mizarc.bellclaims.application.results

sealed class DoesPlayerHaveTransferRequestResult {
    data class Success(val hasRequest: Boolean): DoesPlayerHaveTransferRequestResult()
    object ClaimNotFound: DoesPlayerHaveTransferRequestResult()
    object StorageError: DoesPlayerHaveTransferRequestResult()
}