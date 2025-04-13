package dev.mizarc.bellclaims.application.results.claim.permission

sealed class RevokeAllPlayerClaimPermissionsResult {
    object Success : RevokeAllPlayerClaimPermissionsResult()
    object ClaimNotFound : RevokeAllPlayerClaimPermissionsResult()
    object AllAlreadyRevoked : RevokeAllPlayerClaimPermissionsResult()
    object StorageError: RevokeAllPlayerClaimPermissionsResult()
}