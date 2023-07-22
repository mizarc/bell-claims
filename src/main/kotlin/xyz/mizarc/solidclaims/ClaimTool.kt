package xyz.mizarc.solidclaims

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.mizarc.solidclaims.utils.lore
import xyz.mizarc.solidclaims.utils.name

fun getClaimTool(): ItemStack {
    val tool = ItemStack(Material.STICK)
        .name("§bClaim Tool")
        .lore("Use in main hand to edit claim borders")
        .lore("Use in offhand to open menu")
    val itemMeta = tool.itemMeta
    itemMeta?.setCustomModelData(1)
    tool.itemMeta = itemMeta
    return tool
}