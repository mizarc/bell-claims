package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.domain.entities.Partition
import java.util.UUID

class GetClaimPartitions(private val partitionRepository: PartitionRepository) {
    fun execute(claimId: UUID): List<Partition> {
        return partitionRepository.getByClaim(claimId).toList().sortedBy { it.getBlockCount() }
    }
}