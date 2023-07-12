package xyz.mizarc.solidclaims.partitions

import org.bukkit.Location

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
open class Position3D(open val x: Int, open val y: Int, open val z: Int) {
    constructor(location: Location): this(location.blockX, location.blockY, location.blockZ)

    fun toChunk(): Position {
        return Position(x shr 4, z shr 4)
    }
}