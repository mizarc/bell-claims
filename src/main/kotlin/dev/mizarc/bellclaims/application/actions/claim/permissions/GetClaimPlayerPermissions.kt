package dev.mizarc.bellclaims.application.actions.claim.permissions

import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import java.util.UUID

class GetClaimPlayerPermissions(private val playerAccessRepository: PlayerAccessRepository) {
    fun execute(claimId: UUID, playerId: UUID): List<ClaimPermission> {
        return playerAccessRepository.getForPlayerInClaim(claimId, playerId).toList()
    }
}