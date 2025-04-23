package dev.mizarc.bellclaims.application.enums

sealed class DisableClaimFlagResult {
    object Success : DisableClaimFlagResult()
    object ClaimNotFound : DisableClaimFlagResult()
    object DoesNotExist : DisableClaimFlagResult()
    object StorageError: DisableClaimFlagResult()
}