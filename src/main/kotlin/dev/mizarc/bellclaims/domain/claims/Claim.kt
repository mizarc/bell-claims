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
 *
 * @constructor Compiles an existing claim using its complete data set.
 * @property id The unique identifier for the claim.
 * @property worldId the unique identifier for the world.
 * @property owner A reference to the owning player.
 * @property creationTime The timestamp when the claim was created.
 * @property name The name of the claim.
 * @property description The description of the claim.
 * @property position The position in the world the claim exists in.
 * @property icon The material the claim is using as an icon.
 */
class Claim(var id: UUID, var worldId: UUID, var owner: OfflinePlayer, val creationTime: Instant,
            var name: String, var description: String, var position: Position3D, var icon: Material) {
    var breakCount = 3

    private val defaultBreakCount = 3
    private var breakPeriod = false

    // Key: UUID of player which transfer request is sent to
    // Value: expiry time of the transfer request (Default request timestamp + 5 minutes)
    var transferRequests: HashMap<UUID, Int> = HashMap()

    /**
     * Compiles a new claim based on the minimum details required.
     *
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param owner The player who owns the claim.
     * @param position The position of the claim.
     * @param name The name of the claim.
     */
    constructor(worldId: UUID, owner: OfflinePlayer, position: Position3D, name: String) : this(
        UUID.randomUUID(), worldId, owner, Instant.now(), name, "", position, Material.BELL)

    /**
     * Compiles a claim based on claim builder object data.
     *
     * @param builder The claim builder to build a claim out of.
     */
    constructor(builder: Builder): this(builder.location.world.uid, builder.player,
        Position3D(builder.location), builder.name)

    /**
     * Gets a reference to the world if available.
     * @return The World object that the claim exists in. May return null if the world isn't loaded.
     */
    fun getWorld(): World? {
        return Bukkit.getWorld(worldId)
    }

    /**
     * Resets the break count after a set period of time.
     */
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

    /**
     * A builder for creating instances of a Claim.
     *
     * @property player The player who should own the claim.
     * @property location The location the claim should exist in.
     */
    class Builder(val player: Player, val location: Location) {
        var name = ""

        fun build() = Claim(this)
    }
}