package dev.mizarc.bellclaims.domain.values

import org.bukkit.Location
import org.bukkit.World

/**
 * Stores two integers to define a 3D position in the world.
 *
 * @property x The X-Axis position.
 * @property y The Y-Axis position.
 * @property z The Z-Axis position.
 */
data class Position3D(override val x: Int, override val y: Int, override val z: Int): Position(x, y, z)