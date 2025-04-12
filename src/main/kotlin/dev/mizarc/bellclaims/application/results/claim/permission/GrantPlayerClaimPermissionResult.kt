package dev.mizarc.bellclaims.application.results.claim.permission

sealed class GrantPlayerClaimPermissionResult {
    object Success : GrantPlayerClaimPermissionResult()
    object ClaimNotFound : GrantPlayerClaimPermissionResult()
    object AlreadyExists : GrantPlayerClaimPermissionResult()
    object StorageError: GrantPlayerClaimPermissionResult()
}