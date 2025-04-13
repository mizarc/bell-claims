package dev.mizarc.bellclaims.application.results.claim.permission

sealed class GrantAllClaimWidePermissionsResult {
    object Success : GrantAllClaimWidePermissionsResult()
    object ClaimWideNotFound : GrantAllClaimWidePermissionsResult()
    object AllAlreadyGrantedWide : GrantAllClaimWidePermissionsResult()
    object StorageError: GrantAllClaimWidePermissionsResult()
}