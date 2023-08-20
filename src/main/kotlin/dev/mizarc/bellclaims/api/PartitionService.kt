package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.PartitionCreationResult
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.enums.PartitionResizeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import java.util.UUID

interface PartitionService {
    fun getPartitionById(uuid: UUID): Partition
    fun getPartitionsByClaim(claim: Claim): Partition
    fun createPartition(area: Area, claim: Claim): PartitionCreationResult
    fun resizePartition(partition: Partition, area: Area): PartitionResizeResult
    fun destroyPartition(partition: Partition): PartitionDestroyResult
}