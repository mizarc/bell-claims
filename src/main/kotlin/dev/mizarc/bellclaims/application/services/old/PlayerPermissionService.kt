package dev.mizarc.bellclaims.application.services.old

import dev.mizarc.bellclaims.application.results.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.bukkit.OfflinePlayer

/**
 * A service that handles the modification of specific player permissions for a claim.
 */
interface PlayerPermissionService {
    /**
     * Checks whether the specified player has the given permission in the claim.
     *
     * @param claim The claim to check permissions for.
     * @param player The player to check permissions for.
     * @param permission The permission to check.
     * @return True if the player has the permission.
     */
    fun doesPlayerHavePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): Boolean

    /**
     * Gets a map of each player and their associated permissions for the specified claim.
     *
     * @param claim The claim to retrieve permissions from.
     * @return The map of players and their associated permissions.
     */
    fun getByClaim(claim: Claim): Map<OfflinePlayer, Set<ClaimPermission>>

    /**
     * Gets the permissions that the specified player has in the given claim.
     *
     * @param claim The claim to get the player's permissions from.
     * @param player The player to get permissions for.
     * @return The set of permissions for the player.
     */
    fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission>

    /**
     * Adds a permission for a player in the given claim.
     *
     * @param claim The claim to add player's permissions to.
     * @param player The player to add the permission for.
     * @return The result of giving the player the given permission for the claim.
     */
    fun addForPlayer(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): PlayerPermissionChangeResult

    /**
     * Adds all available permissions for a player in the given claim.
     *
     * @param claim The claim to add player's permissions to.
     * @param player The player to add permissions for.
     * @return The result of giving the player the given permission for the claim.
     */
    fun addAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult

    /**
     * Removes a permission for a player in the given claim.
     *
     * @param claim The claim to remove player's permissions from.
     * @param player The player to remove the permission from.
     * @return The result of removing the given permission for the player in the claim.
     */
    fun removeForPlayer(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): PlayerPermissionChangeResult

    /**
     * Removes all permissions for a player in the given claim.
     *
     * @param claim The claim to remove player's permissions from.
     * @param player The player to remove permissions from.
     * @return The result of removing all permissions for the player in the claim.
     */
    fun removeAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult
}