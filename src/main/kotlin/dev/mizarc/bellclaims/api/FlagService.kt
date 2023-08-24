package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.FlagChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimRule

interface FlagService {
    fun getByClaim(claim: Claim): Set<ClaimRule>
    fun add(claim: Claim, claimRule: ClaimRule): FlagChangeResult
    fun addAll(claim: Claim): FlagChangeResult
    fun remove(claim: Claim, claimRule: ClaimRule): FlagChangeResult
    fun removeAll(claim: Claim): FlagChangeResult
}