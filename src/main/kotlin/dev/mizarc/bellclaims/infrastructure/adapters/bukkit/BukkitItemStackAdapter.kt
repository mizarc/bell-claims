package dev.mizarc.bellclaims.infrastructure.adapters.bukkit

import dev.mizarc.bellclaims.infrastructure.namespaces.ItemKeys
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Read custom persistent data from an ItemStack and return it as an immutable map.
 *
 * Notes:
 * - Must be called on the Bukkit main thread (reads Bukkit API state).
 * - Returns null if the receiver is null, has no item meta, or no recognised keys are present.
 */
fun ItemStack?.toCustomItemData(): Map<String, String>? {
    // Returns null if the item does not have any metadata
    if (this == null || !this.hasItemMeta()) return null
    val itemMeta = this.itemMeta ?: return null

    // Get metadata values from data container
    val container = itemMeta.persistentDataContainer
    val moveValue = container.get(ItemKeys.MOVE_TOOL_KEY, PersistentDataType.STRING)
    val claimValue = container.get(ItemKeys.CLAIM_TOOL_KEY, PersistentDataType.BOOLEAN)

    // Return map if metadata value exists
    if (moveValue == null && claimValue == null) return null
    return buildMap {
        if (moveValue != null) put(ItemKeys.MOVE_TOOL_KEY.key, moveValue)
        if (claimValue != null) put(ItemKeys.CLAIM_TOOL_KEY.key, claimValue.toString())
    }
}