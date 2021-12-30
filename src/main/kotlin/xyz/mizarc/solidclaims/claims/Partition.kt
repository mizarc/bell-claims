package xyz.mizarc.solidclaims.claims

import org.bukkit.World

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 * @constructor Creates a partition with all required data.
 * @property claim The claim linked to this partition.
 * @property area The area defining the space of this partition.
 */
class Partition(var claim: Claim, var area: Area) {
    fun isPositionInCorner(position: Position, world: World): Boolean {
        return (area.isPositionInCorner(position) && (world.uid == claim.worldId))
    }
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
     * @param areaQuery The area to check.
     * @param world The world of the position.
     * @return True if the position is within the claim.
     */
    fun isAreaOverlap(areaQuery: Area, world: World): Boolean {
        return (area.isAreaOverlap(areaQuery)) && (world.uid == claim.worldId)
    }

    /**
     * Checks if area is directly adjacent to this one.
     * @param areaQuery The area to check.
     * @return True if area is adjacent.
     */
    fun isAreaAdjacent(areaQuery: Area, world: World): Boolean {
        return area.isAreaAdjacent(areaQuery) && (world.uid == claim.worldId)
    }
}