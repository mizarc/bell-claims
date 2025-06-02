package dev.mizarc.bellclaims.infrastructure

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import java.util.UUID


val move_tool_key = NamespacedKey("bellclaims", "move_tool")

fun isClaimMoveTool(itemStack: ItemStack): Boolean {
    val itemMeta = itemStack.itemMeta ?: return false
    val container = itemMeta.persistentDataContainer
    return container.has(move_tool_key, PersistentDataType.STRING)
}

fun getClaimMoveTool(localizationProvider: LocalizationProvider,
                     playerId: UUID, claim: Claim): ItemStack {
    val tool = ItemStack(Material.BELL)
        .name(localizationProvider.get(playerId, LocalizationKeys.ITEM_MOVE_TOOL_NAME, claim.name))
        .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_MOVE_TOOL_LORE))
    val itemMeta = tool.itemMeta
    itemMeta?.setCustomModelData(1)
    itemMeta.persistentDataContainer.set(move_tool_key, PersistentDataType.STRING, claim.id.toString())
    tool.itemMeta = itemMeta
    return tool
}

fun getClaimIdFromMoveTool(itemStack: ItemStack): String? {
    val itemMeta = itemStack.itemMeta ?: return null
    return itemMeta.persistentDataContainer.get(move_tool_key, PersistentDataType.STRING)
}