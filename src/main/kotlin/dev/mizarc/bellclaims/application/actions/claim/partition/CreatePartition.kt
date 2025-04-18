package dev.mizarc.bellclaims.application.actions.claim.partition

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.partition.CreatePartitionResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position2D
import java.util.UUID

class CreatePartition(private val claimRepository: ClaimRepository,
                      private val partitionRepository: PartitionRepository,
                      private val playerMetadataService: PlayerMetadataService,
                      private val config: MainConfig) {
    fun execute(claimId: UUID, area: Area): CreatePartitionResult {
        val partition = Partition(claimId, area)

        // Check if selection overlaps an existing claim
        if (isPartitionOverlap(partition)) return CreatePartitionResult.Overlaps

        // Check if selection is too close to another claim's partition
        if (isPartitionTooClose(partition)) return CreatePartitionResult.TooClose

        // Check if claim meets minimum size
        if (area.getXLength() < config.minimumPartitionSize || area.getZLength() < config.minimumPartitionSize) {
            return CreatePartitionResult.TooSmall(config.minimumPartitionSize)
        }

        // Check if the player has reached their claim block limit
        val claim = claimRepository.getById(claimId) ?: return CreatePartitionResult.StorageError
        val playerBlockLimit = playerMetadataService.getPlayerClaimBlockLimit(claim.playerId)
        val playerBlockCount = claimRepository.getByPlayer(claim.playerId).flatMap { playerClaim ->
            partitionRepository.getByClaim(playerClaim.id)
        }.sumOf { partition ->
            partition.getBlockCount()
        }

        // Check if new area takes too much space
        if (playerBlockCount + area.getBlockCount() > playerBlockLimit) {
            val requiredExtraBlocks = (playerBlockCount + area.getBlockCount()) - playerBlockLimit
            return CreatePartitionResult.InsufficientBlocks(requiredExtraBlocks)
        }

        // Append partition to existing claim if adjacent partition is part of the same claim
        val adjacentPartitions = getAdjacent(partition)
        for (adjacentPartition in adjacentPartitions) {
            if (adjacentPartition.claimId == partition.claimId) {
                partitionRepository.add(partition)
                return CreatePartitionResult.Success(claim, partition)
            }
        }

        return CreatePartitionResult.Disconnected
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

    /**
     * Gets all partitions in the target chunk.
     * @param worldId The world the chunk is in.
     * @param position The coordinates of the chunk.
     * @return A set of partitions that exist within the target chunk.
     */
    private fun getByChunk(worldId: UUID, position: Position2D): Set<Partition> {
        return filterByWorld(worldId, partitionRepository.getByChunk(position))
    }

    /**
     * Gets all partitions that are adjacent and connected to the target partition.
     * @param partition The target partition.
     * @return The partitions in the world that are physically touching the target partition.
     */
    private fun getAdjacent(partition: Partition): ArrayList<Partition> {
        // Find all partitions within the chunks of the partition
        val claim = claimRepository.getById(partition.claimId) ?: return ArrayList()
        val partitionChunks = partition.getChunks().toSet()

        // Add chunk as well as surrounding chunks to get partitions on chunk edges
        val chunks = mutableSetOf<Position2D>()
        for (chunk in partitionChunks) {
            chunks.add(chunk)
            chunks.addAll(getSurroundingPositions(chunk, 1))
        }

        // Fetch all partitions
        val chunkPartitions = ArrayList<Partition>()
        for (chunk in chunks) {
            chunkPartitions.addAll(getByChunk(claim.worldId, chunk))
        }

        // Find which of the partitions in the chunks are adjacent
        val adjacentPartitions = ArrayList<Partition>()
        for (chunkPartition in chunkPartitions) {
            if (chunkPartition.isPartitionAdjacent(partition)) {
                adjacentPartitions.add(chunkPartition)
            }
        }
        return adjacentPartitions
    }
}