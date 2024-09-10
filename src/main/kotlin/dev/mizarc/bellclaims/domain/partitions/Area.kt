package dev.mizarc.bellclaims.domain.partitions

import kotlin.math.absoluteValue

/**
 * Stores two positions that define the corners of an area.
 *
 * @property lowerPosition2D The lower corner position.
 * @property upperPosition2D The upper corner position.
 */
data class Area(var lowerPosition2D: Position2D, var upperPosition2D: Position2D) {
    init {
        sortPositionSizes()
    }

    /**
     * Checks if the position is on one of the four corners of the claim.
     *
     * @param position2D The position to check
     * @return True if in corner.
     */
    fun isPositionInCorner(position2D: Position2D): Boolean {
        if (position2D == lowerPosition2D || position2D == upperPosition2D) {
            return true
        }
        if (position2D == Position2D(lowerPosition2D.x, upperPosition2D.z)) {
            return true
        }
        if (position2D == Position2D(upperPosition2D.x, lowerPosition2D.z)) {
            return true
        }

        return false
    }

    /**
     * Checks if the specified position exists within the bounds of this area.
     *
     * @param position2D The position to check
     * @return True if in area.
     */
    fun isPositionInArea(position: Position): Boolean {
        return (position.x >= lowerPosition2D.x
                && position.x <= upperPosition2D.x
                && position.z >= lowerPosition2D.z
                && position.z <= upperPosition2D.z)
    }

    /**
     * Checks if an area is overlapping this one.
     *
     * @param area The area to check
     * @return True if area overlaps.
     */
    fun isAreaOverlap(area: Area): Boolean {
        return lowerPosition2D.x <= area.upperPosition2D.x
                && upperPosition2D.x >= area.lowerPosition2D.x
                && lowerPosition2D.z <= area.upperPosition2D.z
                && upperPosition2D.z >= area.lowerPosition2D.z
    }

