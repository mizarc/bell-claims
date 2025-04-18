package dev.mizarc.bellclaims.application.results.claim.partition

sealed class CanRemovePartitionResult {
    object Success: CanRemovePartitionResult()
    object WillBeDisconnected: CanRemovePartitionResult()
    object StorageError: CanRemovePartitionResult()
}