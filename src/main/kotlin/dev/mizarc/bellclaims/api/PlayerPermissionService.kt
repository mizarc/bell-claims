package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import org.bukkit.OfflinePlayer
import java.util.*

interface PlayerPermissionService {
    fun doesPlayerHavePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): Boolean
    fun getByClaim(claim: Claim): Map<UUID, Set<ClaimPermission>>
    fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission>
    fun addForPlayer(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): PlayerPermissionChangeResult
    fun addAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult
    fun removeForPlayer(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): PlayerPermissionChangeResult
    fun removeAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult
}