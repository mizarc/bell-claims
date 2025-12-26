package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult
import dev.mizarc.bellclaims.domain.entities.Claim

sealed class UpdateClaimDescriptionResult {
    data class Success(val claim: Claim) : UpdateClaimDescriptionResult()
    object ClaimNotFound : UpdateClaimDescriptionResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimDescriptionResult()
    object StorageError : UpdateClaimDescriptionResult()
}