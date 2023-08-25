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

    /**
     * Gets a partition by the location in the world.
     *
     * @param location The Location to query
     * @return The found claim or null if not found.
     */
    fun getByLocation(location: Location): Claim?
    
    fun getByPlayer(player: OfflinePlayer): Set<Claim>
    fun getBlockCount(claim: Claim): Int
    fun getPartitionCount(claim: Claim): Int
    fun create(name: String, location: Location, player: OfflinePlayer): ClaimCreationResult
    fun changeName(claim: Claim, name: String)
    fun changeDescription(claim: Claim, name: String)
    fun changeIcon(claim: Claim, material: Material)
    fun changeLocation(claim: Claim, location: Location): ClaimMoveResult
    fun destroy(claim: Claim)
}