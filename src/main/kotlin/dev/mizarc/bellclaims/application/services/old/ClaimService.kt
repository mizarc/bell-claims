package dev.mizarc.bellclaims.application.services.old

import dev.mizarc.bellclaims.domain.entities.Claim
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import java.util.HashMap
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
     * Gets all claims owned by a player.
     *
     * @param player The player to query.
     * @return The set of claims that belong to the player.
     */
    fun getByPlayer(player: OfflinePlayer): Set<Claim>

    /**
     * Gets the amount of blocks that the claim occupies.
     *
     * @param claim The claim to query.
     * @return The number of blocks.
     */
    fun getBlockCount(claim: Claim): Int

    /**
     * Gets the amount of partitions that belong to a claim.
     *
     * @param claim The claim to query.
     * @return The number of partitions.
     */
    fun getPartitionCount(claim: Claim): Int

    /**
     * Changes the name of the claim.
     *
     * @param claim The target claim.
     * @param name The new name.
     */
    fun changeName(claim: Claim, name: String)

    /**
     * Changes the description of the claim.
     *
     * @param claim The target claim.
     * @param description The new description.
     */
    fun changeDescription(claim: Claim, description: String)

    /**
     * Changes the icon of a claim using a material type.
     *
     * @param claim The target claim.
     * @param material The material to use.
     */
    fun changeIcon(claim: Claim, material: Material)

    /**
     * Transfer claim to player
     *
     * @param claim The target claim.
     * @param player The player which will receive the claim
     */
    fun transferClaim(claim: Claim, player: OfflinePlayer)

    /**
     * Add transfer request for player
     *
     * @param claim The target claim.
     * @param player The player which will receive the claim
     */
    fun addTransferRequest(claim: Claim, player: OfflinePlayer)

    /**
     * Get transfer requests for claim
     *
     * @param claim The target claim.
     */
    fun getTransferRequests(claim: Claim): HashMap<UUID, Int>

    /**
     * Check if transfer request exists for player
     *
     * @param claim The target claim.
     */
    fun playerHasTransferRequest(claim: Claim, player: OfflinePlayer): Boolean

    /**
     * Get transfer requests for claim
     *
     * @param claim The target claim.
     */
    fun deleteTransferRequest(claim: Claim, player: OfflinePlayer)

    /**
     * Deletes a claim and all its associated data.
     *
     * @param claim The claim to delete.
     */
    fun destroy(claim: Claim)
}