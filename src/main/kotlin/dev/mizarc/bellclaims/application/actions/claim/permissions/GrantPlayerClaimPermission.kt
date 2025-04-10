package dev.mizarc.bellclaims.application.actions.claim.permissions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.GrantPlayerClaimPermissionResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

/**
 * Action for granting a player a permission in a claim.
 *
 * @property playerAccessRepository Repository for managing player accesses.
 * @property claimRepository Repository for managing claims.
 */
class GrantPlayerClaimPermission(private val playerAccessRepository: PlayerAccessRepository,
                                 private val claimRepository: ClaimRepository) {

    /**
     * Grants a specified [permission] for the [playerId] in a given [claimId].
     *
     * @param claimId The [UUID] of the target claim.
     * @param playerId The [UUID] of the player that should receive the permission.
     * @param permission The [ClaimPermission] to grant.
     * @return An [GrantPlayerClaimPermissionResult] indicating the outcome of the permission grant operation.
     */
    fun execute(claimId: UUID, playerId: UUID, permission: ClaimPermission): GrantPlayerClaimPermissionResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return GrantPlayerClaimPermissionResult.ClaimNotFound

        // Add the permission for the player in the claim
        try {
            return when (playerAccessRepository.add(claimId, playerId, permission)) {
                true -> GrantPlayerClaimPermissionResult.Success
                false -> GrantPlayerClaimPermissionResult.AlreadyExists
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return GrantPlayerClaimPermissionResult.StorageError
        }
    }
}