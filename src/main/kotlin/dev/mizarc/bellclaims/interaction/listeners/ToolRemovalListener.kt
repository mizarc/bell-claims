package dev.mizarc.bellclaims.interaction.listeners

import dev.mizarc.bellclaims.application.actions.player.tool.IsItemClaimTool
import dev.mizarc.bellclaims.application.actions.player.tool.IsItemMoveTool
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toCustomItemData
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
import org.bukkit.block.data.type.DecoratedPot
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class ToolRemovalListener : Listener, KoinComponent {
    private val isItemClaimTool: IsItemClaimTool by inject()
    private val isItemMoveTool: IsItemMoveTool by inject()


    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        if (isKeyItem(itemStack)) {
            event.itemDrop.remove()
        }
    }

    @EventHandler
    fun onMoveToInventory(event: InventoryClickEvent) {
        val itemStack = event.cursor
        if (!isKeyItem(itemStack)) return

        // Detect click outside of the inventory window
        if (event.clickedInventory == null) {
            event.view.setCursor(null)
            return
        }

        // Cancel if item is in bottom (standard inventory)
        if (event.clickedInventory === event.view.bottomInventory) {
            return
        }

        // Check if the item is trying to be placed in the top slot (Chests, Furnaces, etc)
        event.isCancelled = true
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
        if (isKeyItem(itemStack)) {
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
        val item = event.whoClicked.inventory.getItem(event.hotbarButton) ?: return
        if (isKeyItem(item)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDragToInventory(event: InventoryDragEvent) {
        val itemStack = event.oldCursor
        val otherInv = event.view.topInventory
        if (isKeyItem(itemStack) && otherInv == event.inventory) {
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
        if (isKeyItem(mainHandItem)) {
            event.isCancelled = true
        }

        // Cancel event if offhand has item and main hand is empty
        if (isKeyItem(offHandItem) && mainHandItem.type == Material.AIR) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPotUse(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (block.blockData !is DecoratedPot) {
            return
        }

        // Cancel event if main hand has item
        val item = event.item ?: return
        if (isKeyItem(item)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val droppedItems = event.drops
        val itemsToRemove = arrayListOf<ItemStack>()
        for (droppedItem in droppedItems) {
            if (isKeyItem(droppedItem)) {
                itemsToRemove.add(droppedItem)
            }
        }
    }

    private fun isKeyItem(itemStack: ItemStack): Boolean {
        if (isItemClaimTool.execute(itemStack.toCustomItemData())) return true
        if (isItemMoveTool.execute(itemStack.toCustomItemData())) return true
        return false
    }
}