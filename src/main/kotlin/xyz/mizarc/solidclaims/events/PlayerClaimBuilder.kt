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
class PlayerClaimBuilder(var playerId: UUID, var firstLocation: Location, var secondLocation: Location?) {
    /**
     * Creates a PlayerClaimBuilder object with only the first location value.
     * @param playerId The unique identifier of the player.
     * @param firstLocation The location of the first corner.
     */
    constructor(playerId: UUID, firstLocation: Location) : this(playerId, firstLocation, null)

    fun getBlockCount() : Int? {
        if (secondLocation == null) {
            return null
        }

        return ((secondLocation!!.x.toInt() - firstLocation.x.toInt() + 1) *
                (secondLocation!!.z.toInt() - firstLocation.z.toInt() + 1)).absoluteValue
    }

    fun getXLength() : Int {
        return (firstLocation.x - secondLocation!!.x).absoluteValue.toInt()
    }

    fun getZLength() : Int {
        return (firstLocation.z - secondLocation!!.z).absoluteValue.toInt()
    }
}