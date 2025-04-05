package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.enums.AddFlagToClaimResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Action for adding a specific flag to a claim.
 *
 * @property flagRepository Repository for managing claim flags.
 * @property claimRepository Repository for managing claims.
 */
class AddFlagToClaim(private val flagRepository: ClaimFlagRepository, private val claimRepository: ClaimRepository) {

    /**
     * Add the specified [flag] to the claim with the given [claimId].
     *
     * @param flag The [Flag] to be added to the claim.
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [AddFlagToClaimResult] indicating the outcome of the flag addition operation.
     */
    fun execute(flag: Flag, claimId: UUID): AddFlagToClaimResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return AddFlagToClaimResult.ClaimNotFound

        // Add the flag to the claim
        return try {
            when (flagRepository.add(claimId, flag)) {
                true -> AddFlagToClaimResult.Success
                false -> AddFlagToClaimResult.AlreadyExists
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            AddFlagToClaimResult.StorageError
        }
    }
}