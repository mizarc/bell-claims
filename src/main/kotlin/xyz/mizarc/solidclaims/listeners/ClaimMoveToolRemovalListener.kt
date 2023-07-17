package xyz.mizarc.solidclaims.listeners

import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import xyz.mizarc.solidclaims.getClaimMoveTool
import xyz.mizarc.solidclaims.getClaimTool
import xyz.mizarc.solidclaims.utils.getStringMeta


class ClaimMoveToolRemovalListener : Listener {
    val claimMoveToolKey = "claim"

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        if (itemStack.getStringMeta(claimMoveToolKey) != null) {
            event.itemDrop.remove()
        }
    }

    @EventHandler
    fun onMoveToInventory(event: InventoryClickEvent) {
        if (event.cursor == null) {
            return
        }

        // Cancel if item is in bottom
        if (event.clickedInventory === event.view.bottomInventory) {
            return
        }

        // Cancel if item meta doesn't exist
        val itemStack = event.cursor ?: return

        // Check if item is trying to be placed in top slot
        if (itemStack.getStringMeta(claimMoveToolKey) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onShiftClick(event: InventoryClickEvent) {
        // Cancel if event isn't a shift click
        if (!event.click.isShiftClick) {
            return
        }

        // Cancel if inventory is top inventory
        if (event.clickedInventory === event.view.topInventory) {
            return
        }

        // Cancel if no item in slot
        val itemStack = event.currentItem ?: return
        if (itemStack.getStringMeta(claimMoveToolKey) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onNumberSwap(event: InventoryClickEvent) {
        // Cancel if event isn't a shift click
        if (event.hotbarButton == -1) {
            return
        }

        // Cancel if inventory is not top inventory
        if (event.clickedInventory != event.view.topInventory) {
            return
        }

        // Cancel if no item in slot
        val itemStack = event.whoClicked.inventory.getItem(event.hotbarButton) ?: return
        if (itemStack.getStringMeta(claimMoveToolKey) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDragToInventory(event: InventoryDragEvent) {
        val itemStack = event.oldCursor
        val otherInv = event.view.topInventory
        if (itemStack.getStringMeta(claimMoveToolKey) != null && otherInv == event.inventory) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onItemFrameUse(event: PlayerInteractEntityEvent) {
        if (event.rightClicked !is ItemFrame) {
            return
        }
        val mainHandItem = event.player.inventory.itemInMainHand
        val offHandItem = event.player.inventory.itemInOffHand

        // Cancel event if main hand has item
        if (mainHandItem.getStringMeta(claimMoveToolKey) != null) {
            event.isCancelled = true
        }

        // Cancel event if offhand has item and main hand is empty
        if (offHandItem.getStringMeta(claimMoveToolKey) != null && mainHandItem.type == Material.AIR) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val droppedItems = event.drops
        val itemsToRemove = arrayListOf<ItemStack>()
        for (droppedItem in droppedItems) {
            if (droppedItem.getStringMeta(claimMoveToolKey) != null) {
                itemsToRemove.add(droppedItem)
            }
        }

        for (item in itemsToRemove) {
            droppedItems.remove(item)
        }
    }
}