package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult
import dev.mizarc.bellclaims.domain.entities.Claim

sealed class UpdateClaimAttributeResult {
    data class Success(val claim: Claim) : UpdateClaimAttributeResult()
    object ClaimNotFound : UpdateClaimAttributeResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimAttributeResult()
    object StorageError : UpdateClaimAttributeResult()
}