package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.interaction.listeners.ClaimRule

interface ClaimRuleRepository {
    fun getByClaim(claim: Claim): MutableSet<ClaimRule>
    fun add(claim: Claim, rule: ClaimRule)
    fun remove(claim: Claim, rule: ClaimRule)
    fun removeByClaim(claim: Claim)
}