package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import xyz.mizarc.solidclaims.partitions.Position
import xyz.mizarc.solidclaims.partitions.Position3D
import java.time.Instant
import java.util.*
import javax.swing.Icon

/**
 * A claim object holds the data for the world its in and the players associated with it. It relies on partitions to
 * define its shape.
 * @constructor Compiles an existing claim with associated ID and trusted players.
 * @property id The unique identifier for the claim.
 * @property worldId the unique identifier for the world.
 * @property owner A reference to the owning player.
 * @property defaultPermissions The permissions of this claim for all players
 * @property playerAccesses A list of trusted players.
 * @property partitions The partitions linked to this claim.
 */
class Claim(var id: UUID, var worldId: UUID, var owner: OfflinePlayer, val creationTime: Instant,
            var name: String, var description: String, var position: Position3D, var icon: Material) {

    /**
     * Compiles a new claim based on the world and owning player.
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param owner A reference to the owning player.
     */
    constructor(worldId: UUID, owner: OfflinePlayer, creationTime: Instant, position: Position3D, icon:Material) : this(
        UUID.randomUUID(), worldId, owner, creationTime, "", "", position, icon)

    constructor(builder: Builder): this(UUID.randomUUID(), builder.world.uid, builder.player, Instant.now(),
        builder.name, builder.description, builder.position, builder.icon)

    /**
     * Gets a reference to the world if available.
     * @return The World object that the claim exists in. May return null if the world isn't loaded.
     */
    fun getWorld(): World? {
        return Bukkit.getWorld(worldId)
    }

    class Builder(val player: Player, val world: World, val position: Position3D) {
        var name = ""
        var description = ""
        var icon = Material.BELL

        fun build() = Claim(this)
    }
}