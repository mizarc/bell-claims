package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.results.UpdateClaimAttributeResult
import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import java.util.UUID

class UpdateClaimName(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, name: String): UpdateClaimAttributeResult {
        // Check if claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimAttributeResult.ClaimNotFound

        // Change Name and persist to storage
        existingClaim.name = name
        try {
            claimRepository.update(existingClaim)
            return UpdateClaimAttributeResult.Success
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimAttributeResult.StorageError
        }
    }
}