package dev.mizarc.bellclaims.application.enums

sealed class DisableClaimFlagResult {
    object Success : DisableClaimFlagResult()
    object ClaimFlagNotFound : DisableClaimFlagResult()
    object DoesNotExist : DisableClaimFlagResult()
    object StorageError: DisableClaimFlagResult()
}