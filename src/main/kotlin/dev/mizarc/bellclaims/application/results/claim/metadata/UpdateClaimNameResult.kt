package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult
import dev.mizarc.bellclaims.domain.entities.Claim

sealed class UpdateClaimNameResult {
    data class Success(val claim: Claim) : UpdateClaimNameResult()
    object ClaimNotFound : UpdateClaimNameResult()
    object NameAlreadyExists: UpdateClaimNameResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimNameResult()
    object StorageError : UpdateClaimNameResult()
}