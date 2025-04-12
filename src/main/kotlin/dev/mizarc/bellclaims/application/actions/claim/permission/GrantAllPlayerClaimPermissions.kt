package dev.mizarc.bellclaims.application.actions.claim.permission

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.permission.GrantAllClaimPermissionsResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class GrantAllPlayerClaimPermissions(private val claimPermissionRepository: ClaimPermissionRepository,
                                     private val claimRepository: ClaimRepository) {
    /**
     * Adds all available flags to the claim with the given [claimId].
     *
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [GrantAllClaimPermissionsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID): GrantAllClaimPermissionsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GrantAllClaimPermissionsResult.ClaimNotFound

        // Add all flags to the claim
        var anyPermissionEnabled = false
        try {
            val allPermissions = ClaimPermission.entries
            for (permission in allPermissions) {
                if (claimPermissionRepository.add(claimId, permission)) anyPermissionEnabled = true
            }

            // Return success if at least one permission was granted
            return if (anyPermissionEnabled) {
                GrantAllClaimPermissionsResult.Success
            } else {
                GrantAllClaimPermissionsResult.AllAlreadyGranted
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return GrantAllClaimPermissionsResult.StorageError
        }
    }
}