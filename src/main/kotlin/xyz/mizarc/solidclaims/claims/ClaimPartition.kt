package xyz.mizarc.solidclaims.claims

import org.bukkit.Location

/**
 * A partition of a claim. Claims can be made up of multiple partitions that defines the overall shape. A single
 * partition holds the positions of two corners of a rectangle and the claim associated with it.
 * @constructor Creates a partition with all required data.
 * @property claim The claim linked to this partition.
 * @property firstPosition The pair of integers defining the first position.
 * @property secondPosition The pair of integers defining the second position.
 */
class ClaimPartition(var claim: Claim, var firstPosition: Pair<Int, Int>, var secondPosition: Pair<Int, Int>) {
    init {
        // Make it so that the first position coordinates are smaller than the second position coordinates.
        if (firstPosition.first > secondPosition.first) {
            val newFirstPosition = Pair(secondPosition.first, firstPosition.second)
            val newSecondPosition = Pair(firstPosition.first, secondPosition.second)
            firstPosition = newFirstPosition
            secondPosition = newSecondPosition
        }

        if (firstPosition.second > secondPosition.second) {
            val newFirstPosition = Pair(firstPosition.first, secondPosition.second)
            val newSecondPosition = Pair(secondPosition.first, firstPosition.second)
            firstPosition = newFirstPosition
            secondPosition = newSecondPosition
        }
    }

    /**
     * Checks whether the specified location in the world is within the bounds of this claim.
     * @param location The location object to check for.
     * @return True if the location is within the claim.
     */
    fun isLocationInClaim(location: Location) : Boolean {
        if (location.world?.uid != claim.worldId) {
            return false
        }

        if (location.x >= firstPosition.first
            && location.x <= secondPosition.first
            && location.z >= firstPosition.second
            && location.z <= secondPosition.second) {
            return true
        }

        return false
    }

    fun isBoxInClaim(otherFirstPosition: Pair<Int, Int>, otherSecondPosition: Pair<Int, Int>) : Boolean {
        return firstPosition.first <= otherSecondPosition.first
                && secondPosition.first >= otherFirstPosition.first
                && firstPosition.second <= otherSecondPosition.second
                && secondPosition.second >= otherFirstPosition.second
    }

    /**
     * Gets the list of X and Z block positions that define the edges of a claim.
     * @return A set of Integer pairs specifying the X and Z coordinates of each position.
     */
    fun getEdgeBlockPositions() : Array<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.first..secondPosition.first) {
            blocks.add(Pair(block, firstPosition.second))
        }
        for (block in firstPosition.first..secondPosition.first) {
            blocks.add(Pair(block, secondPosition.second))
        }
        for (block in firstPosition.second..secondPosition.second) {
            blocks.add(Pair(block, firstPosition.first))
        }
        for (block in firstPosition.second..secondPosition.second) {
            blocks.add(Pair(block, secondPosition.first))
        }
        return blocks.toTypedArray()
    }

    /**
     * Converts the X and Z coordinates of the first position to a Location object.
     * @return Location object including the claim's world and an empty Y coordinate.
     */
    fun getFirstLocation() : Location {
        return Location(claim.getWorld(), firstPosition.first.toDouble(), 0.0, firstPosition.second.toDouble())
    }

    /**
     * Converts the X and Z coordinates of the second position to a Location object.
     * @return Location object including the claim's world and an empty Y coordinate.
     */
    fun getSecondLocation() : Location {
        return Location(claim.getWorld(), secondPosition.first.toDouble(), 0.0, secondPosition.second.toDouble())
    }
}