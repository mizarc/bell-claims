package dev.mizarc.bellclaims.api.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.OfflinePlayer
import java.util.*

interface ClaimRepository {
    fun getAll(): Set<Claim>
    fun getById(id: UUID): Claim?
    fun getByPlayer(player: OfflinePlayer): Set<Claim>
    fun getByPosition(position3D: Position3D): Claim?
    fun add(claim: Claim)
    fun update(claim: Claim)
    fun remove(claim: Claim)
}