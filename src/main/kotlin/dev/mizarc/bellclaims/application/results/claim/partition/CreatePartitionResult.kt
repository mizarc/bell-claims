package dev.mizarc.bellclaims.application.results.claim.partition

sealed class CreatePartitionResult {
    object Success: CreatePartitionResult()
    object Overlaps: CreatePartitionResult()
    object TooClose: CreatePartitionResult()
    object Disconnected: CreatePartitionResult()
    object TooSmall: CreatePartitionResult()
    object InsufficientBlocks: CreatePartitionResult()
    object StorageError: CreatePartitionResult()
}