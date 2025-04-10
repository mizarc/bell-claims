package dev.mizarc.bellclaims.application.results

sealed class GrantClaimWidePermissionResult {
    object Success : GrantClaimWidePermissionResult()
    object ClaimNotFound : GrantClaimWidePermissionResult()
    object AlreadyExists : GrantClaimWidePermissionResult()
    object StorageError: GrantClaimWidePermissionResult()
}