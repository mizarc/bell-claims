package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimNameResult
import java.util.UUID

class UpdateClaimName(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, name: String): UpdateClaimNameResult {
        // Check if claim exists
        val existingClaim = claimRepository.getById(claimId) ?: return UpdateClaimNameResult.ClaimNotFound

        // Check if name already exists in player's list of claims
        if (claimRepository.getByName(existingClaim.playerId, name) != null)
            return UpdateClaimNameResult.NameAlreadyExists

        // Change Name and persist to storage
        existingClaim.name = name
        try {
            claimRepository.update(existingClaim)
            return UpdateClaimNameResult.Success
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return UpdateClaimNameResult.StorageError
        }
    }
}