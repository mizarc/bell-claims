package dev.mizarc.bellclaims.application.actions.claim.permission

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.permission.GrantAllClaimWidePermissionsResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class GrantAllClaimWidePermissions(private val claimPermissionRepository: ClaimPermissionRepository,
                                   private val claimRepository: ClaimRepository) {
    /**
     * Adds all available flags to the claim with the given [claimId].
     *
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [GrantAllClaimWidePermissionsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID): GrantAllClaimWidePermissionsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GrantAllClaimWidePermissionsResult.ClaimWideNotFound

        // Add all flags to the claim
        var anyPermissionEnabled = false
        try {
            val allPermissions = ClaimPermission.entries
            for (permission in allPermissions) {
                if (claimPermissionRepository.add(claimId, permission)) anyPermissionEnabled = true
            }

            // Return success if at least one permission was granted
            return if (anyPermissionEnabled) {
                GrantAllClaimWidePermissionsResult.Success
            } else {
                GrantAllClaimWidePermissionsResult.AllAlreadyGrantedWide
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return GrantAllClaimWidePermissionsResult.StorageError
        }
    }
}