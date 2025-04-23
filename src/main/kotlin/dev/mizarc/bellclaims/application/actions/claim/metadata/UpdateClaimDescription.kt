package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimAttributeResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import java.util.UUID

class UpdateClaimDescription(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, description: String): UpdateClaimAttributeResult {
        // Check if claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimAttributeResult.ClaimNotFound

        // Change description and persist to storage
        val newClaim = existingClaim.copy(description = description)
        try {
            claimRepository.update(newClaim)
            return UpdateClaimAttributeResult.Success(newClaim)
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimAttributeResult.StorageError
        }
    }
}