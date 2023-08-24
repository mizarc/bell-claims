package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import org.bukkit.OfflinePlayer

interface DefaultPermissionService {
    fun getPermissions(claim: Claim): Set<ClaimPermission>
    fun addAllPermissions(claim: Claim): DefaultPermissionChangeResult
    fun removePermission(claim: Claim, claimPermission: ClaimPermission): DefaultPermissionChangeResult
    fun removeAllPermissions(claim: Claim): DefaultPermissionChangeResult
}