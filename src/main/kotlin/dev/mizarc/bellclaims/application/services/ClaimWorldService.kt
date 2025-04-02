package dev.mizarc.bellclaims.application.services

import dev.mizarc.bellclaims.application.enums.ClaimCreationResult
import dev.mizarc.bellclaims.application.enums.ClaimMoveResult
import dev.mizarc.bellclaims.domain.entities.Claim
import org.bukkit.Location
import org.bukkit.OfflinePlayer

/**
 * A service that handles the existence of claims in the world. Actions involve the physical manipulations of claims
 * that depend on partition checks.
 */
interface ClaimWorldService {
    /**
     * Checks to see if a new claim can be created in a location based on the existence of a claim bell and whether a
     * partition can be created in the surrounding space.
     *
     * @param location The location to query.
     * @return True if the queried location is valid.
     */
    fun isNewLocationValid(location: Location): Boolean

    /**
     * Checks to see if an existing claim can be moved to a location based on whether the new location exists in the
     * boundaries of the claim partition.
     *
     * @param claim The existing claim.
     * @param location The location to query.
     * @return True if the queried location is valid.
     */
    fun isMoveLocationValid(claim: Claim, location: Location): Boolean

    /**
     * Gets a claim by the location in the world.
     *
     * @param location The Location to query
     * @return The found claim or null if not found.
     */
    fun getByLocation(location: Location): Claim?

    /**
     * Creates a claim and its associated partition in the selection location.
     *
     * @param name The name of the claim.
     * @param location The location to query.
     * @param player The player to assign the claim to.
     * @return The enum result of the creation action.
     */
    fun create(name: String, location: Location, player: OfflinePlayer): ClaimCreationResult

    /**
     * Moves the position of the claim block to a different location. The new location must be in an area that the claim
     * partition boundaries occupy.
     *
     * @param claim The existing claim.
     * @param location The location to move the claim block to.
     * @return The enum result of the move action.
     */
    fun move(claim: Claim, location: Location): ClaimMoveResult
}