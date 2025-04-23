package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * A repository that handles the persistence of claim flags.
 */
interface ClaimFlagRepository {
    /**
     * Checks if claim has an associated flag.
     *
     * @param claimId The id of the claim to query.
     * @return True if claim has a flag, false if not.
     */
    fun doesClaimHaveFlag(claimId: UUID, flag: Flag): Boolean

    /**
     * Gets all flags linked to a given claim.
     *
     * @param claimId The id of the claim to query.
     * @return The set of flags the claim has.
     */
    fun getByClaim(claimId: UUID): Set<Flag>

    /**
     * Adds a flag to a given claim.
     *
     * @param claimId The id of the clam to add the flag to.
     * @param flag The flag to add.
     * @return True if successful, false if duplicate
     */
    fun add(claimId: UUID, flag: Flag): Boolean

    /**
     * Removes an existing flag from a given claim.
     *
     * @param claimId The id of the  claim to remove the flag from.
     * @param flag The flag to remove.
     * @return True if successful, false if entry doesn't exist.
     */
    fun remove(claimId: UUID, flag: Flag): Boolean

    /**
     * Removes all flags attached to a given claim.
     *
     * @param claimId The id of the claim to remove the flags from.
     * @return True if successful, false if no entries exist.
     */
    fun removeByClaim(claimId: UUID): Boolean
}