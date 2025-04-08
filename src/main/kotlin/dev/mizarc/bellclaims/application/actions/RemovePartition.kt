package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.RemovePartitionResult
import java.util.UUID

/**
 * Action for removing a specific partition from a claim.
 *
 * @property partitionRepository Repository for managing partitions.
 */
class RemovePartition(private val partitionRepository: PartitionRepository) {

    /**
     * Removes the specified partition using its given [partitionId].
     *
     * @param partitionId The [UUID] of the claim to which the flag should be added.
     * @return An [RemovePartitionResult] indicating the outcome of the flag addition operation.
     */
    fun execute(partitionId: UUID): RemovePartitionResult {
        // Add the flag to the claim
        try {
            return when (partitionRepository.remove(partitionId)) {
                true -> RemovePartitionResult.Success
                false -> RemovePartitionResult.DoesNotExist
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return RemovePartitionResult.StorageError
        }
    }
}