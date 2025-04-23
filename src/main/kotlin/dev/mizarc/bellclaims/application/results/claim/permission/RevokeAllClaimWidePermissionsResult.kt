package dev.mizarc.bellclaims.application.results.claim.permission

sealed class RevokeAllClaimWidePermissionsResult {
    object Success : RevokeAllClaimWidePermissionsResult()
    object ClaimNotFound: RevokeAllClaimWidePermissionsResult()
    object AllAlreadyRevoked : RevokeAllClaimWidePermissionsResult()
    object StorageError: RevokeAllClaimWidePermissionsResult()
}