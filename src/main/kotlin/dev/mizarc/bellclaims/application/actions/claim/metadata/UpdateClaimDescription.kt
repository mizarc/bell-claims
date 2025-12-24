package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimDescriptionResult
import java.util.UUID

class UpdateClaimDescription(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, description: String): UpdateClaimDescriptionResult {
        // Check if the claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimDescriptionResult.ClaimNotFound

        // Change description and persist in storage
        val newClaim = existingClaim.copy(description = description)
        try {
            claimRepository.update(newClaim)
            return UpdateClaimDescriptionResult.Success(newClaim)
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimDescriptionResult.StorageError
        }
    }
}