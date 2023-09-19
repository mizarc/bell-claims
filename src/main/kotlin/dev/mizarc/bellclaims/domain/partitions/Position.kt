package dev.mizarc.bellclaims.domain.partitions

/**
 * Stores two integers to define a flat position in the world.
 *
 * @property x The X-Axis position.
 * @property z The Z-Axis position.
 */
open class Position(open val x: Int, open val y: Int?, open val z: Int) {
    /**
     * Gets the chunk position of this position.
     *
     * @return The chunk position.
     */
    fun getChunk(): Position2D {
        return Position2D(x shr 4, z shr 4)
    }
}