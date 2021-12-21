package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import xyz.mizarc.solidclaims.claims.Claim
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

    /**
     * Alternative constructor that omits the new corner position to be added later
     */
    constructor(playerId: UUID, claimPartition: ClaimPartition, selectedCorner: Pair<Int, Int>):
            this(playerId, claimPartition, selectedCorner, null)

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
            newFirstPosition = Pair(newFirstPosition.first, newLocation!!.z.toInt())
        } else {
            newFirstPosition = Pair(newFirstPosition.first, claimPartition.firstPosition.second)
        }

        if (selectedCorner.second == claimPartition.secondPosition.second) {
            newSecondPosition = Pair(newSecondPosition.first, newLocation!!.z.toInt())
        } else {
            newSecondPosition = Pair(newSecondPosition.first, claimPartition.secondPosition.second)
        }
        newClaimPartition = ClaimPartition(claimPartition.claim, newFirstPosition, newSecondPosition)
        newClaimPartition.sortPositionSizes()
        return newClaimPartition
    }

    fun extraBlockCount() : Int? {
        if (!::newFirstPosition.isInitialized || !::newSecondPosition.isInitialized) {
            return null
        }

        return ((newSecondPosition.first - newFirstPosition.first + 1) *
                (newSecondPosition.second - newFirstPosition.second + 1)).absoluteValue -
                ((claimPartition.secondPosition.first - claimPartition.firstPosition.first + 1) *
                        (claimPartition.secondPosition.second - claimPartition.firstPosition.second + 1))
                    .absoluteValue
    }

    fun getXLength() : Int {
        return (newFirstPosition.first - newSecondPosition.first).absoluteValue
    }

    fun getZLength() : Int {
        return (newFirstPosition.second - newSecondPosition.second).absoluteValue.toInt()
    }
}