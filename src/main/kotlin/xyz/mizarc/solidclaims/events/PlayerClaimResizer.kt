package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import xyz.mizarc.solidclaims.claims.ClaimPartition
import java.util.*
import kotlin.math.absoluteValue

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property claimPartition The partition to resize
 */
class PlayerClaimResizer(var playerId: UUID, var claimPartition: ClaimPartition, var selectedCorner: Pair<Int, Int>,
                        var newLocation: Location?) {
    lateinit var newFirstPosition: Pair<Int, Int>
    lateinit var newSecondPosition: Pair<Int, Int>
    lateinit var newClaimPartition: ClaimPartition

    fun setNewCorner() : ClaimPartition {
        if (selectedCorner.first == claimPartition.firstPosition.first) {
            newFirstPosition = Pair(newLocation!!.x.toInt(), 0)
        } else {
            newFirstPosition = Pair(claimPartition.firstPosition.first, 0)
        }

        if (selectedCorner.first == claimPartition.secondPosition.first) {
            newSecondPosition = Pair(newLocation!!.x.toInt(), 0)
        } else {
            newSecondPosition = Pair(claimPartition.secondPosition.first, 0)
        }

        if (selectedCorner.second == claimPartition.firstPosition.second) {
            newFirstPosition = Pair(newFirstPosition.first, newLocation!!.x.toInt())
        } else {
            newFirstPosition = Pair(newFirstPosition.first, claimPartition.firstPosition.second)
        }

        if (selectedCorner.second == claimPartition.secondPosition.second) {
            newSecondPosition = Pair(newFirstPosition.first, newLocation!!.x.toInt())
        } else {
            newSecondPosition = Pair(newFirstPosition.first, claimPartition.secondPosition.second)
        }
        newClaimPartition.firstPosition = newFirstPosition
        newClaimPartition.secondPosition = newSecondPosition
        newClaimPartition.sortPositionSizes()
        return newClaimPartition
    }

    fun extraBlockCount() : Int? {
        if (::newFirstPosition.isInitialized || ::newSecondPosition.isInitialized) {
            return null
        }

        return ((newSecondPosition.first - newFirstPosition.first + 1) *
                (newSecondPosition.second - newFirstPosition.second + 1)).absoluteValue
    }
}