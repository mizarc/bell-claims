package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.infrastructure.persistence.Config
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerHarvestBlockEvent
import org.bukkit.event.player.PlayerInteractEvent

class HarvestReplantListener(private val config: Config): Listener {
    @EventHandler
    fun onInventoryClose(event: PlayerInteractEvent) {
        // Check if clicked block in set
        val clickedBlock = event.clickedBlock ?: return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val hand = event.hand ?: return
        if (!config.rightClickHarvest) return
        val cropMaterials = setOf(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.COCOA, Material.NETHER_WART
        )
        if (clickedBlock.type !in cropMaterials) return

        // Check if the block is an 'Ageable' crop (covers most common crops)
        val blockData = clickedBlock.blockData
        if (blockData is Ageable) {
            val ageableData = blockData
            if (ageableData.age == ageableData.maximumAge) {
                event.player.swingMainHand()

                // Call built in Bukkit harvest event
                val harvestEvent = PlayerHarvestBlockEvent(event.player, clickedBlock, hand, listOf())
                harvestEvent.callEvent()
                if (harvestEvent.isCancelled) {
                    return
                }

                // Harvest the crop and break it as usual
                event.isCancelled = true
                val harvested = clickedBlock.breakNaturally()

                // Replant if harvest is successful
                if (harvested) {
                    ageableData.age = 0
                    clickedBlock.blockData = ageableData
                }
                event.player.playSound(clickedBlock.location, Sound.ITEM_CROP_PLANT, 1.0f, 1.0f)
            }
        }
    }
}