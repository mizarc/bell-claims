package dev.mizarc.bellclaims.domain.partitions

import org.bukkit.Location

/**
 * Stores two integers to define a flat position in the world.
 *
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
data class Position2D(override val x: Int, override val z: Int): Position(x, null, z) {
    /**
     * Creates a position from a location.
     *
     * @param location The location instance to use.
     */
    constructor(location: Location): this(location.blockX, location.blockZ)

    /**
     * Creates a position from a 3D position
     *
     * @param position3D The 3D position to use
     */
    constructor(position3D: Position3D): this(position3D.x, position3D.z)
}