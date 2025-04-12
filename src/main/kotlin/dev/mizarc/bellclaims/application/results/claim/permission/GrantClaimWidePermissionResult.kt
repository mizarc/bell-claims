package dev.mizarc.bellclaims.application.results.claim.permission

sealed class GrantClaimWidePermissionResult {
    object Success : GrantClaimWidePermissionResult()
    object ClaimNotFound : GrantClaimWidePermissionResult()
    object AlreadyExists : GrantClaimWidePermissionResult()
    object StorageError: GrantClaimWidePermissionResult()
}