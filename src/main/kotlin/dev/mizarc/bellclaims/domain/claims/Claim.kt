package dev.mizarc.bellclaims.domain.claims

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.domain.partitions.Position3D
import org.bukkit.Location
import java.time.Instant
import java.util.*
import kotlin.concurrent.thread

/**
 * A claim object holds the data for the world its in and the players associated with it. It relies on partitions to
 * define its shape.
 * @constructor Compiles an existing claim with associated ID and trusted players.
 * @property id The unique identifier for the claim.
 * @property worldId the unique identifier for the world.
 * @property owner A reference to the owning player.
 */
class Claim(var id: UUID, var worldId: UUID, var owner: OfflinePlayer, val creationTime: Instant,
            var name: String, var description: String, var position: Position3D, var icon: Material) {
    val defaultBreakCount = 3
    var breakCount = 3
    var breakPeriod = false

    /**
     * Compiles a new claim based on the world and owning player.
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param owner A reference to the owning player.
     */
    constructor(worldId: UUID, owner: OfflinePlayer, position: Position3D, name: String) : this(
        UUID.randomUUID(), worldId, owner, Instant.now(), name, "", position, Material.BELL)

    constructor(builder: Builder): this(UUID.randomUUID(), builder.location.world.uid, builder.player, Instant.now(),
        builder.name, builder.description, Position3D(builder.location), builder.icon)

    /**
     * Gets a reference to the world if available.
     * @return The World object that the claim exists in. May return null if the world isn't loaded.
     */
    fun getWorld(): World? {
        return Bukkit.getWorld(worldId)
    }

    fun resetBreakCount() {
        if (!breakPeriod) {
            thread(start = true) {
                breakPeriod = true
                Thread.sleep(10000)
                breakCount = defaultBreakCount
                breakPeriod = false
            }
        }
    }

    class Builder(val player: Player, val location: Location) {
        var name = ""
        var description = ""
        var icon = Material.BELL

        fun build() = Claim(this)
    }
}