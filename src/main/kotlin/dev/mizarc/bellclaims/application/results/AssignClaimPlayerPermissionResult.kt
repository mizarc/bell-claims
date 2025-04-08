package dev.mizarc.bellclaims.application.results

sealed class AssignClaimPlayerPermissionResult {
    object Success : AssignClaimPlayerPermissionResult()
    object ClaimNotFound : AssignClaimPlayerPermissionResult()
    object AlreadyExists : AssignClaimPlayerPermissionResult()
    object StorageError: AssignClaimPlayerPermissionResult()
}