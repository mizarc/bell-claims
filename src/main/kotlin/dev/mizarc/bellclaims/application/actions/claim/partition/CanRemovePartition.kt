package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.partition.CanRemovePartitionResult
import dev.mizarc.bellclaims.domain.entities.Partition
import java.util.UUID

class CanRemovePartition(private val claimRepository: ClaimRepository,
                         private val partitionRepository: PartitionRepository) {
    fun execute(partitionId: UUID): CanRemovePartitionResult {
        val partition = partitionRepository.getById(partitionId) ?: return CanRemovePartitionResult.StorageError
        if (isRemoveResultInAnyDisconnected(partition)) return CanRemovePartitionResult.WillBeDisconnected
        partitionRepository.remove(partition.id)
        return CanRemovePartitionResult.Success
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