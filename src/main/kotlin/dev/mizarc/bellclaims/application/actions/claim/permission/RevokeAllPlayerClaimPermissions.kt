package dev.mizarc.bellclaims.application.actions.claim.permission

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.claim.permission.GrantAllClaimPermissionsResult
import dev.mizarc.bellclaims.application.results.claim.permission.RevokeAllPlayerClaimPermissionsResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class RevokeAllPlayerClaimPermissions(private val claimRepository: ClaimRepository,
                                      private val playerAccessRepository: PlayerAccessRepository) {
    /**
     * Removes all available flags to the claim with the given [claimId].
     *
     * @param claimId The [UUID] of the claim to which the flag should be added.
     * @return An [GrantAllClaimPermissionsResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID, playerId: UUID): RevokeAllPlayerClaimPermissionsResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return RevokeAllPlayerClaimPermissionsResult.ClaimNotFound

        // Remove all permissions from the player
        var anyPermissionDisabled = false
        try {
            val allPermissions = ClaimPermission.entries
            for (permission in allPermissions) {
                if (playerAccessRepository.remove(claimId, playerId, permission)) anyPermissionDisabled = true
            }

            // Return success if at least one permission was revoked
            return if (anyPermissionDisabled) {
                RevokeAllPlayerClaimPermissionsResult.Success
            } else {
                RevokeAllPlayerClaimPermissionsResult.AllAlreadyRevoked
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database: ${error.message}")
            return RevokeAllPlayerClaimPermissionsResult.StorageError
        }
    }
}