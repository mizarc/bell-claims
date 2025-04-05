package dev.mizarc.bellclaims.application.actions

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.entities.Claim
import java.util.UUID

class ListPlayerClaims(private val claimRepository: ClaimRepository) {
    fun execute(playerId: UUID): List<Claim> {
        return claimRepository.getByPlayer(playerId).toList().sortedBy { it.name }
    }
}