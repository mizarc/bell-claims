package dev.mizarc.bellclaims.application.results

sealed class UpdateClaimNameResult {
    object Success : UpdateClaimNameResult()
    object ClaimNotFound : UpdateClaimNameResult()
    object NameAlreadyExists: UpdateClaimNameResult()
    data class InputTextInvalid(val errors: List<TextValidationErrorResult>) : UpdateClaimNameResult()
    object StorageError : UpdateClaimNameResult()
}