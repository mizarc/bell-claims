package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.errors.DatabaseOperationException
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.AddClaimPlayerPermissionResult
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

/**
 * Action for adding a specific flag to a claim.
 *
 * @property playerAccessRepository Repository for managing claim flags.
 * @property claimRepository Repository for managing claims.
 */
class AddClaimPlayerPermission(private val playerAccessRepository: PlayerAccessRepository,
                               private val claimRepository: ClaimRepository) {

    /**
     * Add the specified [permission] for the [playerId] in a given [claimId].
     *
     * @param claimId The [UUID] of the claim that should be assigned to.
     * @param playerId The [UUID] of the player that should receive the permission.
     * @param permission The [ClaimPermission] to assign to the player.
     * @return An [AddClaimPlayerPermissionResult] indicating the outcome of the flag addition operation.
     */
    fun execute(claimId: UUID, playerId: UUID, permission: ClaimPermission): AddClaimPlayerPermissionResult {
        // Check if claim exists
        claimRepository.getById(claimId) ?: return AddClaimPlayerPermissionResult.ClaimNotFound

        // Add the permission for the player in the claim
        try {
            return when (playerAccessRepository.add(claimId, playerId, permission)) {
                true -> AddClaimPlayerPermissionResult.Success
                false -> AddClaimPlayerPermissionResult.AlreadyExists
            }
        } catch (error: DatabaseOperationException) {
            println("Error has occurred trying to save to the database")
            return AddClaimPlayerPermissionResult.StorageError
        }
    }
}