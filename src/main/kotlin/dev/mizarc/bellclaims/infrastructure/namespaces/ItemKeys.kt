package dev.mizarc.bellclaims.infrastructure.namespaces

import org.bukkit.NamespacedKey
// Removed import for org.bukkit.plugin.java.JavaPlugin as it's no longer needed for NamespacedKey creation

/**
 * Object to hold and manage Bukkit-specific NamespacedKeys for custom item data.
*/
object ItemKeys {
    /**
     * The string to be used as the namespace for all plugin NamespacedKeys.
     */
    private const val PLUGIN_NAMESPACE_ID = "bell_claims"

    /**
     * Identifies the claim tool.
     */
    val CLAIM_ITEM: NamespacedKey by lazy {
        NamespacedKey(PLUGIN_NAMESPACE_ID, "claim_tool")
    }

    /**
     * Identifies the move tool.
     */
    val MOVE_TOOL_KEY: NamespacedKey by lazy {
        NamespacedKey(PLUGIN_NAMESPACE_ID, "move_tool")
    }
}