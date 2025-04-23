package dev.mizarc.bellclaims.domain.entities

import dev.mizarc.bellclaims.domain.values.Position3D
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import java.time.Instant
import java.util.UUID
import kotlin.concurrent.thread

/**
 * A claim object holds the data for the world its in and the players associated with it. It relies on partitions to
 * define its shape.
 *
 * @constructor Compiles an existing claim using its complete data set.
 * @property id The unique identifier for the claim.
 * @property worldId the unique identifier for the world.
 * @property playerId The unique identifier of the owning player.
 * @property creationTime The timestamp when the claim was created.
 * @property name The name of the claim.
 * @property description The description of the claim.
 * @property position The position in the world the claim exists in.
 * @property icon The name of the material the claim is using as an icon.
 */
data class Claim(var id: UUID, var worldId: UUID, var playerId: UUID, val creationTime: Instant,
            val name: String, val description: String, val position: Position3D, val icon: String) {
    init {
        require(name.length in 1..50) { "Name must be between 1 and 50 characters." }
        require(description.length <= 300) { "Description cannot exceed 300 characters." }
    }

    var breakCount = 3

    private val defaultBreakCount = 3
    private var breakPeriod = false

    // Key: UUID of player which transfer request is sent to
    // Value: expiry time of the transfer request (Default request timestamp + 5 minutes)
    var transferRequests: java.util.HashMap<UUID, Int> = HashMap()

    /**
     * Compiles a new claim based on the minimum details required.
     *
     * @param worldId The unique identifier of the world the claim is to be made in.
     * @param playerId The id of the owning player.
     * @param position The position of the claim.
     * @param name The name of the claim.
     */
    constructor(worldId: UUID, playerId: UUID, position: Position3D, name: String) : this(
        UUID.randomUUID(), worldId, playerId, Instant.now(), name, "", position, "BELL")

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
}