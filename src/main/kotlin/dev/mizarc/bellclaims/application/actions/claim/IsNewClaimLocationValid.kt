package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.results.claim.IsNewClaimLocationValidResult
import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.config.MainConfig
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position
import dev.mizarc.bellclaims.domain.values.Position2D
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.floor

class IsNewClaimLocationValid(private val claimRepository: ClaimRepository,
                              private val partitionRepository: PartitionRepository,
                              private val worldManipulationService: WorldManipulationService,
                              private val config: MainConfig) {
    fun execute(position: Position, worldId: UUID): IsNewClaimLocationValidResult {
        // Create the area that is required for the partition
        val initialClaimSize = config.initialClaimSize
        val offsetMin = (initialClaimSize - 1) / 2
        val offsetMax = initialClaimSize / 2
        val area = Area(
            Position2D(position.x - offsetMin, position.z - offsetMin),
            Position2D(position.x + offsetMax, position.z + offsetMax)
        )

        // Get the partitions that exist in the occupied chunk space
        val chunks = area.getChunks().flatMap { getSurroundingPositions(it, 1) }
        val partitions = chunks.flatMap { getByChunk(worldId, it) }.toSet()


        // Check if the area with the padded boundary overlaps
        val areaWithBoundary = Area(
            Position2D( area.lowerPosition2D.x - config.distanceBetweenClaims,
                area.lowerPosition2D.z - config.distanceBetweenClaims),
            Position2D( area.upperPosition2D.x + config.distanceBetweenClaims,
                area.upperPosition2D.z + config.distanceBetweenClaims))
        for (partition in partitions) {
            if (partition.isAreaOverlap(areaWithBoundary)) {
                return IsNewClaimLocationValidResult.Overlap
            }
        }

        // Validate area is within world border
        if (!worldManipulationService.isInsideWorldBorder(worldId, areaWithBoundary)) {
            return IsNewClaimLocationValidResult.TooCloseToWorldBorder
        }

        return IsNewClaimLocationValidResult.Valid
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

    private fun getByChunk(worldId: UUID, position2D: Position2D): Set<Partition> {
        return filterByWorld(worldId, partitionRepository.getByChunk(Position2D(position2D.x, position2D.z)))
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