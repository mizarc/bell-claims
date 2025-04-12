package dev.mizarc.bellclaims.application.services.old

import dev.mizarc.bellclaims.application.results.FlagChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Flag

/**
 * A service that handles the modification of flags for a claim, which handles game protection behaviour.
 */
interface FlagService {
    /**
     * Checks if the given claim has the specified flag.
     *
     * @param claim The claim to query.
     * @param flag The flag to check for.
     * @return True if the claim contains the flag.
     */
    fun doesClaimHaveFlag(claim: Claim, flag: Flag): Boolean

    /**
     * Gets the set of flags associated with the specified claim.
     *
     * @param claim The claim to retrieve permissions from.
     * @return The Set of permissions attached to the claim.
     */
    fun getByClaim(claim: Claim): Set<Flag>

    /**
     * Adds a flag to a given claim.
     *
     * @param claim The claim to add to.
     * @param flag The flag to be added.
     * @return The result of adding the flag to the claim.
     */
    fun add(claim: Claim, flag: Flag): FlagChangeResult

    /**
     * Adds all available flags to a given claim.
     *
     * @param claim The claim to add to.
     * @return The result of adding all flags to the claim.
     */
    fun addAll(claim: Claim): FlagChangeResult

    /**
     * Removes a flag from a given claim.
     *
     * @param claim The claim to remove from.
     * @param flag The flag to be removed.
     * @return The result of removing the flag from the claim.
     */
    fun remove(claim: Claim, flag: Flag): FlagChangeResult

    /**
     * Remove all flags from the claim.
     *
     * @param claim The claim to remove from.
     * @return The result of removing all flags from the claim.
     */
    fun removeAll(claim: Claim): FlagChangeResult
}