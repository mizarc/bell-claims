package dev.mizarc.bellclaims.application.results.claim.partition

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition

sealed class CreatePartitionResult {
    data class Success(val claim: Claim, val partition: Partition): CreatePartitionResult()
    object Overlaps: CreatePartitionResult()
    object TooClose: CreatePartitionResult()
    object Disconnected: CreatePartitionResult()
    object TooSmall: CreatePartitionResult()
    object InsufficientBlocks: CreatePartitionResult()
    object StorageError: CreatePartitionResult()
}