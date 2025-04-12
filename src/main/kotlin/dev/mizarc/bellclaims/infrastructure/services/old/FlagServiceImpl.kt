package dev.mizarc.bellclaims.infrastructure.services.old

import dev.mizarc.bellclaims.application.services.old.FlagService
import dev.mizarc.bellclaims.application.results.FlagChangeResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.domain.values.Flag

class FlagServiceImpl(private val flagRepo: ClaimFlagRepository): FlagService {
    override fun doesClaimHaveFlag(claim: Claim, flag: Flag): Boolean {
        return flagRepo.getByClaim(claim).contains(flag)
    }

    override fun getByClaim(claim: Claim): Set<Flag> {
        return flagRepo.getByClaim(claim)
    }

    override fun add(claim: Claim, flag: Flag): FlagChangeResult {
        if (flag in flagRepo.getByClaim(claim))
            return FlagChangeResult.UNCHANGED

        flagRepo.add(claim, flag)
        return FlagChangeResult.SUCCESS
    }

    override fun addAll(claim: Claim): FlagChangeResult {
        val flagsToAdd = Flag.entries - getByClaim(claim)
        if (flagsToAdd.isEmpty()) return FlagChangeResult.UNCHANGED

        for (flag in flagsToAdd) {
            flagRepo.add(claim, flag)
        }
        return FlagChangeResult.SUCCESS
    }

    override fun remove(claim: Claim, flag: Flag): FlagChangeResult {
        if (flag !in flagRepo.getByClaim(claim))
            return FlagChangeResult.UNCHANGED

        flagRepo.remove(claim, flag)
        return FlagChangeResult.SUCCESS
    }

    override fun removeAll(claim: Claim): FlagChangeResult {
        val flagsToRemove = getByClaim(claim)
        if (flagsToRemove.isEmpty()) return FlagChangeResult.UNCHANGED

        for (flag in flagsToRemove) {
            flagRepo.remove(claim, flag)
        }
        return FlagChangeResult.SUCCESS
    }
}