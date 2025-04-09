package dev.mizarc.bellclaims.application.results

sealed class EnableClaimFlagResult {
    object Success : EnableClaimFlagResult()
    object ClaimNotFound : EnableClaimFlagResult()
    object AlreadyExists : EnableClaimFlagResult()
    object StorageError: EnableClaimFlagResult()
}