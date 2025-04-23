package dev.mizarc.bellclaims.application.actions.claim.anchor

import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.claim.anchor.BreakClaimAnchorResult
import dev.mizarc.bellclaims.domain.values.Position3D
import java.util.UUID

class BreakClaimAnchor(private val claimRepository: ClaimRepository,
                       private val partitionRepository: PartitionRepository,
                       private val flagRepository: ClaimFlagRepository,
                       private val claimPermissionRepository: ClaimPermissionRepository,
                       private val playerAccessRepository: PlayerAccessRepository) {

    fun execute(worldId: UUID, position: Position3D): BreakClaimAnchorResult {
        val claim = claimRepository.getByPosition(position, worldId) ?: return BreakClaimAnchorResult.ClaimNotFound

        // Trigger the break reset countdown and decrement break count by 1
        claim.resetBreakCount()
        if (claim.breakCount > 1) {
            return BreakClaimAnchorResult.ClaimBreaking(claim.breakCount)
        }

        // If break counter met, destroy claim and all associated
        playerAccessRepository.removeByClaim(claim.id)
        claimPermissionRepository.removeByClaim(claim.id)
        flagRepository.removeByClaim(claim.id)
        partitionRepository.removeByClaim(claim.id)
        claimRepository.remove(claim.id)
        return BreakClaimAnchorResult.Success
    }
}