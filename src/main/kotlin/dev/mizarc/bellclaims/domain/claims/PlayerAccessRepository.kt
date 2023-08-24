package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import org.bukkit.OfflinePlayer

interface PlayerAccessRepository {
    fun getByClaim(claim: Claim): MutableMap<OfflinePlayer, MutableSet<ClaimPermission>>
    fun getByPlayer(claim: Claim, player: OfflinePlayer): MutableSet<ClaimPermission>
    fun add(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)
    fun remove(claim: Claim, player: OfflinePlayer, permission: ClaimPermission)
    fun removeByPlayer(claim: Claim, player: OfflinePlayer)
    fun removeByClaim(claim: Claim)
}