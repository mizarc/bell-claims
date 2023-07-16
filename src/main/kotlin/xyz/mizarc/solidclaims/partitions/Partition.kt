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
    constructor(claimId: UUID, area: Area): this(UUID.randomUUID(), claimId, area)
    constructor(builder: Builder): this(builder.id, builder.claimId, Area(builder.firstPosition2D, builder.secondPosition2D))

    /**
     * Checks whether the specified position is within the bounds of this claim.
     * @param position2D The position to check for.
     * @param world The world of the position.
     * @return True if the position is within the partition.
     */
    fun isPositionInPartition(position2D: Position2D): Boolean {
        return (area.isPositionInArea(position2D))
    }

    /**
     * Checks whether the specified position is in one of the corners of this claim.
     * @param position2D The position to check for.
     * @return True if the position is in a corner.
     */
    fun isPositionInCorner(position2D: Position2D): Boolean {
        return (area.isPositionInCorner(position2D))
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
     * Checks if a partition is directly adjacent to this one.
     * @param partition The area to check.
     * @return True if area is adjacent.
     */
    fun isPartitionAdjacent(partition: Partition): Boolean {
        return area.isAreaAdjacent(partition.area)
    }

    /**
     * Checks if partition is connected to another partition
     */
    fun isPartitionLinked(partition: Partition): Boolean {
        return isPartitionAdjacent(partition) && partition.claimId == claimId
    }

    /**
     * Gets a list of the chunks that this claim takes up.
     * @return List of chunk positions.
     */
    fun getChunks(): ArrayList<Position2D> {
        val firstChunk = area.lowerPosition2D.toChunk()
        val secondChunk = area.upperPosition2D.toChunk()

        val chunks: ArrayList<Position2D> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position2D(x, z))
            }
        }

        return chunks
    }

    class Builder(var firstPosition2D: Position2D) {
        val id: UUID = UUID.randomUUID()
        lateinit var secondPosition2D: Position2D
        lateinit var claimId: UUID

        fun build(): Partition {
            if (!::secondPosition2D.isInitialized || !::claimId.isInitialized) {
                throw IncompleteBuilderException("Builder requires a filled second position and claim id.")
            }
            return Partition(this)
        }
    }

    class Resizer(val partition: Partition, val selectedCorner: Position2D) {
        lateinit var newArea: Area

        fun getExtraBlockCount(): Int {
            return ((newArea.upperPosition2D.x - newArea.lowerPosition2D.x + 1) *
                    (newArea.upperPosition2D.z - newArea.lowerPosition2D.z + 1)).absoluteValue -
                    ((partition.area.upperPosition2D.x - partition.area.upperPosition2D.x + 1) *
                    (partition.area.upperPosition2D.z - partition.area.upperPosition2D.z+ 1)).absoluteValue
        }

        fun setNewCorner(newPosition2D: Position2D) {
            var firstPosition2D = if (selectedCorner.x == partition.area.lowerPosition2D.x) {
                Position2D(newPosition2D.x, 0)
            } else {
                Position2D(partition.area.lowerPosition2D.x, 0)
            }

            var secondPosition2D = if (selectedCorner.x == partition.area.upperPosition2D.x) {
                Position2D(newPosition2D.x, 0)
            } else {
                Position2D(partition.area.upperPosition2D.x, 0)
            }

            firstPosition2D = if (selectedCorner.z == partition.area.lowerPosition2D.z) {
                Position2D(firstPosition2D.x, newPosition2D.z)
            } else {
                Position2D(firstPosition2D.x, partition.area.lowerPosition2D.z)
            }

            secondPosition2D = if (selectedCorner.z == partition.area.upperPosition2D.z) {
                Position2D(secondPosition2D.x, newPosition2D.z)
            } else {
                Position2D(secondPosition2D.x, partition.area.upperPosition2D.z)
            }

            newArea = Area(firstPosition2D, secondPosition2D)
        }
    }
}