    /**
     * Checks if area is directly adjacent to this one.
     *
     * @param area The area to check
     * @return True if area is adjacent.
     */
    fun isAreaAdjacent(area: Area): Boolean {
        // Top
        if (area.upperPosition2D.z < lowerPosition2D.z) {
            for (block in area.getTopEdgeBlockPositions()) {
                if (isPositionInArea(Position2D(block.x, block.z + 1))) {
                    return true
                }
            }
        }
        // Bottom
        if (area.lowerPosition2D.z > upperPosition2D.z) {
            for (block in area.getBottomEdgeBlockPositions()) {
                if (isPositionInArea(Position2D(block.x, block.z - 1))) {
                    return true
                }
            }
        }
        // Left
        if (area.lowerPosition2D.x > upperPosition2D.x) {
            for (block in area.getLeftEdgeBlockPositions()) {
                if (isPositionInArea(Position2D(block.x - 1, block.z))) {
                    return true
                }
            }
        }
        // Right
        if (area.upperPosition2D.x < lowerPosition2D.x) {
            for (block in area.getRightEdgeBlockPositions()) {
                if (isPositionInArea(Position2D(block.x + 1, block.z))) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Gets the total block count of the area.
     *
     * @return The number of blocks.
     */
    fun getBlockCount(): Int {
        return ((upperPosition2D.x - lowerPosition2D.x + 1) * (upperPosition2D.z - lowerPosition2D.z + 1)).absoluteValue
    }

    /**
     * Gets the chunk positions that this claim occupies.
     *
     * @return The list of chunk positions.
     */
    fun getChunks(): ArrayList<Position2D> {
        val firstChunk = lowerPosition2D.getChunk()
        val secondChunk = upperPosition2D.getChunk()

        val chunks: ArrayList<Position2D> = ArrayList()
        for (x in firstChunk.x..secondChunk.x) {
            for (z in firstChunk.z..secondChunk.z) {
                chunks.add(Position2D(x, z))
            }
        }

        return chunks
    }

    /**
     * Gets the length of the X axis.
     *
     * @return The
     */
    fun getXLength(): Int {
        return (upperPosition2D.x - lowerPosition2D.x).absoluteValue
    }

    /**
     * Gets the length of the Z axis
     */
    fun getZLength(): Int {
        return (upperPosition2D.z - lowerPosition2D.z).absoluteValue
    }

    fun getCornerBlockPositions(): ArrayList<Position2D> {
        val position2DS = ArrayList<Position2D>()
        position2DS.add(lowerPosition2D)
        position2DS.add(upperPosition2D)
        position2DS.add(Position2D(lowerPosition2D.x, upperPosition2D.z))
        position2DS.add(Position2D(upperPosition2D.x, lowerPosition2D.z))
        return position2DS
    }

    /**
     * Gets the list of X and Z block positions that define the edges of an area.
     * @return An array of position objects
     */
    fun getEdgeBlockPositions(): ArrayList<Position2D> {
        val blocks : ArrayList<Position2D> = ArrayList()
        for (block in lowerPosition2D.x..upperPosition2D.x) {
            blocks.add(Position2D(block, lowerPosition2D.z))
            blocks.add(Position2D(block, upperPosition2D.z))
        }
        for (block in lowerPosition2D.z..upperPosition2D.z) {
            blocks.add(Position2D(lowerPosition2D.x, block))
            blocks.add(Position2D(upperPosition2D.x, block))
        }
        return blocks
    }

    /**
     * Gets the positions of blocks that define the top edge of the area.
     * @return An array of position objects
     */
    fun getTopEdgeBlockPositions(): Array<Position2D> {
        val blocks : ArrayList<Position2D> = ArrayList()
        for (block in lowerPosition2D.x..upperPosition2D.x) {
            blocks.add(Position2D(block, upperPosition2D.z))
        }
        return blocks.toTypedArray()
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getBottomEdgeBlockPositions(): ArrayList<Position2D> {
        val blocks : ArrayList<Position2D> = ArrayList()
        for (block in lowerPosition2D.x..upperPosition2D.x) {
            blocks.add(Position2D(block, lowerPosition2D.z))
        }
        return blocks
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getLeftEdgeBlockPositions(): ArrayList<Position2D> {
        val blocks : ArrayList<Position2D> = ArrayList()
        for (block in lowerPosition2D.z..upperPosition2D.z) {
            blocks.add(Position2D(lowerPosition2D.x, block))
        }
        return blocks
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getRightEdgeBlockPositions(): ArrayList<Position2D> {
        val blocks : ArrayList<Position2D> = ArrayList()
        for (block in lowerPosition2D.z..upperPosition2D.z) {
            blocks.add(Position2D(upperPosition2D.x, block))
        }
        return blocks
    }

    /**
     * Sorts the position sizes to ensure that the upper position contains values larger than the lower position.
     */
    protected fun sortPositionSizes() {
        if (lowerPosition2D.x > upperPosition2D.x) {
            val newLowerPosition2D = Position2D(upperPosition2D.x, lowerPosition2D.z)
            val newUpperPosition2D = Position2D(lowerPosition2D.x, upperPosition2D.z)
            lowerPosition2D = newLowerPosition2D
            upperPosition2D = newUpperPosition2D
        }

        if (lowerPosition2D.z > upperPosition2D.z) {
            val newLowerPosition2D = Position2D(lowerPosition2D.x, upperPosition2D.z)
            val newUpperPosition2D = Position2D(upperPosition2D.x, lowerPosition2D.z)
            lowerPosition2D = newLowerPosition2D
            upperPosition2D = newUpperPosition2D
        }
    }

    class Builder(val firstPosition: Position2D) {
        var secondPosition: Position2D? = null

        fun build(): Area? {
            if (secondPosition == null) {
                return null
            }
            return Area(firstPosition, secondPosition!!)
        }
    }
}