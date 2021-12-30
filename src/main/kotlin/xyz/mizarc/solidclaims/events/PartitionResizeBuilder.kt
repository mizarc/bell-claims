package xyz.mizarc.solidclaims.events

import xyz.mizarc.solidclaims.claims.Area
import xyz.mizarc.solidclaims.claims.Position
import xyz.mizarc.solidclaims.claims.Partition
import java.util.*
import kotlin.math.absoluteValue

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property partition The partition to resize.
 * @property selectedCorner The existing corner of the existing partition.
 */
class PartitionResizeBuilder(var playerId: UUID, var partition: Partition, var selectedCorner: Position) {
    lateinit var newLowerPosition: Position
    lateinit var newUpperPosition: Position

    /**
     * Specifies the new corner to replace an existing corner.
     * @param newPosition The new position to set the corner.
     */
    fun setNewCorner(newPosition: Position) {
        newLowerPosition = if (selectedCorner.x == partition.area.lowerPosition.x) {
            Position(newPosition.x, 0)
        } else {
            Position(partition.area.lowerPosition.x, 0)
        }

        newUpperPosition = if (selectedCorner.x == partition.area.upperPosition.x) {
            Position(newPosition.x, 0)
        } else {
            Position(partition.area.upperPosition.x, 0)
        }

        newLowerPosition = if (selectedCorner.z == partition.area.lowerPosition.z) {
            Position(newLowerPosition.x, newPosition.z)
        } else {
            Position(newLowerPosition.x, partition.area.lowerPosition.z)
        }

        newUpperPosition = if (selectedCorner.z == partition.area.upperPosition.z) {
            Position(newUpperPosition.x, newPosition.z)
        } else {
            Position(newUpperPosition.x, partition.area.upperPosition.z)
        }
    }

    fun build(): Partition {
        return Partition(partition.claim, Area(newLowerPosition, newUpperPosition))
    }

    fun extraBlockCount() : Int? {
        if (!::newLowerPosition.isInitialized || !::newUpperPosition.isInitialized) {
            return null
        }

        return ((newUpperPosition.x - newLowerPosition.x + 1) *
                (newUpperPosition.z - newLowerPosition.z + 1)).absoluteValue -
                ((partition.area.upperPosition.x - partition.area.upperPosition.x + 1)
                        * (partition.area.upperPosition.z - partition.area.upperPosition.z+ 1)).absoluteValue
    }
}