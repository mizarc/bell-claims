package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.api.enums.PartitionCreationResult
import dev.mizarc.bellclaims.api.enums.PartitionDestroyResult
import dev.mizarc.bellclaims.api.enums.PartitionResizeResult
import org.bukkit.Location
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.*
import dev.mizarc.bellclaims.infrastructure.persistence.Config
import java.util.*
import kotlin.collections.ArrayList

/**
 * A service class that allows for the querying and modification of partitions in the world.
 */
class PartitionServiceImpl(private val config: Config,
                           private val partitionRepo: PartitionRepository,
                           private val claimService: ClaimService,
                           private val playerStateService: PlayerStateService) : PartitionService {
    override fun isAreaValid(area: Area, claim: Claim): Boolean {
        val chunks = area.getChunks().flatMap { getSurroundingPositions(it, 1) }
        val partitions = chunks.flatMap { filterByWorld(claim.worldId, getByChunk(claim.worldId, it)) }.toMutableList()
        val claimPartitions = partitions.filter { it.claimId == claim.id }
        partitions.removeAll(claimPartitions)
        val areaWithBoundary = Area(
            Position2D( area.lowerPosition2D.x - config.distanceBetweenClaims,
                area.lowerPosition2D.z - config.distanceBetweenClaims),
            Position2D( area.upperPosition2D.x + config.distanceBetweenClaims,
                area.upperPosition2D.z + config.distanceBetweenClaims))
        return partitions.any { it.isAreaOverlap(areaWithBoundary) } || claimPartitions.any { it.isAreaOverlap(area) }
    }

    override fun getPartitionById(uuid: UUID): Partition? {
        return partitionRepo.getById(uuid)
    }

    override fun getPartitionByLocation(location: Location): Partition? {
        val partitionsInPosition = partitionRepo.getByPosition(Position2D(location))
        return filterByWorld(location.world.uid, partitionsInPosition).firstOrNull()
    }

    override fun getPartitionsByClaim(claim: Claim): Set<Partition> {
        return partitionRepo.getByClaim(claim)
    }

    override fun appendPartition(area: Area, claim: Claim): PartitionCreationResult {
        val partition = Partition(claim.id, area)

        // Check if selection overlaps an existing claim
        if (isPartitionOverlap(partition)) return PartitionCreationResult.OVERLAP

        // Check if selection is too close to another claim's partition
        if (isPartitionTooClose(partition)) return PartitionCreationResult.TOO_CLOSE

        // Check if claim meets minimum size
        if (area.getXLength() < 5 || area.getZLength() < 5) return PartitionCreationResult.TOO_SMALL

        // Check if selection is greater than the player's remaining claim blocks
        val remainingClaimBlockCount = playerStateService.getRemainingClaimBlockCount(claim.owner)
        if (area.getBlockCount() > remainingClaimBlockCount) return PartitionCreationResult.INSUFFICIENT_BLOCKS

        // Append partition to existing claim if adjacent partition is part of the same claim
        val adjacentPartitions = getAdjacent(partition)
        for (adjacentPartition in adjacentPartitions) {
            if (adjacentPartition.claimId == partition.claimId) {
                partitionRepo.add(partition)
                return PartitionCreationResult.SUCCESS
            }
        }
        return PartitionCreationResult.NOT_CONNECTED
    }

    override fun resizePartition(partition: Partition, area: Area): PartitionResizeResult {
        val newPartition = partition.copy()
        newPartition.area = area

        // Check if selection would result in it being disconnected from the claim
        val claim = claimService.getById(partition.claimId) ?: return PartitionResizeResult.DISCONNECTED
        if (isResizeResultInAnyDisconnected(newPartition)) return PartitionResizeResult.DISCONNECTED

        // Check if selection overlaps an existing claim
        if (isPartitionOverlap(partition)) return PartitionResizeResult.OVERLAP

        // Check if selection is too close to another claim's partition
        if (isPartitionTooClose(partition)) return PartitionResizeResult.TOO_CLOSE

        // Check if claim bell would be outside partition
        if (partition.id == getPrimaryPartition(claim).id && !partition.area.isPositionInArea(claim.position))
            return PartitionResizeResult.EXPOSED_CLAIM_HUB

        // Check if claim meets minimum size
        if (partition.area.getXLength() < 5 || partition.area.getZLength() < 5) return PartitionResizeResult.TOO_SMALL

        // Check if claim takes too much space
        if (playerStateService.getUsedClaimBlockCount(claim.owner) - partition.area.getBlockCount()
                + newPartition.area.getBlockCount() > playerStateService.getRemainingClaimBlockCount(claim.owner))
            return PartitionResizeResult.INSUFFICIENT_BLOCKS

        // Check if claim resize would result a partition being disconnected from the main
        if (isResizeResultInAnyDisconnected(newPartition)) return PartitionResizeResult.DISCONNECTED

        // Successful resizing
        partitionRepo.update(partition)
        return PartitionResizeResult.SUCCESS
    }

    override fun deletePartition(partition: Partition): PartitionDestroyResult {
        if (isRemoveResultInAnyDisconnected(partition)) return PartitionDestroyResult.DISCONNECTED
        partitionRepo.remove(partition)
        return PartitionDestroyResult.SUCCESS
    }

    private fun filterByWorld(worldId: UUID, inputPartitions: Set<Partition>): Set<Partition> {
        val outputPartitions = mutableSetOf<Partition>()
        for (partition in inputPartitions) {
            val claimPartition = claimService.getById(partition.claimId) ?: continue
            if (claimPartition.worldId == worldId) {
                outputPartitions.add(partition)
            }
        }
        return outputPartitions
    }

    /**
     * Checks if a partition being put into the world would overlap any existing partition.
     * @param partition The new partition.
     * @return True if the partition area would result in an overlap.
     */
    private fun isPartitionOverlap(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return true
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

    private fun isPartitionTooClose(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId)?: return true
        val chunks = partition.area.getChunks().flatMap { getSurroundingPositions(it, 1) }
        val partitions = chunks.flatMap { filterByWorld(claim.worldId, getByChunk(claim.worldId, it)) }.toMutableList()
        val areaWithBoundary = Area(
            Position2D( partition.area.lowerPosition2D.x - config.distanceBetweenClaims,
                partition.area.lowerPosition2D.z - config.distanceBetweenClaims),
            Position2D( partition.area.upperPosition2D.x + config.distanceBetweenClaims,
                partition.area.upperPosition2D.z + config.distanceBetweenClaims))
        return partitions.any { it.isAreaOverlap(areaWithBoundary) }
    }

    private fun isResizeResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim).toMutableSet()
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
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

    private fun isRemoveResultInAnyDisconnected(partition: Partition): Boolean {
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim).toMutableSet()
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
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
        val claim = claimService.getById(partition.claimId) ?: return false
        val claimPartitions = partitionRepo.getByClaim(claim)
        val mainPartition = partitionRepo.getByPosition(Position2D(claim.position))
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

    private fun getSurroundingPositions(position: Position2D, radius: Int): List<Position2D> {
        val positions = mutableListOf<Position2D>()

        for (i in -1 * radius..1 * radius) {
            for (j in -1 * radius..1 * radius) {
                positions.add(Position2D(position.x + i, position.z + j))
            }
        }
        return positions
    }

    private fun getLinked(partition: Partition, testPartitions: Set<Partition>): Set<Partition> {
        return testPartitions.filter { it.isPartitionLinked(partition) && it.claimId == partition.claimId }.toSet()
    }

    /**
     * Gets the partition that the claim bell is located in.
     * @param claim The target claim.
     * @return The partition that the target claim's claim bell is physically located in.
     */
    private fun getPrimaryPartition(claim: Claim): Partition {
        val claimPartitions = partitionRepo.getByClaim(claim)
        return partitionRepo.getByPosition(Position2D(claim.position)).intersect(claimPartitions.toSet()).first()
    }

    /**
     * Gets all partitions in the target chunk.
     * @param worldId The world the chunk is in.
     * @param position The coordinates of the chunk.
     * @return A set of partitions that exist within the target chunk.
     */
    private fun getByChunk(worldId: UUID, position: Position2D): Set<Partition> {
        return filterByWorld(worldId, partitionRepo.getByChunk(position))
    }

    /**
     * Gets all partitions that are adjacent and connected to the target partition.
     * @param partition The target partition.
     * @return The partitions in the world that are physically touching the target partition.
     */
    private fun getAdjacent(partition: Partition): ArrayList<Partition> {
        // Find all partitions within the chunks of the partition
        val claim = claimService.getById(partition.claimId) ?: return ArrayList()
        val chunkPartitions = ArrayList<Partition>()
        val chunks = partition.getChunks()
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