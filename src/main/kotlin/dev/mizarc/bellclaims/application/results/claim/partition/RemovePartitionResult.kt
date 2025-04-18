package dev.mizarc.bellclaims.application.results.claim.partition

sealed class RemovePartitionResult {
    object Success : RemovePartitionResult()
    object WillBeDisconnected: RemovePartitionResult()
    object DoesNotExist : RemovePartitionResult()
    object StorageError: RemovePartitionResult()
}