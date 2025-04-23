package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.domain.entities.Claim

sealed class UpdateClaimIconResult {
    data class Success(val claim: Claim): UpdateClaimIconResult()
    object NoClaimFound: UpdateClaimIconResult()
    object StorageError: UpdateClaimIconResult()
}