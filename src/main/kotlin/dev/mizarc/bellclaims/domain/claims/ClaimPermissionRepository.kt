package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission
import java.sql.SQLException

interface ClaimPermissionRepository {
    fun getByClaim(claim: Claim): MutableSet<ClaimPermission>
    fun add(claim: Claim, permission: ClaimPermission)
    fun remove(claim: Claim, permission: ClaimPermission)
    fun removeByClaim(claim: Claim)
}