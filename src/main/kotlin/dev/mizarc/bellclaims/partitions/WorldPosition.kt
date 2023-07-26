package dev.mizarc.bellclaims.partitions

import org.bukkit.Location
import java.util.*

class WorldPosition(override val x: Int, override val z: Int, val worldId: UUID): Position(x, z) {
    constructor(location: Location): this(location.blockX, location.blockZ, location.world!!.uid)
    constructor(position2D: Position2D, worldId: UUID): this(position2D.x, position2D.z, worldId)
}