package dev.mizarc.bellclaims.application.results

sealed class RevokePlayerClaimPermissionResult {
    object Success : RevokePlayerClaimPermissionResult()
    object ClaimNotFound : RevokePlayerClaimPermissionResult()
    object AlreadyExists : RevokePlayerClaimPermissionResult()
    object StorageError: RevokePlayerClaimPermissionResult()
}