package xyz.mizarc.solidclaims

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ClaimTool {
    var tool: ItemStack = ItemStack(Material.STICK)

    init {
        val itemMeta = tool.itemMeta
        itemMeta?.setDisplayName("§bClaim Tool")
        itemMeta?.setCustomModelData(1)
        tool.itemMeta = itemMeta
    }
}