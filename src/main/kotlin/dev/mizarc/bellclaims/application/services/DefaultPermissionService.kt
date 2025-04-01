package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.application.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission

/**
 * A service that handles the modification of default permissions for a claim, allowing access for all players.
 */
interface DefaultPermissionService {
    /**
     * Gets the set of permissions associated with a claim.
     *
     * @param claim The claim to retrieve permissions from.
     * @return The Set of permissions attached to the claim.
     */
    fun getByClaim(claim: Claim): Set<ClaimPermission>

    /**
     * Adds the specified permission to the given claim.
     *
     * @param claim The claim to add the permission to.
     * @param permission The permission to be added to the claim.
     * @return The result of adding the permission to the claim.
     */
    fun add(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult

    /**
     * Adds all available permissions to the given claim.
     *
     * @param claim The claim to add the permissions to.
     * @return The result of adding all available permissions to the claim.
     */
    fun addAll(claim: Claim): DefaultPermissionChangeResult

    /**
     * Removes the specified permission from the given claim.
     *
     * @param claim The claim to remove the permission from.
     * @param permission The permission to be removed from the claim.
     * @return The result of removing the permission from the claim.
     */
    fun remove(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult

    /**
     * Removes all permissions from the given claim.
     *
     * @param claim The claim to remove the permissions from.
     * @return The result of removing all permissions from the claim.
     */
    fun removeAll(claim: Claim): DefaultPermissionChangeResult
}