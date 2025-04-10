package dev.mizarc.bellclaims.application.results

sealed class RevokeClaimWidePermissionResult {
    object Success : RevokeClaimWidePermissionResult()
    object ClaimNotFound : RevokeClaimWidePermissionResult()
    object DoesNotExist : RevokeClaimWidePermissionResult()
    object StorageError: RevokeClaimWidePermissionResult()
}