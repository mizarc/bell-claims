package dev.mizarc.bellclaims.application.enums

sealed class RemoveFlagFromClaimResult {
    object Success : RemoveFlagFromClaimResult()
    object ClaimNotFound : RemoveFlagFromClaimResult()
    object DoesNotExist : RemoveFlagFromClaimResult()
    object StorageError: RemoveFlagFromClaimResult()
}