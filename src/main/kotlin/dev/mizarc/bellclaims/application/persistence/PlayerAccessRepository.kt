package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.*

/**
 * A repository that handles the persistence of permissions for specific players in a claim.
 */
interface PlayerAccessRepository {

    /**
     * Gets all player permissions linked to a given claim.
     *
     * @param claimId The id of the claim to query.
     * @return The map of permissions linked to each player.
     */
    fun getByClaim(claimId: UUID): Map<UUID, Set<ClaimPermission>>

    /**
     * Gets the permission that a given player has access to in a claim.
     *
     * @param claimId The id of the claim to query.
     * @param playerId The id of the player to query.
     * @return The set of permissions that the player has access to.
     */
    fun getByPlayer(claimId: UUID, playerId: UUID): Set<ClaimPermission>

    /**
     * Adds a permission to a given player in a claim.
     *
     * @param claimId The target claim.
     * @param playerId The player to give the permission to.
     * @param permission The permission to add.
     */
    fun add(claimId: UUID, playerId: UUID, permission: ClaimPermission): Boolean

    /**
     * Removes a permission from a given player in a claim.
     *
     * @param claimId The target claim.
     * @param playerId The player to remove the permission from.
     * @param permission The permission to remove.
     */
    fun remove(claimId: UUID, playerId: UUID, permission: ClaimPermission): Boolean

    /**
     * Removes all permission from a given player in a claim.
     *
     * @param claimId The target claim's id.
     * @param playerId The id of the player to remove permissions from.
     */
    fun removeByPlayer(claimId: UUID, playerId: UUID): Boolean

    /**
     * Removes all player permissions from a given claim.
     *
     * @param claimId The id of the claim to remove permissions from.
     */
    fun removeByClaim(claimId: UUID): Boolean
}