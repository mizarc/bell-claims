package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.interaction.listeners.Flag

interface ClaimFlagRepository {
    fun getByClaim(claim: Claim): MutableSet<Flag>
    fun add(claim: Claim, rule: Flag)
    fun remove(claim: Claim, rule: Flag)
    fun removeByClaim(claim: Claim)
}