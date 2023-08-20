package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import org.bukkit.OfflinePlayer

interface PlayerPermissionService {
    fun getPermissionsForPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission>
    fun addPermissionForPlayer(claim: Claim, player: OfflinePlayer,
                               claimPermission: ClaimPermission): PlayerPermissionChangeResult
    fun addAllPermissionForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult
    fun removePermissionForPlayer(claim: Claim, player: OfflinePlayer,
                                  claimPermission: ClaimPermission): PlayerPermissionChangeResult
    fun removeAllPermissionForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult
}