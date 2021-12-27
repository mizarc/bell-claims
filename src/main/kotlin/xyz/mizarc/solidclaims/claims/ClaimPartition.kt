package xyz.mizarc.solidclaims.claims

import org.bukkit.World
import xyz.mizarc.solidclaims.Area
import xyz.mizarc.solidclaims.Position

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 * @constructor Creates a partition with all required data.
 * @property claim The claim linked to this partition.
 * @property firstPosition The pair of integers defining the first position.
 * @property secondPosition The pair of integers defining the second position.
 */
class ClaimPartition(var claim: Claim, var area: Area) {
    /**
     * Checks whether the specified position in the world is within the bounds of this claim.
     * @param position The position to check for.
     * @param world The world of the position.
     * @return True if the position is within the claim.
     */
    fun isPositionInPartition(position: Position, world: World): Boolean {
        return (area.isPositionInArea(position)) && (world.uid == claim.worldId)
    }

    /**
     * Checks whether the specified area in the world overlaps this claim
     * @param position The position to check for.
     * @param world The world of the position.
     * @return True if the position is within the claim.
     */
    fun isAreaOverlapsPartition(areaQuery: Area, world: World): Boolean {
        return (area.isAreaOverlap(area)) && (world.uid == claim.worldId)
    }

    /**
     * Checks if area is directly adjacent to this one.
     * @param area The area to check
     * @return True if area is adjacent.
     */
    fun isPartitionAdjacent(areaQuery: Area, world: World): Boolean {
        return area.isAreaAdjacent(areaQuery) && (world.uid == claim.worldId)
    }
}