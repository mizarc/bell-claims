package xyz.mizarc.solidclaims.events

import xyz.mizarc.solidclaims.claims.Area
import xyz.mizarc.solidclaims.claims.Position
import java.util.*

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property firstPosition The location of the first claim corner.
 * @property secondPosition The location of the second claim corner.
 */
class AreaBuilder(var playerId: UUID, var firstPosition: Position, var secondPosition: Position?) {
    /**
     * Creates a PlayerClaimBuilder object with only the first location value.
     * @param playerId The unique identifier of the player.
     * @param firstLocation The location of the first corner.
     */
    constructor(playerId: UUID, firstLocation: Position) : this(playerId, firstLocation, null)

    /**
     * Create an Area object.
     * @return Area object.
     */
    fun build(): Area? {
        if (secondPosition == null) {
            return null
        }
        return Area(firstPosition, secondPosition!!)
    }
}