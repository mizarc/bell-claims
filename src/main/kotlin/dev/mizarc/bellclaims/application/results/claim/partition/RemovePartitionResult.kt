package dev.mizarc.bellclaims.application.results.claim.partition

sealed class RemovePartitionResult {
    object Success : RemovePartitionResult()
    object Disconnected: RemovePartitionResult()
    object ExposedClaimAnchor: RemovePartitionResult()
    object DoesNotExist : RemovePartitionResult()
    object StorageError: RemovePartitionResult()
}