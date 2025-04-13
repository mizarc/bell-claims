package dev.mizarc.bellclaims.application.results.claim

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class GetClaimAnchorAtPositionResult {
    data class Success(val claim: Claim) : GetClaimAnchorAtPositionResult()
    object NoClaimAnchorFound: GetClaimAnchorAtPositionResult()
    object StorageError: GetClaimAnchorAtPositionResult()
}