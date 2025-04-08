package dev.mizarc.bellclaims.application.results

sealed class UpdateClaimAttributeResult {
    object Success : UpdateClaimAttributeResult()
    object ClaimNotFound : UpdateClaimAttributeResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimAttributeResult()
    object StorageError : UpdateClaimAttributeResult()
}