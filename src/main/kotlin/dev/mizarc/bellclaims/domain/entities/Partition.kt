package dev.mizarc.bellclaims.domain.entities

import dev.mizarc.bellclaims.domain.exceptions.IncompletePartitionBuilderException
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position
import dev.mizarc.bellclaims.domain.values.Position2D
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 *
 * @constructor Creates a partition with all required data.
 * @property claimId The unique id of the claim linked to this partition.
 * @property area The area defining the space of this partition.
 */
data class Partition(var id: UUID, var claimId: UUID, var area: Area) {
    constructor(claimId: UUID, area: Area): this(UUID.randomUUID(), claimId, area)
    constructor(builder: Builder): this(builder.id, builder.claimId,
        Area(builder.firstPosition2D, builder.secondPosition2D)
    )

    /**
     * Checks whether the specified position is within the bounds of this claim.
     *
     * @param position The position to check for.
     * @return True if the position is within the partition.
     */
    fun isPositionInPartition(position: Position): Boolean {
        return (area.isPositionInArea(position))
    }

    /**
     * Checks whether the specified position is in one of the corners of this claim.
     *
     * @param position2D The position to check for.
     * @return True if the position is in a corner.
     */
    fun isPositionInCorner(position2D: Position2D): Boolean {
        return (area.isPositionInCorner(position2D))
    }

    /**
     * Checks whether the specified area overlaps this claim.
     *
     * @param areaQuery The area to check.
     * @return True if the position is within the claim.
     */
    fun isAreaOverlap(areaQuery: Area): Boolean {
        return (area.isAreaOverlap(areaQuery))
    }

    /**
     * Checks if a partition is directly adjacent to this one.
     *
     * @param partition The partition to check.
     * @return True if partition is adjacent.
     */
    fun isPartitionAdjacent(partition: Partition): Boolean {
        return area.isAreaAdjacent(partition.area)
    }

    /**
     * Checks if partition is adjacent and part of the same claim as this one.
     *
     * @param partition The partition to check.
     * @return True if partition is linked.
     */
    fun isPartitionLinked(partition: Partition): Boolean {
        return isPartitionAdjacent(partition) && partition.claimId == claimId
    }

    /**
     * Gets a list of the chunks that this claim takes up.
     *
     * @return List of chunk positions.
     */
    fun getChunks(): ArrayList<Position2D> {
        val firstChunk = area.lowerPosition2D.getChunk()
        val secondChunk = area.upperPosition2D.getChunk()

        val chunks: ArrayList<Position2D> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position2D(x, z))
            }
        }

        return chunks
    }

    /**
     * Gets the amount of blocks that make up the claim.
     *
     * @return The amount of blocks.
     */
    fun getBlockCount(): Int {
        return area.getBlockCount()
    }

    /**
     * A builder for creating instances of a Partition.
     *
     * @property claimId The claim that the partition should be linked to.
     * @property firstPosition2D The first corner selection of the partition.
     */
    class Builder(val claimId: UUID, var firstPosition2D: Position2D) {
        val id: UUID = UUID.randomUUID()
        lateinit var secondPosition2D: Position2D

        fun build(): Partition {
            if (!::secondPosition2D.isInitialized) {
                throw IncompletePartitionBuilderException("Builder requires a filled second position.")
            }
            return Partition(this)
        }
    }

    /**
     * A builder for resizing existing partitions.
     *
     * @property partition The partition that the operation should affect.
     * @property selectedCorner The existing corner that should be moved.
     */
    class Resizer(val partition: Partition, val selectedCorner: Position2D) {
        lateinit var newArea: Area

        /**
         * Gets the amount of extra blocks that the claim will occupy after the resize operation.
         *
         * @return The number of blocks.
         */
        fun getExtraBlockCount(): Int {
            return ((newArea.upperPosition2D.x - newArea.lowerPosition2D.x + 1) *
                    (newArea.upperPosition2D.z - newArea.lowerPosition2D.z + 1)).absoluteValue -
                    ((partition.area.upperPosition2D.x - partition.area.lowerPosition2D.x + 1) *
                    (partition.area.upperPosition2D.z - partition.area.lowerPosition2D.z + 1)).absoluteValue
        }

        /**
         * Assigns a new position for the selected corner.
         *
         * @param newPosition2D The new position to be moved to.
         */
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