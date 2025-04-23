package dev.mizarc.bellclaims.application.results.claim.flags

sealed class EnableAllClaimFlagsResult {
    object Success : EnableAllClaimFlagsResult()
    object ClaimNotFound : EnableAllClaimFlagsResult()
    object AllAlreadyEnabled : EnableAllClaimFlagsResult()
    object StorageError: EnableAllClaimFlagsResult()
}