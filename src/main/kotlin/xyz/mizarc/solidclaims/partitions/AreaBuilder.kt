package xyz.mizarc.solidclaims.partitions

import java.util.*

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property firstPosition2D The location of the first claim corner.
 * @property secondPosition2D The location of the second claim corner.
 */
class AreaBuilder(var playerId: UUID, var firstPosition2D: Position2D, var secondPosition2D: Position2D?) {
    /**
     * Creates a PlayerClaimBuilder object with only the first location value.
     * @param playerId The unique identifier of the player.
     * @param firstLocation The location of the first corner.
     */
    constructor(playerId: UUID, firstLocation: Position2D) : this(playerId, firstLocation, null)

    /**
     * Create an Area object.
     * @return Area object.
     */
    fun build(): Area? {
        if (secondPosition2D == null) {
            return null
        }
        return Area(firstPosition2D, secondPosition2D!!)
    }
}