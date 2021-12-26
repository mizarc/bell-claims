package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property firstLocation The location of the first claim corner.
 * @property secondLocation The location of the second claim corner.
 */
class PlayerClaimBuilder(var playerId: UUID, var firstPosition: Pair<Int, Int>, var secondPosition: Pair<Int, Int>?) {
    /**
     * Creates a PlayerClaimBuilder object with only the first location value.
     * @param playerId The unique identifier of the player.
     * @param firstLocation The location of the first corner.
     */
    constructor(playerId: UUID, firstLocation: Pair<Int, Int>) : this(playerId, firstLocation, null)

    fun getBlockCount() : Int? {
        if (secondPosition == null) {
            return null
        }

        return ((secondPosition!!.first - firstPosition.first + 1) *
                (secondPosition!!.second - firstPosition.second + 1)).absoluteValue
    }

    fun getXLength() : Int {
        return (firstPosition.first - secondPosition!!.first).absoluteValue
    }

    fun getZLength() : Int {
        return (firstPosition.second - secondPosition!!.second).absoluteValue
    }

    /**
     * Gets the list of X and Z block positions that define the edges of a claim.
     * @return A set of Integer pairs specifying the X and Z coordinates of each position.
     */
    fun getEdgeBlockPositions() : Array<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.first..secondPosition!!.first) {
            blocks.add(Pair(block, firstPosition.second))
        }
        for (block in firstPosition.first..secondPosition!!.first) {
            blocks.add(Pair(block, secondPosition!!.second))
        }
        for (block in firstPosition.second..secondPosition!!.second) {
            blocks.add(Pair(firstPosition.first, block))
        }
        for (block in firstPosition.second..secondPosition!!.second) {
            blocks.add(Pair(secondPosition!!.first, block))
        }
        return blocks.toTypedArray()
    }

    fun getTopEdgeBlockPositions() : ArrayList<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.first..secondPosition!!.first) {
            println("oof")
            blocks.add(Pair(block, secondPosition!!.second))
        }
        println("ree")
        return blocks
    }

    fun getBottomEdgeBlockPositions() : ArrayList<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.first..secondPosition!!.first.toInt()) {
            blocks.add(Pair(block, firstPosition.second))
        }
        return blocks
    }

    fun getLeftEdgeBlockPositions() : ArrayList<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.second..secondPosition!!.second) {
            blocks.add(Pair(firstPosition.first, block))
        }
        return blocks
    }

    fun getRightEdgeBlockPositions() : ArrayList<Pair<Int, Int>> {
        val blocks : ArrayList<Pair<Int, Int>> = ArrayList()
        for (block in firstPosition.second..secondPosition!!.second) {
            blocks.add(Pair(secondPosition!!.first, block))
        }
        return blocks
    }

    fun sortPositionSizes() {
        if (firstPosition.first > secondPosition!!.first) {
            val newFirstPosition = Pair(secondPosition!!.first, firstPosition.second)
            val newSecondPosition = Pair(firstPosition.first, secondPosition!!.second)
            firstPosition = newFirstPosition
            secondPosition = newSecondPosition
        }

        if (firstPosition.second > secondPosition!!.second) {
            val newFirstPosition = Pair(firstPosition.first, secondPosition!!.second)
            val newSecondPosition = Pair(secondPosition!!.first, firstPosition.second)
            firstPosition = newFirstPosition
            secondPosition = newSecondPosition
        }
    }
}