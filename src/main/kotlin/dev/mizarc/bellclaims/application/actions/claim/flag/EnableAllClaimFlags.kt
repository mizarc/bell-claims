package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.flags.EnableAllClaimFlagsResult
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Action for adding a specific flag to a claim.
 *
 * @property flagRepository Repository for managing claim flags.
 * @property claimRepository Repository for managing claims.
 */
class EnableAllClaimFlags(private val flagRepository: ClaimFlagRepository,
                         private val claimRepository: ClaimRepository) {

    /**
     * Adds all available flags to the claim with the given [claimId].
     *
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [EnableAllClaimFlagsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID): EnableAllClaimFlagsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return EnableAllClaimFlagsResult.ClaimNotFound

        // Add all flags to the claim
        var anyFlagEnabled = false
        try {
            val allFlags = Flag.entries
            for (flag in allFlags) {
                if (flagRepository.add(claimId, flag)) anyFlagEnabled = true
            }

            // Return success if at least one flag was enabled
            return if (anyFlagEnabled) {
                EnableAllClaimFlagsResult.Success
            } else {
                EnableAllClaimFlagsResult.AllAlreadyEnabled
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return EnableAllClaimFlagsResult.StorageError
        }
    }
}