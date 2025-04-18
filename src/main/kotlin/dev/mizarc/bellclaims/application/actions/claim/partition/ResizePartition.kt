package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.partition.ResizePartitionResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position2D
import java.util.UUID

class ResizePartition(private val claimRepository: ClaimRepository,
                      private val partitionRepository: PartitionRepository,
                      private val playerMetadataService: PlayerMetadataService,
                      private val config: MainConfig) {
    fun execute(partitionId: UUID, newArea: Area): ResizePartitionResult {
        val partition = partitionRepository.getById(partitionId) ?: return ResizePartitionResult.StorageError
        val newPartition = partition.copy()
        newPartition.area = newArea

        // Check if selection overlaps an existing claim
        if (isPartitionOverlap(newPartition)) return ResizePartitionResult.Overlaps

        // Check if selection is too close to another claim's partition
        if (isPartitionTooClose(newPartition)) return ResizePartitionResult.TooClose

        // Check if selection would result in it being disconnected from the claim
        val claim = claimRepository.getById(newPartition.claimId) ?: return ResizePartitionResult.Disconnected
        if (isResizeResultInAnyDisconnected(newPartition)) return ResizePartitionResult.Disconnected

        // Check if claim bell would be outside partition
        if (newPartition.id == getPrimaryPartition(claim).id && !newPartition.area.isPositionInArea(claim.position))
            return ResizePartitionResult.ExposedClaimAnchor

        // Check if claim meets minimum size
        if (newPartition.area.getXLength() < config.minimumPartitionSize ||
            newPartition.area.getZLength() < config.minimumPartitionSize)
            return ResizePartitionResult.TooSmall(config.minimumPartitionSize)

        // Check if the player has reached their claim block limit
        val playerBlockLimit = playerMetadataService.getPlayerClaimBlockLimit(claim.playerId)
        val playerBlockCount = claimRepository.getByPlayer(claim.playerId).flatMap { playerClaim ->
            partitionRepository.getByClaim(playerClaim.id)
        }.sumOf { partition ->
            partition.getBlockCount()
        }

        // Check if claim takes too much space
        if (playerBlockCount - partition.area.getBlockCount() + newPartition.area.getBlockCount() > playerBlockLimit) {
            val requiredExtraBlocks = (playerBlockCount + newArea.getBlockCount()) - playerBlockLimit
            return ResizePartitionResult.InsufficientBlocks(requiredExtraBlocks)
        }

        // Check if claim resize would result a partition being disconnected from the main
        if (isResizeResultInAnyDisconnected(newPartition)) return ResizePartitionResult.Disconnected

        // Successful resizing
        partitionRepository.update(newPartition)
        val blocksRemaining = playerBlockLimit - playerBlockCount - newArea.getBlockCount()
        return ResizePartitionResult.Success(claim, partition, blocksRemaining)
    }

    /**
     * Gets the partition that the claim bell is located in.
     * @param claim The target claim.
     * @return The partition that the target claim's claim bell is physically located in.
     */
    private fun getPrimaryPartition(claim: Claim): Partition {
        val claimPartitions = partitionRepository.getByClaim(claim.id)
        return partitionRepository.getByPosition(Position2D(claim.position)).intersect(claimPartitions.toSet()).first()
    }

    private fun isPartitionTooClose(partition: Partition): Boolean {
        val claim = claimRepository.getById(partition.claimId)?: return true
        val chunks = partition.area.getChunks().flatMap { getSurroundingPositions(it, 1) }
        val partitions = chunks.flatMap { filterByWorld(claim.worldId, getByChunk(claim.worldId, it)) }.toMutableList()
        partitions.removeAll { it.claimId == partition.claimId }
        val areaWithBoundary = Area(
            Position2D( partition.area.lowerPosition2D.x - config.distanceBetweenClaims,
                partition.area.lowerPosition2D.z - config.distanceBetweenClaims),
            Position2D( partition.area.upperPosition2D.x + config.distanceBetweenClaims,
                partition.area.upperPosition2D.z + config.distanceBetweenClaims))
        return partitions.any { it.isAreaOverlap(areaWithBoundary) }
    }

    private fun getSurroundingPositions(position: Position2D, radius: Int): List<Position2D> {
        val positions = mutableListOf<Position2D>()

        for (i in -1 * radius..1 * radius) {
            for (j in -1 * radius..1 * radius) {
                positions.add(Position2D(position.x + i, position.z + j))
            }
        }
        return positions
    }

    private fun isResizeResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimRepository.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepository.getByClaim(claim.id).toMutableSet()
        val mainPartition = partitionRepository.getByPosition(Position2D(claim.position))
            .intersect(claimPartitions.toSet()).first()
        claimPartitions.removeAll { it.id == partition.id }
        claimPartitions.add(partition)
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
        val mainPartition = partitionRepository.getByPosition(Position2D(claim.position))
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

    /**
     * Checks if a partition being put into the world would overlap any existing partition.
     * @param partition The new partition.
     * @return True if the partition area would result in an overlap.
     */
    private fun isPartitionOverlap(partition: Partition): Boolean {
        val claim = claimRepository.getById(partition.claimId) ?: return true
        val chunks = partition.area.getChunks()
        val existingPartitions: MutableSet<Partition> = mutableSetOf()
        for (chunk in chunks) {
            existingPartitions.addAll(filterByWorld(claim.worldId, getByChunk(claim.worldId, chunk)))
        }

        existingPartitions.removeAll { it.id == partition.id }
        for (existingPartition in existingPartitions) {
            if (existingPartition.isAreaOverlap(partition.area)) {
                return true
            }
        }

        return false
    }

    private fun getByChunk(worldId: UUID, chunkPosition: Position2D): Set<Partition> {
        return filterByWorld(worldId, partitionRepository.getByChunk(chunkPosition))
    }

    private fun filterByWorld(worldId: UUID, inputPartitions: Set<Partition>): Set<Partition> {
        val outputPartitions = mutableSetOf<Partition>()
        for (partition in inputPartitions) {
            val claimPartition = claimRepository.getById(partition.claimId) ?: continue
            if (claimPartition.worldId == worldId) {
                outputPartitions.add(partition)
            }
        }
        return outputPartitions
    }
}