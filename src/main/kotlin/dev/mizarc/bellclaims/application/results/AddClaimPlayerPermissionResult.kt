package dev.mizarc.bellclaims.application.results

sealed class AddClaimPlayerPermissionResult {
    object Success : AddClaimPlayerPermissionResult()
    object ClaimNotFound : AddClaimPlayerPermissionResult()
    object AlreadyExists : AddClaimPlayerPermissionResult()
    object StorageError: AddClaimPlayerPermissionResult()
}