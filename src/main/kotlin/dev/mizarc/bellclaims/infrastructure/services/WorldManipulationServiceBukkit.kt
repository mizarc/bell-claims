package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.domain.values.Area
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.UUID
import kotlin.math.abs

class WorldManipulationServiceBukkit : WorldManipulationService {
    override fun breakWithoutItemDrop(worldId: UUID, position: Position3D): Boolean {
        val world = Bukkit.getWorld(worldId) ?: return false
        val block = world.getBlockAt(position.toLocation(world))
        block.type = Material.AIR
        return true
    }

    override fun isInsideWorldBorder(worldId: UUID, area: Area): Boolean {
        val world = Bukkit.getWorld(worldId) ?: return true
        val worldBorder = world.worldBorder
        val center = worldBorder.center
        val radius = worldBorder.size / 2

        val borderMinX = center.x - radius
        val borderMaxX = center.x + radius
        val borderMinZ = center.z - radius
        val borderMaxZ = center.z + radius

        val areaMinX = area.lowerPosition2D.x.toDouble()
        val areaMaxX = area.upperPosition2D.x.toDouble()
        val areaMinZ = area.lowerPosition2D.z.toDouble()
        val areaMaxZ = area.upperPosition2D.z.toDouble()

        val isContained =
            areaMinX >= borderMinX &&
                    areaMaxX <= borderMaxX &&
                    areaMinZ >= borderMinZ &&
                    areaMaxZ <= borderMaxZ

        return isContained
    }

    override fun isOnEndPlatform(worldId: UUID, position3D: Position3D): Boolean {
        val world = Bukkit.getWorld(worldId) ?: return false

        // Filter to the end
        if (world.environment != org.bukkit.World.Environment.THE_END) return false

        // Get the bounds
        val startX = 100
        val startY = 49
        val startZ = 0

        val dx = abs(position3D.x - startX)
        val dz = abs(position3D.z - startZ)
        val dy = position3D.y - startY

        // 5x5 horizontally centered on start
        return dx <= 2 && dz <= 2 && dy in -1 .. 3

    }
}