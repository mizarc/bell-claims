package dev.mizarc.bellclaims.domain.claims

import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * A repository that handles the persistence of Claims.
 */
interface ClaimRepository {
    /**
     * Gets all claims that exist.
     *
     * @return The set of all claims.
     */
    fun getAll(): Set<Claim>

    /**
     * Gets a claim by its id.
     *
     * @param id the unique id of the claim.
     * @return The found claim, or null if it doesn't exist.
     */
    fun getById(id: UUID): Claim?

    /**
     * Gets all claims that a player owns.
     *
     * @param player The player to retrieve claims for.
     * @return A set of claims owned by the player.
     */
    fun getByPlayer(player: OfflinePlayer): Set<Claim>

    /**
     * Retrieves a claim by the position in the world.
     *
     * @param position The position in the world.
     * @param worldId The unique id of the world.
     * @return The found claim, or null if it doesn't exist.
     */
    fun getByPosition(position: Position3D, worldId: UUID): Claim?

    /**
     * Adds a new claim.
     *
     * @param claim The claim to add.
     */
    fun add(claim: Claim)

    /**
     * Updates the data of an existing claim.
     *
     * @param claim The claim to update.
     */
    fun update(claim: Claim)

    /**
     * Transfer the claim to another player
     *
     * @param claim The claim to update.
     * @param player Player which will receive the claim.
     * @return True if claim was transferred
     */
    fun transfer(claim: Claim, player: OfflinePlayer)

    /**
     * Removes an existing claim.
     *
     * @param claim The claim to remove.
     */
    fun remove(claim: Claim)
}