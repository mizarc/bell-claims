package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position
import java.util.UUID

class GetClaimAtPosition(private val claimRepository: ClaimRepository,
                         private val partitionRepository: PartitionRepository) {
    fun execute(worldId: UUID, position: Position): GetClaimAtPositionResult {
        val partitions = partitionRepository.getByPosition(position)
        val worldPartition = filterByWorld(worldId, partitions) ?: return GetClaimAtPositionResult.NoClaimFound
        val claim = claimRepository.getById(worldPartition.claimId) ?: return GetClaimAtPositionResult.NoClaimFound
        return GetClaimAtPositionResult.Success(claim)
    }

    private fun filterByWorld(worldId: UUID, inputPartitions: Set<Partition>): Partition? {
        for (partition in inputPartitions) {
            val claimPartition = claimRepository.getById(partition.claimId) ?: continue
            if (claimPartition.worldId == worldId) {
                return partition
            }
        }
        return null
    }
}