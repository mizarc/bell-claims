package dev.mizarc.bellclaims.application.results

sealed class RemovePartitionResult {
    object Success : RemovePartitionResult()
    object DoesNotExist : RemovePartitionResult()
    object StorageError: RemovePartitionResult()
}