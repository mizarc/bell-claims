package dev.mizarc.bellclaims.application.results.claim.partition

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition

sealed class CreatePartitionResult {
    data class Success(val claim: Claim, val partition: Partition): CreatePartitionResult()
    data class InsufficientBlocks(val requiredExtraBlocks: Int): CreatePartitionResult()
    data class TooSmall(val minimumSize: Int): CreatePartitionResult()
    object Overlaps: CreatePartitionResult()
    object TooClose: CreatePartitionResult()
    object Disconnected: CreatePartitionResult()
    object StorageError: CreatePartitionResult()
}