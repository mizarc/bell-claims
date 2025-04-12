package dev.mizarc.bellclaims.application.results

sealed class UpdateClaimIconResult {
    object Success: UpdateClaimIconResult()
    object NoClaimFound: UpdateClaimIconResult()
    object StorageError: UpdateClaimIconResult()
}