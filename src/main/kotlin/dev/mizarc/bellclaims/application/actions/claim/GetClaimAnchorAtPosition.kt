package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.GetClaimAnchorAtPositionResult
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class GetClaimAnchorAtPosition(private val claimRepository: ClaimRepository) {
    fun execute(position3D: Position3D, worldId: UUID): GetClaimAnchorAtPositionResult {
        val claim = claimRepository.getByPosition(position3D, worldId) ?: return GetClaimAnchorAtPositionResult.NoClaimAnchorFound
        return GetClaimAnchorAtPositionResult.Success(claim)
    }
}