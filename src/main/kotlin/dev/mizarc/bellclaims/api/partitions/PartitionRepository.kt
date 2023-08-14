package dev.mizarc.bellclaims.api.partitions

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position
import java.util.*

interface PartitionRepository {
    fun getAll(): Set<Partition>
    fun getById(id: UUID): Partition?
    fun getByClaim(claim: Claim): Set<Partition>
    fun getByPosition(position: Position): Set<Partition>
    fun getByChunk(position: Position): Set<Partition>
    fun add(partition: Partition)
    fun update(partition: Partition)
    fun remove(partition: Partition)
    fun removeByClaim(claim: Claim)
}