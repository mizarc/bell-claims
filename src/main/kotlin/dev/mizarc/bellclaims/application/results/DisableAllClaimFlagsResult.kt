package dev.mizarc.bellclaims.application.results

sealed class DisableAllClaimFlagsResult {
    object Success : DisableAllClaimFlagsResult()
    object ClaimNotFound : DisableAllClaimFlagsResult()
    object AllAlreadyDisabled : DisableAllClaimFlagsResult()
    object StorageError: DisableAllClaimFlagsResult()
}