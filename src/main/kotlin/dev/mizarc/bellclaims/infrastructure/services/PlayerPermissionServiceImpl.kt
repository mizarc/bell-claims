package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.api.PlayerPermissionService
import dev.mizarc.bellclaims.api.enums.DefaultPermissionChangeResult
import dev.mizarc.bellclaims.api.enums.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.claims.PlayerAccessRepository
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import org.bukkit.OfflinePlayer
import java.util.*

class PlayerPermissionServiceImpl(private val
                                  playerAccessRepo: PlayerAccessRepository): PlayerPermissionService {
    override fun doesPlayerHavePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): Boolean {
        val playerPermissions = playerAccessRepo.getByPlayer(claim, player)
        return playerPermissions.contains(permission)
    }

    override fun getByClaim(claim: Claim): Map<UUID, Set<ClaimPermission>> {
        return playerAccessRepo.getByClaim(claim)
    }

    override fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission> {
        return playerAccessRepo.getByPlayer(claim, player)
    }

    override fun addForPlayer(claim: Claim, player: OfflinePlayer,
                              permission: ClaimPermission): PlayerPermissionChangeResult {
        if (permission in getByPlayer(claim, player)) return PlayerPermissionChangeResult.PERMISSION_STATE_UNCHANGED
        playerAccessRepo.add(claim, player, permission)
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun addAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult {
        val permissionsToAdd = ClaimPermission.values().toMutableList() - getByPlayer(claim, player)
        if (permissionsToAdd.isEmpty()) DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        for (permission in permissionsToAdd) {
            playerAccessRepo.add(claim, player, permission)
        }
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun removeForPlayer(claim: Claim, player: OfflinePlayer,
                                 permission: ClaimPermission): PlayerPermissionChangeResult {
        if (permission !in getByPlayer(claim, player)) return PlayerPermissionChangeResult.PERMISSION_STATE_UNCHANGED
        playerAccessRepo.remove(claim, player, permission)
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun removeAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult {
        val permissionsToRemove = getByPlayer(claim, player)
        if (permissionsToRemove.isEmpty()) DefaultPermissionChangeResult.PERMISSION_STATE_UNCHANGED

        for (permission in permissionsToRemove) {
            playerAccessRepo.remove(claim, player, permission)
        }
        return PlayerPermissionChangeResult.SUCCESS
    }
}