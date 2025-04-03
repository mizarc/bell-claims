package dev.mizarc.bellclaims.application.persistence

import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * A repository that handles the persistence of claim flags.
 */
interface ClaimFlagRepository {
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
     */
    fun add(claimId: UUID, flag: Flag)

    /**
     * Removes an existing flag from a given claim.
     *
     * @param claimId The id of the  claim to remove the flag from.
     * @param flag The flag to remove.
     */
    fun remove(claimId: UUID, flag: Flag)

    /**
     * Removes all flags attached to a given claim.
     *
     * @param claimId The id of the claim to remove the flags from.
     */
    fun removeByClaim(claimId: UUID)
}