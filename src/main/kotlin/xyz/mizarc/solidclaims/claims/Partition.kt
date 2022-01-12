package xyz.mizarc.solidclaims.claims

import org.bukkit.World
import java.util.*
import kotlin.collections.ArrayList

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 * @constructor Creates a partition with all required data.
 * @property claim The claim linked to this partition.
 * @property area The area defining the space of this partition.
 */
class Partition(var id: UUID, var claimId: UUID, var area: Area) {
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
    fun isAreaAdjacent(areaQuery: Area): Boolean {
        return area.isAreaAdjacent(areaQuery)
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
}