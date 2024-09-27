package dev.mizarc.bellclaims.interaction.listeners

import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin

class BlockLaunchListener(private val plugin: JavaPlugin): Listener {
    @EventHandler
    fun onBlockLaunch(event: EntityChangeBlockEvent) {
        if (event.entity !is FallingBlock) return
        if (event.to != Material.AIR) return
        event.entity.setMetadata("origin_location", FixedMetadataValue(plugin, event.block.location))
    }
}