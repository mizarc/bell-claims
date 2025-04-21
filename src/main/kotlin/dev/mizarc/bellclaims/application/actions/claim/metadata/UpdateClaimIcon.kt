package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimIconResult
import java.util.UUID

class UpdateClaimIcon(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, materialName: String): UpdateClaimIconResult {
        // Check if claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimIconResult.NoClaimFound

        // Change icon and persist to storage
        val newClaim = existingClaim.copy(icon = materialName)
        try {
            claimRepository.update(newClaim)
            return UpdateClaimIconResult.Success
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimIconResult.StorageError
        }
    }
}