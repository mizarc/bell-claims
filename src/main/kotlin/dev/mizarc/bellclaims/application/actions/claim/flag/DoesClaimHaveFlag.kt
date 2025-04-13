package dev.mizarc.bellclaims.application.actions.claim.flag

import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.results.claim.flags.DoesClaimHaveFlagResult
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

class DoesClaimHaveFlag(private val claimRepository: ClaimRepository,
                        private val claimFlagRepository: ClaimFlagRepository) {
    fun execute(claimId: UUID, flag: Flag): DoesClaimHaveFlagResult {
        claimRepository.getById(claimId) ?: DoesClaimHaveFlagResult.ClaimNotFound
        return DoesClaimHaveFlagResult.Success(claimFlagRepository.doesClaimHaveFlag(claimId, flag))
    }
}