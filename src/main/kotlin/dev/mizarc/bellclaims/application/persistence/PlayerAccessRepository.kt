package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * A repository that handles the persistence of permissions for specific players in a claim.
 */
interface PlayerAccessRepository {

    /**
     * Gets all player permissions linked to a given claim.
     *
     * @param claim The claim to query.
     * @return The map of permissions linked to each player.
     */
    fun getByClaim(claim: Claim): Map<UUID, Set<ClaimPermission>>

    /**
     * Gets the permission that a given player has access to in a claim.
     *
     * @param claim The claim to query.
     * @param player The player to query.
     * @return The set of permissions that the player has access to.
     */
    fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission>

    /**
     * Adds a permission to a given player in a claim.
     *
     * @param claim The target claim.
     * @param player The player to give the permission to.
     * @param permission The permission to add.
     */
    fun add(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)

    /**
     * Removes a permission from a given player in a claim.
     *
     * @param claim The target claim.
     * @param player The player to remove the permission from.
     * @param permission The permission to remove.
     */
    fun remove(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)

    /**
     * Removes all permission from a given player in a claim.
     *
     * @param claim The target claim.
     * @param player The player to remove permissions from.
     */
    fun removeByPlayer(claim: Claim, player: OfflinePlayer)

    /**
     * Removes all player permissions from a given claim.
     *
     * @param claim The claim to remove permissions from.
     */
    fun removeByClaim(claim: Claim)
}