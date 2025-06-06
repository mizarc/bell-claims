package dev.mizarc.bellclaims.application.results.claim

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class GetClaimAtPositionResult {
    data class Success(val claim: Claim) : GetClaimAtPositionResult()
    object NoClaimFound: GetClaimAtPositionResult()
    object StorageError: GetClaimAtPositionResult()
}