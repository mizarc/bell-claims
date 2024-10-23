package dev.mizarc.bellclaims.infrastructure.services

import com.sun.org.apache.xpath.internal.operations.Bool
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.VisualisationService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import dev.mizarc.bellclaims.utils.toward
import dev.mizarc.bellclaims.utils.transparentMaterials
import org.bukkit.Location
import javax.swing.border.Border

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
        val resultingBorder = mutableSetOf<Position2D>()

        // Trace outer border
        val outerBorder = traceOuterBorder(borders)
        resultingBorder.addAll(outerBorder)
        borders.removeAll(outerBorder)

        // Trace all inner borders
        val innerBorders = traceInnerBorders(borders, outerBorder, claim)
        for (border in innerBorders) {
            resultingBorder.addAll(border)
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

    /**
     * Determine if the position exists within a set of partitions.
     * @param position The 2D position in the world.
     * @param partitions The set of partitions to check against.
     * @return True if the position exists in at least one of the partitions.
     */
    private fun isPositionInPartitions(position: Position2D, partitions: Set<Partition>): Boolean {
        for (partition in partitions) {
            if (partition.isPositionInPartition(position)) {
                return true
            }
        }
        return false
    }

    /**
     * Trace the outer border of a claim.
     *
     * @param borders The list of border block positions.
     * @return The set of positions making up the outermost border of the claim.
     */
    private fun traceOuterBorder(borders: MutableList<Position2D>): Set<Position2D> {
        // Get starting position by finding the position with the largest x coordinate.
        var startingPosition = borders[0]
        for (border in borders) {
            if (border.x > startingPosition.x) {
                startingPosition = border
            }
        }

        // Get second position by getting block either in front or to the right in a clockwise direction
        var currentPosition = findNextPosition(startingPosition, borders, Position2D(0, 1), Position2D(-1, 0))
            ?: return setOf()

        return traceBorder(startingPosition, currentPosition, borders)
    }

    /**
     * Trace the inner border of a claim, given a border that already omits the outer border.
     *
     * The outer border must be omitted by running the outer border trace first and modifying the border list, otherwise
     * it will be included by this inner border function and cause potential issues.
     * @param borders The list of border block positions excluding the outer border.
     * @param outerBorder The list of border block positions of the outer border.
     * @param claim The claim to check against.
     * @return A set of sets, each being a collection of positions that make up a complete inner border
     */
    private fun traceInnerBorders(borders: MutableList<Position2D>, outerBorder: Set<Position2D>, claim: Claim):
            Set<Set<Position2D>> {
        val partitions = partitionService.getByClaim(claim)
        val queryBorders = borders.toMutableList()
        val resultingBorders: MutableSet<Set<Position2D>> = mutableSetOf()
        val checkedPositions = mutableSetOf<Position2D>()

        // Perform check for each border block
        while (queryBorders.isNotEmpty()) {
            val startingPosition = queryBorders[0]

            // A map of directions to move to depending on found direction (North, South, West, East)
            val directions = mapOf(
                Position2D(startingPosition.x, startingPosition.z - 1) to listOf(Position2D(1, 0), Position2D(0, 1)),
                Position2D(startingPosition.x, startingPosition.z + 1) to listOf(Position2D(-1, 0), Position2D(0, -1)),
                Position2D(startingPosition.x - 1, startingPosition.z) to listOf(Position2D(0, -1), Position2D(1, 0)),
                Position2D(startingPosition.x + 1, startingPosition.z) to listOf(Position2D(0, 1), Position2D(-1, 0))
            )

            // If on the edge, find the first block to navigate towards
            var currentPosition: Position2D = startingPosition
            for ((position, candidates) in directions) {
                if (!isPositionInPartitions(position, partitions)) {
                    currentPosition = findNextPosition(currentPosition, borders, *candidates.toTypedArray()) ?: continue
                    break
                }
            }

            // Stop this iteration if no navigation is found
            if (currentPosition == startingPosition) {
                queryBorders.remove(startingPosition)
                checkedPositions.add(startingPosition)
                continue
            }

            // Trace using the found edge
            val mergedBorder = borders.toMutableList()
            mergedBorder.addAll(outerBorder)
            val innerBorder = traceBorder(startingPosition, currentPosition, mergedBorder)
            resultingBorders.add(innerBorder)
            checkedPositions.addAll(innerBorder)
            break
        }

        return resultingBorders
    }

    /**
     * Trace outer/inner border perimeter by rotating until the starting position is found.
     * @param startingPosition The starting position on the border.
     * @param nextPosition The following position on the border to move to.
     * @param borders The entire border structure to trace on.
     * @return The set of positions that make up the entire traced border.
     */
    private fun traceBorder(startingPosition: Position2D, nextPosition: Position2D,
                            borders: MutableList<Position2D>): Set<Position2D> {
        val resultingBorder = mutableSetOf<Position2D>()
        var previousPosition = startingPosition
        var currentPosition = nextPosition
        do {
            val newPosition: Position2D = when (getTravelDirection(previousPosition, currentPosition)) {
                Direction.North -> findNextPosition(currentPosition, borders,
                    Position2D(-1, 0), Position2D(0, -1), Position2D(1, 0))
                Direction.East -> findNextPosition(currentPosition, borders,
                    Position2D(0, -1), Position2D(1, 0), Position2D(0, 1))
                Direction.South -> findNextPosition(currentPosition, borders,
                    Position2D(1, 0), Position2D(0, 1), Position2D(-1, 0))
                else -> findNextPosition(currentPosition, borders,
                    Position2D(0, 1), Position2D(-1, 0), Position2D(0, -1))
            } ?: continue

            resultingBorder.add(newPosition)
            previousPosition = currentPosition
            currentPosition = newPosition
        } while (previousPosition != startingPosition)
        return resultingBorder
    }

    /**
     * Find the next valid position in the border map given directions to navigate towards.
     * @param current The current position.
     * @param borders The complete border structure to check against.
     * @param directions Relative directions from the current position to attempt to navigate towards.
     * @return The valid position that was found within the border structure, null if not found.
     */
    private fun findNextPosition(current: Position2D, borders: MutableList<Position2D>,
                                 vararg directions: Position2D): Position2D? {
        return directions.mapNotNull { direction ->
            borders.firstOrNull { it.x == current.x + direction.x && it.z == current.z + direction.z }}.firstOrNull()
    }
}