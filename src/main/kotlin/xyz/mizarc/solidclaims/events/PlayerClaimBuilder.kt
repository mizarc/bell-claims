package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import java.util.*
import kotlin.collections.ArrayList

/**
 * Associates a player with two different locations to get the locations intended to make a claim out of
 * @property playerId The unique identifier of the player.
 * @property firstLocation The location of the first claim corner.
 * @property secondLocation The location of the second claim corner.
 */
class PlayerClaimBuilder(var playerId: UUID, var firstLocation: Location?, var secondLocation: Location?) {
    /**
     * Creates a PlayerClaimBuilder object with empty location values.
     * @param playerId The unique identifier of the player.
     */
    constructor(playerId: UUID) : this(playerId, null, null)
}