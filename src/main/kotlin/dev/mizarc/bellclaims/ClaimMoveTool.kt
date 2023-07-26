package dev.mizarc.bellclaims

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import dev.mizarc.bellclaims.claims.Claim
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name

fun getClaimMoveTool(claim: Claim) : ItemStack {
    val tool = ItemStack(Material.BELL)
        .name("§bMove Claim '${claim.name}'")
        .lore("Place this where you want the new location to be.")
    val itemMeta = tool.itemMeta
    itemMeta?.setCustomModelData(1)
    itemMeta.persistentDataContainer.set(
        NamespacedKey("solidclaims","claim"), PersistentDataType.STRING, claim.id.toString())
    tool.itemMeta = itemMeta
    return tool
}