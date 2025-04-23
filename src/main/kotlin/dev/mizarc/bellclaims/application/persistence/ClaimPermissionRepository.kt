package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

/**
 * A repository that handles the persistence of claim permissions.
 */
interface ClaimPermissionRepository {
    /**
     * Checks whether the given claim contains a permission.
     *
     * @param claimId The id of the claim to query.
     * @param permission The permission to check for.
     * @return True if the claim has the permission.
     */
    fun doesClaimHavePermission(claimId: UUID, permission: ClaimPermission): Boolean

    /**
     * Gets all default permissions linked to a given claim.
     *
     * @param claimId The id of the claim to query.
     * @return The set of all default permissions.
     */
    fun getByClaim(claimId: UUID): Set<ClaimPermission>

    /**
     * Adds a new permission to a claim.
     *
     * @param claimId The id of the claim to add to.
     * @param permission The permission to add.
     */
    fun add(claimId: UUID, permission: ClaimPermission): Boolean

    /**
     * Removes an existing permission from a claim.
     *
     * @param claimId The id of the claim to remove from.
     * @param permission The permission to remove.
     */
    fun remove(claimId: UUID, permission: ClaimPermission): Boolean

    /**
     * Removes all permissions linked to a claim.
     *
     * @param claimId The id of the claim to remove from.
     */
    fun removeByClaim(claimId: UUID): Boolean
}