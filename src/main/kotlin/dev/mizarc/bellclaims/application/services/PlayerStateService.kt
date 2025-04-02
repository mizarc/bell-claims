package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.application.enums.PlayerRegisterResult
import dev.mizarc.bellclaims.application.enums.PlayerUnregisterResult
import dev.mizarc.bellclaims.domain.entities.PlayerState
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

/**
 * A service that handles the temporary state of player's on the server pertaining to claim management.
 */
interface PlayerStateService {
    /**
     * Checks if the given player is registered to hold state data.
     *
     * @param player The player to query.
     * @return True if the player is registered.
     */
    fun isPlayerRegistered(player: OfflinePlayer): Boolean

    /**
     * Gets all players that are currently registered.
     *
     * @return The set of all players that are registered.
     */
    fun getAllOnline(): Set<PlayerState>

    /**
     * Gets a player's state by a given id.
     *
     * @param id The unique id of the player.
     * @return The player's state data.
     */
    fun getById(id: UUID): PlayerState?

    /**
     * Gets a player's state by a given player instance.
     *
     * @param player The in-game player instance.
     * @return The player's state data.
     */
    fun getByPlayer(player: OfflinePlayer): PlayerState?

    /**
     * Registers the player to hold state data.
     *
     * @param player The player to register.
     * @return The result of registering the player.
     */
    fun registerPlayer(player: Player): PlayerRegisterResult

    /**
     * Unregisters the player to remove their state data.
     *
     * @param player The player to unregister.
     * @return The result of unregistering the player.
     */
    fun unregisterPlayer(player: OfflinePlayer): PlayerUnregisterResult
}