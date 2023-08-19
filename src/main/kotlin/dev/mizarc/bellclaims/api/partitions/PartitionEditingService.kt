package dev.mizarc.bellclaims.api.partitions

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import java.util.UUID

interface PartitionEditingService {
    fun getParititonById(uuid: UUID)
    fun getPartitionsByClaim(claim: Claim)
    fun addPartition(partition: Partition, claim: Claim)
    fun resizePartition(partition: Partition, area: Area)
    fun deletePartition(partition: Partition)
}