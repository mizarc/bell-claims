package dev.mizarc.bellclaims.application.results.claim.flags

sealed class EnableClaimFlagResult {
    object Success : EnableClaimFlagResult()
    object ClaimNotFound : EnableClaimFlagResult()
    object AlreadyExists : EnableClaimFlagResult()
    object StorageError: EnableClaimFlagResult()
    object Blacklisted: EnableClaimFlagResult()
}