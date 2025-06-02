package dev.mizarc.bellclaims.infrastructure

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

import java.util.UUID

val claim_tool_key = NamespacedKey("bellclaims", "claim_tool")

fun isClaimTool(itemStack: ItemStack): Boolean {
    val itemMeta = itemStack.itemMeta ?: return false
    val container = itemMeta.persistentDataContainer
    return container.has(claim_tool_key, PersistentDataType.BOOLEAN)
}

fun getClaimTool(localizationProvider: LocalizationProvider,
                 playerId: UUID): ItemStack {
    val tool = ItemStack(Material.STICK)
        .name(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_NAME))
        .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_LORE_MAIN_HAND))
        .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_LORE_OFF_HAND))


    val itemMeta = tool.itemMeta
    itemMeta?.setCustomModelData(1)
    itemMeta.persistentDataContainer.set(claim_tool_key, PersistentDataType.BOOLEAN, true)
    tool.itemMeta = itemMeta
    return tool
}