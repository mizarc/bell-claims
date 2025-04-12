package dev.mizarc.bellclaims.application.results.claim.permission

sealed class GrantAllClaimPermissionsResult {
    object Success : GrantAllClaimPermissionsResult()
    object ClaimNotFound : GrantAllClaimPermissionsResult()
    object AllAlreadyGranted : GrantAllClaimPermissionsResult()
    object StorageError: GrantAllClaimPermissionsResult()
}