package dev.mizarc.bellclaims.domain.flags

import dev.mizarc.bellclaims.domain.claims.Claim

interface ClaimFlagRepository {
    fun getByClaim(claim: Claim): MutableSet<Flag>
    fun add(claim: Claim, rule: Flag)
    fun remove(claim: Claim, rule: Flag)
    fun removeByClaim(claim: Claim)
}