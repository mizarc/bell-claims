package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.*

class GetClaimFlags(private val flagRepository: ClaimFlagRepository) {
    fun execute(claimId: UUID): List<Flag> {
        return flagRepository.getByClaim(claimId).toList()
    }
}