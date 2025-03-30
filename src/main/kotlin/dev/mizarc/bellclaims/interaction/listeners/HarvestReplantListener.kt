package dev.mizarc.bellclaims.interaction.listeners

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class HarvestReplantListener: Listener {
    @EventHandler
    fun onInventoryClose(event: PlayerInteractEvent) {
        // Check if clicked block in set
        val clickedBlock = event.clickedBlock ?: return
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
                event.isCancelled = true
                event.player.swingMainHand()
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