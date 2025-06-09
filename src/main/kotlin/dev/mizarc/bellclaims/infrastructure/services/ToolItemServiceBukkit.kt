package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.errors.PlayerNotFoundException
import dev.mizarc.bellclaims.application.services.ToolItemService
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
import dev.mizarc.bellclaims.infrastructure.namespaces.ItemKeys
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ToolItemServiceBukkit(private val localizationProvider: LocalizationProvider): ToolItemService {
    override fun giveClaimTool(playerId: UUID): Boolean {
        // Create the claim tool with special metadata
        val tool = ItemStack(Material.STICK)
            .name(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_LORE_MAIN_HAND))
            .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_CLAIM_TOOL_LORE_OFF_HAND))
        val itemMeta = tool.itemMeta
        itemMeta?.setCustomModelData(1)
        itemMeta.persistentDataContainer.set(ItemKeys.CLAIM_TOOL_KEY, PersistentDataType.BOOLEAN, true)
        tool.itemMeta = itemMeta

        // Give the player the item
        val player = Bukkit.getPlayer(playerId) ?: return false
        val inventory = player.inventory
        inventory.addItem(tool)
        return true
    }

    override fun giveMoveTool(playerId: UUID, claim: Claim): Boolean {
        // Create the claim tool with special metadata
        val tool = ItemStack(Material.BELL)
            .name(localizationProvider.get(playerId, LocalizationKeys.ITEM_MOVE_TOOL_NAME, claim.name))
            .lore(localizationProvider.get(playerId, LocalizationKeys.ITEM_MOVE_TOOL_LORE))
        val itemMeta = tool.itemMeta
        itemMeta?.setCustomModelData(1)
        itemMeta.persistentDataContainer.set(ItemKeys.MOVE_TOOL_KEY, PersistentDataType.STRING, claim.id.toString())
        tool.itemMeta = itemMeta

        // Give the player the tool
        val player = Bukkit.getPlayer(playerId) ?: return false
        val inventory = player.inventory
        inventory.addItem(tool)
        return true
    }

    override fun doesPlayerHaveClaimTool(playerId: UUID): Boolean {
        val player = Bukkit.getPlayer(playerId) ?: throw PlayerNotFoundException(playerId)
        val inventory = player.inventory
        for (item in inventory) {
            val itemData = item.toCustomItemData() ?: continue
            if (isClaimTool(itemData)) return true
        }
        return false
    }

    override fun doesPlayerHaveMoveTool(playerId: UUID, claim: Claim): Boolean {
        val player = Bukkit.getPlayer(playerId) ?: return false
        val inventory = player.inventory
        for (item in inventory) {
            val itemData = item.toCustomItemData() ?: continue
            if (getClaimIdFromPlayerMoveTool(itemData) == claim.id.toString()) return true
        }
        return false
    }

    override fun isClaimTool(itemData: Map<String, String>?): Boolean {
        return itemData?.get(ItemKeys.CLAIM_TOOL_KEY.key) != null
    }

    override fun isMoveTool(itemData: Map<String, String>?): Boolean {
        return itemData?.get(ItemKeys.MOVE_TOOL_KEY.key) != null
    }

    override fun getClaimIdFromPlayerMoveTool(itemData: Map<String, String>?): String? {
        return itemData?.get(ItemKeys.MOVE_TOOL_KEY.key)
    }
}