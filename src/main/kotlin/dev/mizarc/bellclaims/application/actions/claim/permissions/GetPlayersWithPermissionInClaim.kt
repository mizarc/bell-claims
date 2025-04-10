package dev.mizarc.bellclaims.application.actions.claim.permissions

import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import java.util.UUID

class GetPlayersWithPermissionInClaim(private val playerAccessRepository: PlayerAccessRepository) {
    fun execute(claimId: UUID): List<UUID> {
        return playerAccessRepository.getPlayersWithPermissionInClaim(claimId).toList()
    }
}