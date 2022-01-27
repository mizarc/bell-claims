package xyz.mizarc.solidclaims.partitions

import org.bukkit.Location
import java.util.*

class WorldPosition(override val x: Int, override val z: Int, val worldId: UUID): Position(x, z) {
    constructor(location: Location): this(location.blockX, location.blockZ, location.world!!.uid)
}