package dev.mizarc.bellclaims.partitions

import org.bukkit.Location
import org.bukkit.World

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
data class Position3D(override val x: Int, override val y: Int, override val z: Int): Position(x, y, z) {
    constructor(location: Location): this(location.blockX, location.blockY, location.blockZ)

    fun toLocation(world: World): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}