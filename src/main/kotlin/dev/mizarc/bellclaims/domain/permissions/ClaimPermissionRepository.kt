package dev.mizarc.bellclaims.domain.permissions

import dev.mizarc.bellclaims.domain.claims.Claim

interface ClaimPermissionRepository {
    fun doesClaimHavePermission(claim: Claim, permission: ClaimPermission): Boolean
    fun getByClaim(claim: Claim): Set<ClaimPermission>
    fun add(claim: Claim, permission: ClaimPermission)
    fun remove(claim: Claim, permission: ClaimPermission)
    fun removeByClaim(claim: Claim)
}