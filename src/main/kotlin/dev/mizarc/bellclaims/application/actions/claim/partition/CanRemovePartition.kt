package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.partition.CanRemovePartitionResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Position2D
import java.util.UUID

class CanRemovePartition(private val claimRepository: ClaimRepository,
                         private val partitionRepository: PartitionRepository) {
    fun execute(partitionId: UUID): CanRemovePartitionResult {
        val partition = partitionRepository.getById(partitionId) ?: return CanRemovePartitionResult.StorageError

        // Check if removal would result in partitions being disconnected to the claim anchor
        if (isRemoveResultInAnyDisconnected(partition)) return CanRemovePartitionResult.Disconnected

        // Check if claim bell would be outside partition
        val claim = claimRepository.getById(partition.claimId) ?: return CanRemovePartitionResult.StorageError
        if (partitionId == getPrimaryPartition(claim).id) return CanRemovePartitionResult.ExposedClaimAnchor

        partitionRepository.remove(partition.id)
        return CanRemovePartitionResult.Success
    }

    private fun getPrimaryPartition(claim: Claim): Partition {
        val claimPartitions = partitionRepository.getByClaim(claim.id)
        return partitionRepository.getByPosition(Position2D(claim.position)).intersect(claimPartitions.toSet()).first()
    }

    private fun isRemoveResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimRepository.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepository.getByClaim(claim.id).toMutableSet()
        val mainPartition = partitionRepository.getByPosition(claim.position)
            .intersect(claimPartitions.toSet()).first()
        claimPartitions.remove(partition)
        for (claimPartition in claimPartitions) {
            if (claimPartition.id == mainPartition.id) {
                continue
            }
            if (isPartitionDisconnected(claimPartition, claimPartitions)) {
                return true
            }
        }
        return false
    }

    private fun isPartitionDisconnected(partition: Partition, testPartitions: Set<Partition>): Boolean {
        val claim = claimRepository.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepository.getByClaim(claim.id)
        val mainPartition = partitionRepository.getByPosition(claim.position)
            .intersect(claimPartitions.toSet()).first()

        val traversedPartitions = ArrayList<Partition>()
        val partitionQueries = ArrayList<Partition>()
        partitionQueries.add(partition)
        while(partitionQueries.isNotEmpty()) {
            val partitionsToAdd = ArrayList<Partition>() // Partitions yet to query
            val partitionsToRemove = ArrayList<Partition>() // Partitions already queried
            for (partitionQuery in partitionQueries) {
                val adjacentPartitions = getLinked(partitionQuery, testPartitions)
                for (adjacentPartition in adjacentPartitions) {
                    if (adjacentPartition.id == mainPartition.id) {
                        return false
                    }
                    if (adjacentPartition in traversedPartitions) {
                        continue
                    }
                    partitionsToAdd.add(adjacentPartition)
                }
                partitionsToRemove.add(partitionQuery)
                traversedPartitions.add(partitionQuery)
            }
            partitionQueries.removeAll(partitionsToRemove.toSet())
            partitionQueries.addAll(partitionsToAdd)
            partitionsToAdd.clear()
        }
        return true
    }

    private fun getLinked(partition: Partition, testPartitions: Set<Partition>): Set<Partition> {
        return testPartitions.filter { it.isPartitionLinked(partition) && it.claimId == partition.claimId }.toSet()
    }
}