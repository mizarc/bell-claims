package dev.mizarc.bellclaims.application.actions.claim.permission

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.claim.permission.GrantAllPlayerClaimPermissionsResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class GrantAllPlayerClaimPermissions(private val claimRepository: ClaimRepository,
                                      private val playerAccessRepository: PlayerAccessRepository) {
    /**
     * Adds all available permissions to the claim with the given [claimId].
     *
     * @param claimId The [UUID] of the claim to which the permission should be added.
     * @return An [GrantAllPlayerClaimPermissionsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID, playerId: UUID): GrantAllPlayerClaimPermissionsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GrantAllPlayerClaimPermissionsResult.ClaimNotFound

        // Add all permissions to the player
        var anyPermissionEnabled = false
        try {
            val allPermissions = ClaimPermission.entries
            for (permission in allPermissions) {
                if (playerAccessRepository.remove(claimId, playerId, permission)) anyPermissionEnabled = true
            }

            // Return success if at least one permission was granted
            return if (anyPermissionEnabled) {
                GrantAllPlayerClaimPermissionsResult.Success
            } else {
                GrantAllPlayerClaimPermissionsResult.AllAlreadyGranted
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return GrantAllPlayerClaimPermissionsResult.StorageError
        }
    }
}