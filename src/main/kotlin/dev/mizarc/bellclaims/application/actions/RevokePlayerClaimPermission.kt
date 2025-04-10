package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.RevokePlayerClaimPermissionResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

/**
 * Action for revoking a player's permission in a claim.
 *
 * @property playerAccessRepository Repository for managing player accesses.
 * @property claimRepository Repository for managing claims.
 */
class RevokePlayerClaimPermission(private val playerAccessRepository: PlayerAccessRepository,
                                  private val claimRepository: ClaimRepository) {

    /**
     * Revokes a specified [permission] for the [playerId] in a given [claimId].
     *
     * @param claimId The [UUID] of the target claim.
     * @param playerId The [UUID] of the player that should have their permission revoked.
     * @param permission The [ClaimPermission] to revoke.
     * @return An [RevokePlayerClaimPermissionResult] indicating the outcome of the permission revoke operation.
     */
    fun execute(claimId: UUID, playerId: UUID, permission: ClaimPermission): RevokePlayerClaimPermissionResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return RevokePlayerClaimPermissionResult.ClaimNotFound

        // Remove the permission for the player in the claim
        try {
            return when (playerAccessRepository.remove(claimId, playerId, permission)) {
                true -> RevokePlayerClaimPermissionResult.Success
                false -> RevokePlayerClaimPermissionResult.DoesNotExist
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return RevokePlayerClaimPermissionResult.StorageError
        }
    }
}