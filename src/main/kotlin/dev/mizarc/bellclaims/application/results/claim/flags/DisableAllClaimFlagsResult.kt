package dev.mizarc.bellclaims.application.results.claim.flags

sealed class DisableAllClaimFlagsResult {
    object Success : DisableAllClaimFlagsResult()
    object ClaimNotFound : DisableAllClaimFlagsResult()
    object AllAlreadyDisabled : DisableAllClaimFlagsResult()
    object StorageError: DisableAllClaimFlagsResult()
}