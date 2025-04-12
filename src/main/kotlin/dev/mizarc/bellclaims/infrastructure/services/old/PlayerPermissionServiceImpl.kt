package dev.mizarc.bellclaims.infrastructure.services.old

import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.results.old.PlayerPermissionChangeResult
import dev.mizarc.bellclaims.application.services.old.PlayerPermissionService
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PlayerPermissionServiceImpl(private val playerAccessRepo: PlayerAccessRepository): PlayerPermissionService {
    override fun doesPlayerHavePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): Boolean {
        val playerPermissions = playerAccessRepo.getForPlayerInClaim(claim, player)
        return playerPermissions.contains(permission)
    }

    override fun getByClaim(claim: Claim): Map<OfflinePlayer, Set<ClaimPermission>> {
        return playerAccessRepo.getForAllPlayersInClaim(claim).mapKeys { Bukkit.getOfflinePlayer(it.key) }
    }

    override fun getByPlayer(claim: Claim, player: OfflinePlayer): Set<ClaimPermission> {
        return playerAccessRepo.getForPlayerInClaim(claim, player)
    }

    override fun addForPlayer(claim: Claim, player: OfflinePlayer,
                              permission: ClaimPermission
    ): PlayerPermissionChangeResult {
        if (permission in getByPlayer(claim, player)) return PlayerPermissionChangeResult.UNCHANGED
        playerAccessRepo.add(claim, player, permission)
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun addAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult {
        val permissionsToAdd = ClaimPermission.entries.toMutableList() - getByPlayer(claim, player)
        if (permissionsToAdd.isEmpty()) return PlayerPermissionChangeResult.UNCHANGED

        for (permission in permissionsToAdd) {
            playerAccessRepo.add(claim, player, permission)
        }
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun removeForPlayer(claim: Claim, player: OfflinePlayer,
                                 permission: ClaimPermission
    ): PlayerPermissionChangeResult {
        if (permission !in getByPlayer(claim, player)) return PlayerPermissionChangeResult.UNCHANGED
        playerAccessRepo.remove(claim, player, permission)
        return PlayerPermissionChangeResult.SUCCESS
    }

    override fun removeAllForPlayer(claim: Claim, player: OfflinePlayer): PlayerPermissionChangeResult {
        val permissionsToRemove = getByPlayer(claim, player)
        if (permissionsToRemove.isEmpty()) return PlayerPermissionChangeResult.UNCHANGED

        for (permission in permissionsToRemove) {
            playerAccessRepo.remove(claim, player, permission)
        }
        return PlayerPermissionChangeResult.SUCCESS
    }
}