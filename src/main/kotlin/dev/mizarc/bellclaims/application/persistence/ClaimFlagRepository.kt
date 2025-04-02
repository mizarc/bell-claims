package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.entities.Flag

/**
 * A repository that handles the persistence of claim flags.
 */
interface ClaimFlagRepository {
    /**
     * Gets all flags linked to a given claim.
     *
     * @param claim The claim to query.
     * @return The set of flags the claim has.
     */
    fun getByClaim(claim: Claim): Set<Flag>

    /**
     * Adds a flag to a given claim.
     *
     * @param claim The clam to add the flag to.
     * @param flag The flag to add.
     */
    fun add(claim: Claim, flag: Flag)

    /**
     * Removes an existing flag from a given claim.
     *
     * @param claim The claim to remove the flag from.
     * @param flag The flag to remove.
     */
    fun remove(claim: Claim, flag: Flag)

    /**
     * Removes all flags attached to a given claim.
     *
     * @param claim The claim to remove the flags from.
     */
    fun removeByClaim(claim: Claim)
}