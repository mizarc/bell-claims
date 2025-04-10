package dev.mizarc.bellclaims.application.results

sealed class RevokePlayerClaimPermissionResult {
    object Success : RevokePlayerClaimPermissionResult()
    object ClaimNotFound : RevokePlayerClaimPermissionResult()
    object DoesNotExist : RevokePlayerClaimPermissionResult()
    object StorageError: RevokePlayerClaimPermissionResult()
}