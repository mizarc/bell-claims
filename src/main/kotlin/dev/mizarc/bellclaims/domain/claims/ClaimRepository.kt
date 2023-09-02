package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.OfflinePlayer
import org.bukkit.World
import java.util.*

interface ClaimRepository {
    fun getAll(): Set<Claim>
    fun getById(id: UUID): Claim?
    fun getByPlayer(player: OfflinePlayer): Set<Claim>
    fun getByPosition(position3D: Position3D, worldId: UUID): Claim?
    fun add(claim: Claim)
    fun update(claim: Claim)
    fun remove(claim: Claim)
}