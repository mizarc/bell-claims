package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.results.claim.flags.EnableAllClaimFlagsResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.flags.EnableClaimFlagResult
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Action for adding a specific flag to a claim.
 *
 * @property flagRepository Repository for managing claim flags.
 * @property claimRepository Repository for managing claims.
 */
class EnableClaimFlag(private val flagRepository: ClaimFlagRepository, private val claimRepository: ClaimRepository) {

    /**
     * Add the specified [flag] to the claim with the given [claimId].
     *
     * @param flag The [Flag] to be added to the claim.
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [EnableAllClaimFlagsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(flag: Flag, claimId: UUID): EnableClaimFlagResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return EnableClaimFlagResult.ClaimNotFound

        // Add the flag to the claim
        try {
            return when (flagRepository.add(claimId, flag)) {
                true -> EnableClaimFlagResult.Success
                false -> EnableClaimFlagResult.AlreadyExists
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.cause}")
            return EnableClaimFlagResult.StorageError
        }
    }
}