package dev.mizarc.bellclaims.application.actions.claim

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class GetClaimAnchorAtPosition(private val claimRepository: ClaimRepository) {
    fun execute(position3D: Position3D, worldId: UUID): GetClaimAtPositionResult {
        val claim = claimRepository.getByPosition(position3D, worldId) ?: return GetClaimAtPositionResult.NoClaimFound
        return GetClaimAtPositionResult.Success(claim)
    }
}