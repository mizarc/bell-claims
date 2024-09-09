package dev.mizarc.bellclaims.infrastructure

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name

import dev.mizarc.bellclaims.utils.getLangText


fun getClaimTool(): ItemStack {
    val tool = ItemStack(Material.STICK)
        .name(getLangText("ClaimTool3"))
        .lore(getLangText("ClaimToolMainHand"))
        .lore(getLangText("ClaimToolOffHand"))


    val itemMeta = tool.itemMeta
    itemMeta?.setCustomModelData(1)
    tool.itemMeta = itemMeta
    return tool
}