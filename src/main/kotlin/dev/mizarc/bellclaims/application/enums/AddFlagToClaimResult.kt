package dev.mizarc.bellclaims.application.enums

sealed class AddFlagToClaimResult {
    object Success : AddFlagToClaimResult()
    object ClaimNotFound : AddFlagToClaimResult()
    object AlreadyExists : AddFlagToClaimResult()
    object StorageError: AddFlagToClaimResult()
}