package xyz.mizarc.solidclaims

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun getClaimTool() : ItemStack {
    val tool = ItemStack(Material.STICK)
    val itemMeta = tool.itemMeta
    itemMeta?.setDisplayName("§bClaim Tool")
    itemMeta?.setCustomModelData(1)
    tool.itemMeta = itemMeta
    return tool
}