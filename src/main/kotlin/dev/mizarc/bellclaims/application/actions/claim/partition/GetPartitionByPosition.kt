package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position
import java.util.UUID

class GetPartitionByPosition(private val partitionRepository: PartitionRepository,
                             private val claimRepository: ClaimRepository
) {
    fun execute(position: Position, worldId: UUID): Partition? {
        val partitions = partitionRepository.getByPosition(position)

        for (partition in partitions) {
            val claim = claimRepository.getById(partition.claimId) ?: continue
            if (claim.worldId == worldId) return partition
        }
        return null
    }
}