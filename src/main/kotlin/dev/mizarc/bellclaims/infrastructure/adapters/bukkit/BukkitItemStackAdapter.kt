package dev.mizarc.bellclaims.infrastructure.adapters.bukkit

import dev.mizarc.bellclaims.infrastructure.namespaces.ItemKeys
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

fun ItemStack?.toCustomItemData(): Map<String, String>? {
    if (this == null || !this.hasItemMeta()) {
        return null
    }
    val itemMeta = this.itemMeta ?: return null
    val persistentDataContainer = itemMeta.persistentDataContainer

    val metadataMap = mutableMapOf<String, String>()
    if (persistentDataContainer.has(ItemKeys.MOVE_TOOL_KEY, PersistentDataType.STRING)) {
        val value = persistentDataContainer.get(ItemKeys.MOVE_TOOL_KEY, PersistentDataType.STRING) ?: return null
        metadataMap[ItemKeys.MOVE_TOOL_KEY.key] = value
    }
    if (persistentDataContainer.has(ItemKeys.CLAIM_TOOL_KEY, PersistentDataType.BOOLEAN)) {
        val value = persistentDataContainer.get(ItemKeys.CLAIM_TOOL_KEY, PersistentDataType.BOOLEAN) ?: return null
        metadataMap[ItemKeys.CLAIM_TOOL_KEY.key] = value.toString()
    }
    return metadataMap
}