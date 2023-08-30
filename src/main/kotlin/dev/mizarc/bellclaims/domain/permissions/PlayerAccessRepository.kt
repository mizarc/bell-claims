package dev.mizarc.bellclaims.domain.permissions

import dev.mizarc.bellclaims.domain.claims.Claim
import org.bukkit.OfflinePlayer
import java.util.*

interface PlayerAccessRepository {
    fun getByClaim(claim: Claim): MutableMap<UUID, MutableSet<ClaimPermission>>
    fun getByPlayer(claim: Claim, player: OfflinePlayer): MutableSet<ClaimPermission>
    fun add(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)
    fun remove(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)
    fun removeByPlayer(claim: Claim, player: OfflinePlayer)
    fun removeByClaim(claim: Claim)
}