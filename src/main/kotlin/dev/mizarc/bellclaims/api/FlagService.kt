package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.FlagChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.flags.Flag

interface FlagService {
    fun doesClaimHaveFlag(claim: Claim, flag: Flag): Boolean
    fun getByClaim(claim: Claim): Set<Flag>
    fun add(claim: Claim, flag: Flag): FlagChangeResult
    fun addAll(claim: Claim): FlagChangeResult
    fun remove(claim: Claim, flag: Flag): FlagChangeResult
    fun removeAll(claim: Claim): FlagChangeResult
}