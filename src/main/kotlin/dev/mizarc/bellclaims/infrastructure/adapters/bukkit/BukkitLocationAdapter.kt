package dev.mizarc.bellclaims.infrastructure.adapters.bukkit

import dev.mizarc.bellclaims.domain.values.Position
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import org.bukkit.Location
import org.bukkit.World

fun Location.toPosition2D(): Position2D {
    return Position2D(this.x.toInt(), this.z.toInt())
}

fun Location.toPosition3D(): Position3D {
    return Position3D(this.x.toInt(), this.y.toInt(), this.z.toInt())
}

fun Position.toLocation(world: World): Location {
    return Location(world, this.x.toDouble(), this.y?.toDouble() ?: 0.0, this.z.toDouble())
}
