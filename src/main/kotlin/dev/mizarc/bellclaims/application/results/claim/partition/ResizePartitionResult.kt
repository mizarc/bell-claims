package dev.mizarc.bellclaims.application.results.claim.partition

sealed class ResizePartitionResult {
    object Success: ResizePartitionResult()
    object Overlaps: ResizePartitionResult()
    object TooClose: ResizePartitionResult()
    object Disconnected: ResizePartitionResult()
    object ExposedClaimAnchor: ResizePartitionResult()
    object TooSmall: ResizePartitionResult()
    object InsufficientBlocks: ResizePartitionResult()
    object StorageError: ResizePartitionResult()
}