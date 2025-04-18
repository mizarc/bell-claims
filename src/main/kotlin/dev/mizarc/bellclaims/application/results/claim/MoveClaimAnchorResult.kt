package dev.mizarc.bellclaims.application.results.claim

sealed class MoveClaimAnchorResult {
    object Success: MoveClaimAnchorResult()
    object NoPermission: MoveClaimAnchorResult()
    object InvalidPosition: MoveClaimAnchorResult()
    object StorageError: MoveClaimAnchorResult()
}