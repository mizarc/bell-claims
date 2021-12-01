package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.util.*
import kotlin.collections.ArrayList

/**
 * A claim object holds the data for the world its in and the players associated with it. It relies on partitions to
 * define its shape.
 * @constructor Compiles an existing claim with associated ID and trusted players.
 * @property id The unique identifier for the claim.
 * @property worldId the unique identifier for the world.
 * @property owner A reference to the owning player.
 * @property claimPlayers A list of trusted players.
 */
class Claim(var id: UUID, var worldId: UUID, var owner: OfflinePlayer,
            var defaultPermissions: ArrayList<ClaimPermission>, var claimPlayers: ArrayList<ClaimPlayer>) {
    /**
     * Compiles a new claim based on the world and owning player.
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param owner A reference to the owning player.
     */
    constructor(worldId: UUID, owner: OfflinePlayer) : this(UUID.randomUUID(), worldId, owner, ArrayList(), ArrayList())

    /**
     * Gets a reference to the world if available.
     * @return The World object that the claim exists in. May return null if the world isn't loaded.
     */
    fun getWorld() : World? {
        return Bukkit.getWorld(worldId)
    }
}