package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.VisualisationService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
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
        val borders = getPartitionedBorders(claim).values.flatten()

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
        val resultingBorder: ArrayList<Position2D> = arrayListOf()
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
        } while (previousPosition != startingPosition)
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
     */
    private fun isBlockVisible(loc: Location): Boolean {
        val above = Location(loc.world, loc.x, loc.y + 1, loc.z).block.blockData.material
        val below = Location(loc.world, loc.x, loc.y - 1, loc.z).block.blockData.material
        return transparentMaterials.contains(above) || transparentMaterials.contains(below)
    }

    private fun get3DPositions(positions: Set<Position2D>, renderLocation: Location): Set<Position3D> {
        val visualisedBlocks: MutableSet<Position3D> = mutableSetOf()
        for (position in positions) {
            for (y in renderLocation.blockY + 1 .. renderLocation.blockY + 1 + upperRange) {
                val blockLocation = Location(renderLocation.world, position.x.toDouble(),
                    y.toDouble(), position.z.toDouble())
                if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue
                if (!isBlockVisible(blockLocation)) continue
                visualisedBlocks.add(Position3D(blockLocation))
                break
            }
            for (y in renderLocation.blockY downTo renderLocation.blockY - lowerRange) {
                val blockLocation = Location(renderLocation.world, position.x.toDouble(),
                    y.toDouble(), position.z.toDouble())
                if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue
                if (!isBlockVisible(blockLocation)) continue
                visualisedBlocks.add(Position3D(blockLocation))
                break
            }
        }
        return visualisedBlocks
    }
}