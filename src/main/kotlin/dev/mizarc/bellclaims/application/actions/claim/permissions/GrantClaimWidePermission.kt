package dev.mizarc.bellclaims.application.actions.claim.permissions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.GrantClaimWidePermissionResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class GrantClaimWidePermission(private val claimPermissionRepository: ClaimPermissionRepository,
                               private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID, permission: ClaimPermission): GrantClaimWidePermissionResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GrantClaimWidePermissionResult.ClaimNotFound

        // Add the permission for the player in the claim
        try {
            return when (claimPermissionRepository.add(claimId, permission)) {
                true -> GrantClaimWidePermissionResult.Success
                false -> GrantClaimWidePermissionResult.AlreadyExists
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return GrantClaimWidePermissionResult.StorageError
        }
    }
}