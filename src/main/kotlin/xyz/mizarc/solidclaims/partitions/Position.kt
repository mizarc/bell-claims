package xyz.mizarc.solidclaims.partitions

import org.bukkit.Location

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
open class Position(open val x: Int, open val z: Int) {

    fun toChunk(): Position2D {
        return Position2D(x shr 4, z shr 4)
    }
}