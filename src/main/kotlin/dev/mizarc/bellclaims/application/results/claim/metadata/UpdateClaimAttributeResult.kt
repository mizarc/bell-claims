package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult

sealed class UpdateClaimAttributeResult {
    object Success : UpdateClaimAttributeResult()
    object ClaimNotFound : UpdateClaimAttributeResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimAttributeResult()
    object StorageError : UpdateClaimAttributeResult()
}