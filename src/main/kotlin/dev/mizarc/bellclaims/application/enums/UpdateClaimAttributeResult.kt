package dev.mizarc.bellclaims.application.enums

sealed class UpdateClaimAttributeResult {
    object Success : UpdateClaimAttributeResult()
    object ClaimNotFound : UpdateClaimAttributeResult()
    object StorageError : UpdateClaimAttributeResult()
}