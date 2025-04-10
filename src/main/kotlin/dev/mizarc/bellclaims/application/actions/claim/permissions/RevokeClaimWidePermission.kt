package dev.mizarc.bellclaims.application.actions.claim.permissions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.RevokeClaimWidePermissionResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class RevokeClaimWidePermission(private val claimPermissionRepository: ClaimPermissionRepository,
                                private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, permission: ClaimPermission): RevokeClaimWidePermissionResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return RevokeClaimWidePermissionResult.ClaimNotFound

        // Remove the permission for the player in the claim
        try {
            return when (claimPermissionRepository.remove(claimId, permission)) {
                true -> RevokeClaimWidePermissionResult.Success
                false -> RevokeClaimWidePermissionResult.DoesNotExist
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return RevokeClaimWidePermissionResult.StorageError
        }
    }
}