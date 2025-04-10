package dev.mizarc.bellclaims.application.actions.claim.metadata

import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.domain.entities.Claim
import java.util.UUID

class GetClaimDetails(private val claimRepository: ClaimRepository) {
    fun execute(claimId: UUID): Claim? {
        return claimRepository.getById(claimId)
    }
}