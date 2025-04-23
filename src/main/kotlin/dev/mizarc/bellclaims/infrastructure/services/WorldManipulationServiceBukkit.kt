package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.UUID

class WorldManipulationServiceBukkit: WorldManipulationService {
    override fun breakWithoutItemDrop(worldId: UUID, position: Position3D): Boolean {
        val world = Bukkit.getWorld(worldId) ?: return false
        val block = world.getBlockAt(position.toLocation(world))
        block.type = Material.AIR
        return true
    }
}