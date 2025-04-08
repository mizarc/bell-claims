package dev.mizarc.bellclaims.application.results

sealed class EnableClaimFlagResult {
    object Success : EnableClaimFlagResult()
    object ClaimFlagNotFound : EnableClaimFlagResult()
    object AlreadyExists : EnableClaimFlagResult()
    object StorageError: EnableClaimFlagResult()
}