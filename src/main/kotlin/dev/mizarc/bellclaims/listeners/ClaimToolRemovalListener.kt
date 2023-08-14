package dev.mizarc.bellclaims.listeners

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
import dev.mizarc.bellclaims.infrastructure.getClaimTool


class ClaimToolRemovalListener : Listener {
    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        val itemMeta = itemStack.itemMeta
        if (itemMeta == getClaimTool().itemMeta) {
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
        val itemStack = event.cursor
        val itemMeta = itemStack!!.itemMeta ?: return

        // Check if item is trying to be placed in top slot
        if (itemMeta == getClaimTool().itemMeta) {
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
        val itemMeta = itemStack.itemMeta
        if (itemMeta == getClaimTool().itemMeta) {
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
        if (event.whoClicked.inventory.getItem(event.hotbarButton)?.itemMeta == getClaimTool().itemMeta) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDragToInventory(event: InventoryDragEvent) {
        val itemStack = event.oldCursor
        val itemMeta = itemStack.itemMeta
        val otherInv = event.view.topInventory
        if (itemMeta == getClaimTool().itemMeta && otherInv == event.inventory) {
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
        val mainHandItemMeta = mainHandItem.itemMeta
        if (mainHandItemMeta != null) {
            if (mainHandItemMeta == getClaimTool().itemMeta) {
                event.isCancelled = true
            }
        }

        // Cancel event if offhand has item and main hand is empty
        val offHandItemMeta = offHandItem.itemMeta
        if (offHandItemMeta != null) {
            if (mainHandItemMeta == getClaimTool().itemMeta && mainHandItem.type == Material.AIR) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val droppedItems = event.drops
        val itemsToRemove = arrayListOf<ItemStack>()
        for (droppedItem in droppedItems) {
            val itemMeta = droppedItem.itemMeta
            if (itemMeta == getClaimTool().itemMeta) {
                itemsToRemove.add(droppedItem)
            }
        }

        for (item in itemsToRemove) {
            droppedItems.remove(item)
        }
    }
}