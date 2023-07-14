package xyz.mizarc.solidclaims.partitions

import org.bukkit.Location

/**
 * Stores two integers to define a flat position in the world.
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
data class Position2D(override val x: Int, override val z: Int): Position(x, z) {
    constructor(location: Location): this(location.blockX, location.blockZ)
    constructor(position3D: Position3D): this(position3D.x, position3D.z)
}