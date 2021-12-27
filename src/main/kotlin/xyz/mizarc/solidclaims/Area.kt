package xyz.mizarc.solidclaims

import kotlin.math.absoluteValue

/**
 * Stores two positions that define the corners of an area.
 * @property lowerPosition The lower corner position.
 * @property upperPosition The upper corner position.
 */
class Area(var lowerPosition: Position, var upperPosition: Position) {
    init {
        sortPositionSizes()
    }

    /**
     * Gets the total block count of the area.
     */
    fun getBlockCount(): Int {
        return ((upperPosition.x - lowerPosition.x + 1) * (upperPosition.z - lowerPosition.z + 1)).absoluteValue
    }

    /**
     * Gets the length of the X axis.
     */
    fun getXLength(): Int {
        return (lowerPosition.x - upperPosition.x).absoluteValue
    }

    /**
     * Gets the length of the Z axis
     */
    fun getZLength(): Int {
        return (lowerPosition.z - upperPosition.z).absoluteValue
    }

    /**
     * Gets the list of X and Z block positions that define the edges of an area.
     * @return An array of position objects
     */
    fun getEdgeBlockPositions(): Array<Position> {
        val blocks : ArrayList<Position> = ArrayList()
        for (block in lowerPosition.x..upperPosition.x) {
            blocks.add(Position(block, lowerPosition.z))
            blocks.add(Position(block, upperPosition.z))
        }
        for (block in lowerPosition.z..upperPosition.z) {
            blocks.add(Position(lowerPosition.x, block))
            blocks.add(Position(upperPosition.x, block))
        }
        return blocks.toTypedArray()
    }

    /**
     * Gets the positions of blocks that define the top edge of the area.
     * @return An array of position objects
     */
    fun getTopEdgeBlockPositions(): Array<Position> {
        val blocks : ArrayList<Position> = ArrayList()
        for (block in lowerPosition.x..upperPosition.x) {
            blocks.add(Position(block, upperPosition.z))
        }
        return blocks.toTypedArray()
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getBottomEdgeBlockPositions(): ArrayList<Position> {
        val blocks : ArrayList<Position> = ArrayList()
        for (block in lowerPosition.x..upperPosition.x) {
            blocks.add(Position(block, lowerPosition.z))
        }
        return blocks
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getLeftEdgeBlockPositions(): ArrayList<Position> {
        val blocks : ArrayList<Position> = ArrayList()
        for (block in lowerPosition.z..upperPosition.z) {
            blocks.add(Position(lowerPosition.x, block))
        }
        return blocks
    }

    /**
     * Gets the positions of blocks that define the bottom edge of the area.
     * @return An array of position objects
     */
    fun getRightEdgeBlockPositions(): ArrayList<Position> {
        val blocks : ArrayList<Position> = ArrayList()
        for (block in lowerPosition.z..upperPosition.z) {
            blocks.add(Position(upperPosition.x, block))
        }
        return blocks
    }

    /**
     * Sorts the position sizes to ensure that the upper position contains values larger than the lower position.
     */
    private fun sortPositionSizes() {
        if (lowerPosition.x > upperPosition.x) {
            val newLowerPosition = Position(upperPosition.x, lowerPosition.z)
            val newUpperPosition = Position(lowerPosition.x, upperPosition.z)
            lowerPosition = newLowerPosition
            upperPosition = newUpperPosition
        }

        if (lowerPosition.z > upperPosition.z) {
            val newLowerPosition = Position(lowerPosition.x, upperPosition.z)
            val newUpperPosition = Position(upperPosition.x, lowerPosition.z)
            lowerPosition = newLowerPosition
            upperPosition = newUpperPosition
        }
    }
}