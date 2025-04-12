package dev.mizarc.bellclaims.application.results.claim.metadata

import dev.mizarc.bellclaims.application.results.common.TextValidationErrorResult

sealed class UpdateClaimNameResult {
    object Success : UpdateClaimNameResult()
    object ClaimNotFound : UpdateClaimNameResult()
    object NameAlreadyExists: UpdateClaimNameResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimNameResult()
    object StorageError : UpdateClaimNameResult()
}