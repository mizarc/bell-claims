package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.ClaimCreationResult
import dev.mizarc.bellclaims.api.enums.ClaimMoveResult
import dev.mizarc.bellclaims.domain.claims.Claim
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.util.UUID

interface ClaimService {
    fun getById(id: UUID): Claim?
    fun getByLocation(location: Location): Claim?
    fun getByPlayer(player: OfflinePlayer): Set<Claim>
    fun getBlockCount(claim: Claim): Int
    fun getPartitionCount(claim: Claim): Int
    fun create(name: String, location: Location, player: OfflinePlayer): ClaimCreationResult
    fun changeName(claim: Claim, name: String)
    fun changeIcon(claim: Claim, material: Material)
    fun changeLocation(claim: Claim, location: Location): ClaimMoveResult
    fun destroy(claim: Claim)
}