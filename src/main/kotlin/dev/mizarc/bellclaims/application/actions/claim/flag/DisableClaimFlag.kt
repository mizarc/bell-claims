package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.enums.DisableClaimFlagResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Action for removing a specific flag from a claim.
 *
 * @property flagRepository Repository for managing claim flags.
 * @property claimRepository Repository for managing claims.
 */
class DisableClaimFlag(private val flagRepository: ClaimFlagRepository, private val claimRepository: ClaimRepository) {

    /**
     * Removes the specified [flag] from the claim with the given [claimId].
     *
     * @param flag The [Flag] to be added to the claim.
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [DisableClaimFlagResult] indicating the outcome of the flag addition operation.
     */
    fun execute(flag: Flag, claimId: UUID): DisableClaimFlagResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return DisableClaimFlagResult.ClaimNotFound

        // Add the flag to the claim
        try {
            return when (flagRepository.remove(claimId, flag)) {
                true -> DisableClaimFlagResult.Success
                false -> DisableClaimFlagResult.DoesNotExist
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return DisableClaimFlagResult.StorageError
        }
    }
}