package xyz.mizarc.solidclaims.events

import xyz.mizarc.solidclaims.Area
import xyz.mizarc.solidclaims.Position
import xyz.mizarc.solidclaims.claims.ClaimPartition
import java.util.*
import kotlin.math.absoluteValue

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property claimPartition The partition to resize.
 * @property selectedCorner The existing corner of the existing partition.
 */
class PartitionResizeBuilder(var playerId: UUID, var claimPartition: ClaimPartition, var selectedCorner: Position) {
    lateinit var newLowerPosition: Position
    lateinit var newUpperPosition: Position

    /**
     * Specifies the new corner to replace an existing corner.
     * @param newPosition The new position to set the corner.
     */
    fun setNewCorner(newPosition: Position) {
        newLowerPosition = if (selectedCorner.x == claimPartition.area.lowerPosition.x) {
            Position(newPosition.x, 0)
        } else {
            Position(claimPartition.area.lowerPosition.x, 0)
        }

        newUpperPosition = if (selectedCorner.x == claimPartition.area.upperPosition.x) {
            Position(newPosition.x, 0)
        } else {
            Position(claimPartition.area.upperPosition.x, 0)
        }

        newLowerPosition = if (selectedCorner.z == claimPartition.area.lowerPosition.z) {
            Position(newLowerPosition.x, newPosition.z)
        } else {
            Position(newLowerPosition.x, claimPartition.area.lowerPosition.z)
        }

        newUpperPosition = if (selectedCorner.z == claimPartition.area.upperPosition.z) {
            Position(newUpperPosition.x, newPosition.z)
        } else {
            Position(newUpperPosition.x, claimPartition.area.upperPosition.z)
        }
    }

    fun build(): ClaimPartition {
        return ClaimPartition(claimPartition.claim, Area(newLowerPosition, newUpperPosition))
    }

    fun extraBlockCount() : Int? {
        if (!::newLowerPosition.isInitialized || !::newUpperPosition.isInitialized) {
            return null
        }

        return ((newUpperPosition.x - newLowerPosition.x + 1) *
                (newUpperPosition.z - newLowerPosition.z + 1)).absoluteValue -
                ((claimPartition.area.upperPosition.x - claimPartition.area.upperPosition.x + 1)
                        * (claimPartition.area.upperPosition.z - claimPartition.area.upperPosition.z+ 1)).absoluteValue
    }
}