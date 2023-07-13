package xyz.mizarc.solidclaims.partitions

import org.bukkit.Location
import org.bukkit.World

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
data class Position3D(val x: Int, val y: Int, val z: Int) {
    constructor(location: Location): this(location.blockX, location.blockY, location.blockZ)

    fun toChunk(): Position {
        return Position(x shr 4, z shr 4)
    }

    fun toLocation(world: World): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}