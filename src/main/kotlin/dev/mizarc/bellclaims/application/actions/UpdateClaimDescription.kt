package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.results.UpdateClaimAttributeResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import java.util.UUID

class UpdateClaimDescription(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, description: String): UpdateClaimAttributeResult {
        // Check if claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimAttributeResult.ClaimNotFound

        // Change description and persist to storage
        existingClaim.description = description
        try {
            claimRepository.update(existingClaim)
            return UpdateClaimAttributeResult.Success
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimAttributeResult.StorageError
        }
    }
}