package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission

interface DefaultPermissionService {
    fun getByClaim(claim: Claim): Set<ClaimPermission>
    fun add(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult
    fun addAll(claim: Claim): DefaultPermissionChangeResult
    fun remove(claim: Claim, permission: ClaimPermission): DefaultPermissionChangeResult
    fun removeAll(claim: Claim): DefaultPermissionChangeResult
}