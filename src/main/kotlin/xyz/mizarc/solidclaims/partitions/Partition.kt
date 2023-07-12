package xyz.mizarc.solidclaims.partitions

import xyz.mizarc.solidclaims.exceptions.IncompleteBuilderException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 * @constructor Creates a partition with all required data.
 * @property claim The claim linked to this partition.
 * @property area The area defining the space of this partition.
 */
class Partition(var id: UUID, var claimId: UUID, var area: Area) {
    constructor(builder: Builder): this(builder.id, builder.claimId, Area(builder.firstPosition, builder.secondPosition))

    /**
     * Checks whether the specified position is within the bounds of this claim.
     * @param position The position to check for.
     * @param world The world of the position.
     * @return True if the position is within the partition.
     */
    fun isPositionInPartition(position: Position): Boolean {
        return (area.isPositionInArea(position))
    }

    /**
     * Checks whether the specified position is in one of the corners of this claim.
     * @param position The position to check for.
     * @return True if the position is in a corner.
     */
    fun isPositionInCorner(position: Position): Boolean {
        return (area.isPositionInCorner(position))
    }

    /**
     * Checks whether the specified area overlaps this claim.
     * @param areaQuery The area to check.
     * @param world The world of the position.
     * @return True if the position is within the claim.
     */
    fun isAreaOverlap(areaQuery: Area): Boolean {
        return (area.isAreaOverlap(areaQuery))
    }

    /**
     * Checks if an area is directly adjacent to this one.
     * @param areaQuery The area to check.
     * @return True if area is adjacent.
     */
    fun isPartitionAdjacent(partition: Partition): Boolean {
        return area.isAreaAdjacent(partition.area)
    }

    fun isPartitionLinked(partition: Partition): Boolean {
        return isPartitionAdjacent(partition) && partition.claimId == claimId
    }

    /**
     * Gets a list of the chunks that this claim takes up.
     * @return List of chunk positions.
     */
    fun getChunks(): ArrayList<Position> {
        val firstChunk = area.lowerPosition.toChunk()
        val secondChunk = area.upperPosition.toChunk()

        val chunks: ArrayList<Position> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position(x, z))
            }
        }

        return chunks
    }

    class Builder(var firstPosition: Position) {
        val id: UUID = UUID.randomUUID()
        lateinit var secondPosition: Position
        lateinit var claimId: UUID

        fun build(): Partition {
            if (!::secondPosition.isInitialized || !::claimId.isInitialized) {
                throw IncompleteBuilderException("Builder requires a filled second position and claim id.")
            }
            return Partition(this)
        }
    }

    class Resizer(val partition: Partition, val selectedCorner: Position) {
        lateinit var newArea: Area

        fun getExtraBlockCount(): Int {
            return ((newArea.upperPosition.x - newArea.lowerPosition.x + 1) *
                    (newArea.upperPosition.z - newArea.lowerPosition.z + 1)).absoluteValue -
                    ((partition.area.upperPosition.x - partition.area.upperPosition.x + 1) *
                    (partition.area.upperPosition.z - partition.area.upperPosition.z+ 1)).absoluteValue
        }

        fun setNewCorner(newPosition: Position) {
            var firstPosition = if (selectedCorner.x == partition.area.lowerPosition.x) {
                Position(newPosition.x, 0)
            } else {
                Position(partition.area.lowerPosition.x, 0)
            }

            var secondPosition = if (selectedCorner.x == partition.area.upperPosition.x) {
                Position(newPosition.x, 0)
            } else {
                Position(partition.area.upperPosition.x, 0)
            }

            firstPosition = if (selectedCorner.z == partition.area.lowerPosition.z) {
                Position(firstPosition.x, newPosition.z)
            } else {
                Position(firstPosition.x, partition.area.lowerPosition.z)
            }

            secondPosition = if (selectedCorner.z == partition.area.upperPosition.z) {
                Position(secondPosition.x, newPosition.z)
            } else {
                Position(secondPosition.x, partition.area.upperPosition.z)
            }

            newArea = Area(firstPosition, secondPosition)
        }
    }
}