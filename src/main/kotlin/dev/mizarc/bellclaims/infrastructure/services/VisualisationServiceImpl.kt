package dev.mizarc.bellclaims.infrastructure.services

import com.sun.org.apache.xpath.internal.operations.Bool
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.VisualisationService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import dev.mizarc.bellclaims.utils.toward
import dev.mizarc.bellclaims.utils.transparentMaterials
import org.bukkit.Location

private const val upperRange = 5
private const val lowerRange = 50

class VisualisationServiceImpl(private val partitionService: PartitionService): VisualisationService {
    enum class Direction {
        North,
        South,
        East,
        West
    }

    override fun getOuterBorders(claim: Claim): Set<Position2D> {
        val borders = getPartitionedBorders(claim).values.flatten().toMutableList()

        // Get starting position by finding the position with the largest x coordinate.
        // Could be the largest or smallest any coordinate, this is personal choice.
        var startingPosition = borders[0]
        for (border in borders) {
            if (border.x > startingPosition.x) {
                startingPosition = border
            }
        }

        // Get second position by getting block either in front or to the right in a clockwise direction
        var currentPosition = borders.firstOrNull { it.z == startingPosition.z + 1 && it.x == startingPosition.x }
            ?: borders.first { it.x == startingPosition.x - 1 && it.z == startingPosition.z }

        // Loop through edges by first checking left, then front, then right side. Traverse whichever is found first
        // until back to the starting position.
        val resultingBorder: MutableSet<Position2D> = mutableSetOf()
        var previousPosition: Position2D = startingPosition
        do {
            val nextPosition: Position2D = when (getTravelDirection(previousPosition, currentPosition)) {
                Direction.North -> {
                    borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                        ?: borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                        ?: borders.first { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                }

                Direction.East -> {
                    borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                        ?: borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                        ?: borders.first { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                }

                Direction.South -> {
                    borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                        ?: borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                        ?: borders.first { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                }

                else -> {
                    borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                        ?: borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                        ?: borders.first { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                }
            }
            resultingBorder.add(nextPosition)
            previousPosition = currentPosition
            currentPosition = nextPosition
            borders.remove(currentPosition)
        } while (previousPosition != startingPosition)


        val partitions = partitionService.getByClaim(claim)
        while (!borders.isEmpty()) {
            val border = borders[0]

            // If on the edge, find the next block to navigate towards
            val startingPosition = border
            var currentPosition: Position2D? = null
            if (!isPositionInPartitions(Position2D(border.x, border.z - 1), partitions)) { // North
                currentPosition = borders.firstOrNull { it.x == startingPosition.x - 1 && it.z == startingPosition.z }
                    ?: borders.firstOrNull { it.x == startingPosition.x && it.z == startingPosition.z + 1}
            }
            else if (!isPositionInPartitions(Position2D(border.x, border.z + 1), partitions)) { // South
                currentPosition = borders.firstOrNull { it.x == startingPosition.x + 1 && it.z == startingPosition.z }
                    ?: borders.firstOrNull { it.x == startingPosition.x && it.z == startingPosition.z - 1}
            }
            else if (!isPositionInPartitions(Position2D(border.x - 1, border.z), partitions)) { // West
                currentPosition = borders.firstOrNull { it.x == startingPosition.x && it.z == startingPosition.z + 1}
                    ?: borders.firstOrNull { it.x == startingPosition.x + 1 && it.z == startingPosition.z}
            }
            else if (!isPositionInPartitions(Position2D(border.x + 1, border.z), partitions)) { // East
                currentPosition = borders.firstOrNull { it.x == startingPosition.x && it.z == startingPosition.z - 1}
                    ?: borders.firstOrNull { it.x == startingPosition.x - 1 && it.z == startingPosition.z}
            }

            // Stop this iteration if not on an edge
            var validCurrentPosition: Position2D
            if (currentPosition == null) {
                borders.remove(borders[0])
                continue
            }

            previousPosition = startingPosition
            validCurrentPosition = currentPosition
            do {
                val nextPosition: Position2D = when (getTravelDirection(previousPosition, validCurrentPosition)) {
                    Direction.North -> {
                        borders.firstOrNull { it.x == validCurrentPosition.x + 1 && it.z == validCurrentPosition.z}
                            ?: borders.firstOrNull { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z - 1}
                            ?: borders.first { it.x == validCurrentPosition.x - 1 && it.z == validCurrentPosition.z }
                    }

                    Direction.East -> {
                        borders.firstOrNull { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z + 1}
                            ?: borders.firstOrNull { it.x == validCurrentPosition.x + 1 && it.z == validCurrentPosition.z}
                            ?: borders.first { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z - 1}
                    }

                    Direction.South -> {
                        borders.firstOrNull { it.x == validCurrentPosition.x - 1&& it.z == validCurrentPosition.z}
                            ?: borders.firstOrNull { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z + 1}
                            ?: borders.first { it.x == validCurrentPosition.x + 1 && it.z == validCurrentPosition.z}
                    }

                    else -> {
                        borders.firstOrNull { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z - 1}
                            ?: borders.firstOrNull { it.x == validCurrentPosition.x - 1 && it.z == validCurrentPosition.z}
                            ?: borders.first { it.x == validCurrentPosition.x && it.z == validCurrentPosition.z + 1}
                    }
                }

                resultingBorder.add(nextPosition)
                previousPosition = validCurrentPosition
                validCurrentPosition = nextPosition
                borders.remove(validCurrentPosition)
            } while (previousPosition != startingPosition)
        }

        return resultingBorder.toSet()
    }

    override fun getPartitionedBorders(claim: Claim): Map<Partition, Set<Position2D>> {
        val borders: MutableMap<Partition, MutableSet<Position2D>> = mutableMapOf()
        val partitions = partitionService.getByClaim(claim)
        for (partition in partitions) {
            val partitionedBorder = borders.getOrPut(partition) { mutableSetOf() }
            partitionedBorder.addAll(partition.area.getEdgeBlockPositions())
        }
        return borders
    }

    override fun getPartitionedCorners(claim: Claim): Map<Partition, Set<Position2D>> {
        val corners: MutableMap<Partition, MutableSet<Position2D>> = mutableMapOf()
        val partitions = partitionService.getByClaim(claim)
        for (partition in partitions) {
            val partitionedBorder = corners.getOrPut(partition) { mutableSetOf() }
            partitionedBorder.addAll(partition.area.getCornerBlockPositions())
        }
        return corners
    }

    override fun getMainPartitionBorders(claim: Claim): Set<Position2D> {
        val partition = partitionService.getPrimary(claim) ?: return setOf()
        return partition.area.getEdgeBlockPositions().toSet()
    }

    override fun getMainPartitionCorners(claim: Claim): Set<Position2D> {
        val partition = partitionService.getPrimary(claim) ?: return setOf()
        return partition.area.getCornerBlockPositions().toSet()
    }

    override fun get3DOuterBorders(claim: Claim, renderLocation: Location): Set<Position3D> {
        return get3DPositions(getOuterBorders(claim), renderLocation)
    }

    override fun get3DPartitionedBorders(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>> {
        val borders = getPartitionedBorders(claim)
        return borders.mapValues { (_, border) -> get3DPositions(border, renderLocation).toMutableSet() }
    }

    override fun get3DPartitionedCorners(claim: Claim, renderLocation: Location): Map<Partition, Set<Position3D>> {
        val borders = getPartitionedCorners(claim)
        return borders.mapValues { (_, border) -> get3DPositions(border, renderLocation).toMutableSet() }
    }

    override fun get3DMainPartitionBorders(claim: Claim, renderLocation: Location): Set<Position3D> {
        return get3DPositions(getMainPartitionBorders(claim), renderLocation)
    }

    override fun get3DMainPartitionCorners(claim: Claim, renderLocation: Location): Set<Position3D> {
        return get3DPositions(getMainPartitionCorners(claim), renderLocation)
    }

    /**
     * Gets the cardinal direction movement from one Position to another.
     * @param first The starting position.
     * @param second The position to move to.
     * @return The Direction enum that is being moved to.
     */
    private fun getTravelDirection(first: Position2D, second: Position2D): Direction {
        return when {
            second.z > first.z -> Direction.South
            second.z < first.z -> Direction.North
            second.x > first.x -> Direction.East
            else -> Direction.West
        }
    }

    /**
     * Determine if a block is a floor/ceiling and therefore should be considered visible
     * @param location The location of the block.
     * @return True if the block is considered visible.
     */
    private fun isBlockVisible(location: Location): Boolean {
        val above = Location(location.world, location.x, location.y + 1, location.z).block.blockData.material
        val below = Location(location.world, location.x, location.y - 1, location.z).block.blockData.material
        return transparentMaterials.contains(above) || transparentMaterials.contains(below)
    }

    /**
     * Gets the 3D positions of the first solid blocks found in both upper and lower directions.
     * @param positions The set of positions to query.
     * @param renderLocation The position of the player as a starting point.
     * @return A set of 3D positions of solid blocks.
     */
    private fun get3DPositions(positions: Set<Position2D>, renderLocation: Location): Set<Position3D> {
        val visualisedBlocks: MutableSet<Position3D> = mutableSetOf()
        for (position in positions) {
            findSolidBlock(position, renderLocation, upperRange, 1)?.let { visualisedBlocks.add(it) }
            findSolidBlock(position, renderLocation, lowerRange, -1)?.let { visualisedBlocks.add(it) }
        }
        return visualisedBlocks
    }

    /**
     * Gets the first solid block when querying each block up or down from a starting position.
     * @param position The 2D position in the world.
     * @param renderLocation The position of the player as a starting point.
     * @param range How many blocks to search.
     * @param direction The direction to check, 1 for up and -1 for down.
     * @return The 3D position of the first solid block.
     */
    private fun findSolidBlock(position: Position2D, renderLocation: Location,
                                range: Int, direction: Int): Position3D? {
        val startY = if (direction > 0) renderLocation.blockY + 1 else renderLocation.blockY
        val endY = renderLocation.blockY + direction * range
        for (y in startY toward endY) {
            val blockLocation = Location(
                renderLocation.world, position.x.toDouble(),
                y.toDouble(), position.z.toDouble()
            )
            if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue
            if (!isBlockVisible(blockLocation)) continue
            return Position3D(blockLocation)
        }
        return null
    }

    private fun isPositionInPartitions(position: Position2D, partitions: Set<Partition>): Boolean {
        for (partition in partitions) {
            if (partition.isPositionInPartition(position)) {
                return true
            }
        }
        return false
    }
}