package dev.mizarc.bellclaims.application.actions.claim.anchor

import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.application.results.claim.anchor.MoveClaimAnchorResult
import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class MoveClaimAnchor(private val claimRepository: ClaimRepository,
                      private val worldManipulationService: WorldManipulationService,
                      private val getClaimAtPosition: GetClaimAtPosition
) {
    fun execute(claimId: UUID, playerId: UUID, newWorldId: UUID, newPosition: Position3D): MoveClaimAnchorResult {
        // Get the claim that is being moved
        val claim = claimRepository.getById(claimId) ?: return MoveClaimAnchorResult.StorageError

        // Get the claim at new position
        val claimAtPosition: Claim = when(val result = getClaimAtPosition.execute(newWorldId, newPosition)) {
            is GetClaimAtPositionResult.Success -> result.claim
            is GetClaimAtPositionResult.NoClaimFound -> return MoveClaimAnchorResult.InvalidPosition
            is GetClaimAtPositionResult.StorageError -> return MoveClaimAnchorResult.StorageError
        }

        // Check if the claim at the new position is the same as the current claim
        if (claimAtPosition.id != claimId) {
            return MoveClaimAnchorResult.InvalidPosition
        }

        // Check if the player moving the claim bell is the owner of the claim
        if (claim.playerId != playerId) {
            return MoveClaimAnchorResult.NoPermission
        }

        // Move the claim anchor
        worldManipulationService.breakWithoutItemDrop(claim.worldId, claim.position)
        claim.position = newPosition
        claimRepository.update(claim)
        return MoveClaimAnchorResult.Success
    }
}