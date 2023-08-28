package dev.mizarc.bellclaims.api

import dev.mizarc.bellclaims.api.enums.ClaimCreationResult
import dev.mizarc.bellclaims.api.enums.ClaimMoveResult
import dev.mizarc.bellclaims.domain.claims.Claim
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.util.UUID

/**
 * A service that handles the creation, destruction, and modification of a claim.
 */
interface ClaimService {
    /**
     * Gets a claim by its unique id.
     *
     * @param id The UUID of the partition.
     * @return The found claim or null if not found.
     */
    fun getById(id: UUID): Claim?

    fun getByPlayer(player: OfflinePlayer): Set<Claim>
    fun getBlockCount(claim: Claim): Int
    fun getPartitionCount(claim: Claim): Int
    fun changeName(claim: Claim, name: String)
    fun changeDescription(claim: Claim, description: String)
    fun changeIcon(claim: Claim, material: Material)
    fun destroy(claim: Claim)
}