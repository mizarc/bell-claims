package xyz.mizarc.solidclaims.events

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*

/**
 * A data structure that contains the type of an event [eventClass], the function to handle the result of the event [executor],
 * a method to obtain the location of the event [location], and a method to obtain the player causing the event [source].
 */
data class PermissionExecutor(val eventClass: Class<out Event>, val executor: (l: Listener, e: Event) -> Unit, val location: (e: Event) -> Location?, val source: (e: Event) -> Player?)

/**
 * A static class object to define the behaviour of event handling for events that occur within claims where the
 * origin does not have the permission to perform such actions.
 */
class PermissionBehaviour {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        val playerInteract = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelEvent, ::getPlayerInteractLocation, ::getPlayerEventSource)
        val playerInteractEntity = PermissionExecutor(PlayerInteractEntityEvent::class.java, ::cancelEvent, ::getPlayerInteractEntityLocation, ::getPlayerEventSource)
        val blockBreak = PermissionExecutor(BlockBreakEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockBreaker)
        val inventoryOpen = PermissionExecutor(InventoryOpenEvent::class.java, ::cancelEvent, ::getInventoryBlockLocation, ::getInventoryInteractPlayer)
        val openChest = PermissionExecutor(InventoryOpenEvent::class.java, ::cancelOpenChest, ::getInventoryBlockLocation, ::getInventoryInteractPlayer)
        val openFurnace = PermissionExecutor(InventoryOpenEvent::class.java, ::cancelOpenFurnace, ::getInventoryBlockLocation, ::getInventoryInteractPlayer)
        val villagerTrade = PermissionExecutor(PlayerInteractEntityEvent::class.java, ::cancelVillagerTrade, ::getPlayerInteractEntityLocation, ::getPlayerEventSource)
        val playerDamageEntity = PermissionExecutor(EntityDamageByEntityEvent::class.java, ::cancelEntityDamageEvent, ::getEntityDamageByEntityLocation, ::getEntityDamageSourcePlayer)
        val leashEntity = PermissionExecutor(PlayerLeashEntityEvent::class.java, ::cancelEvent, ::getLeashEntityLocation, ::getLeashPlayer)
        val shearEntity = PermissionExecutor(PlayerShearEntityEvent::class.java, ::cancelEvent, ::getShearEntityLocation, ::getShearPlayer)
        val blockDamage = PermissionExecutor(BlockDamageEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockDamager)
        val armorStandManipulate = PermissionExecutor(PlayerArmorStandManipulateEvent::class.java, ::cancelEvent, ::getArmorStandLocation, ::getArmorStandManipulator)
        val takeLecternBook = PermissionExecutor(PlayerTakeLecternBookEvent::class.java, ::cancelEvent, ::getLecternLocation, ::getLecternPlayer)
        val fertilize = PermissionExecutor(BlockFertilizeEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockFertilizer)

        /**
         * Cancel any cancellable event.
         */
        private fun cancelEvent(listener: Listener, event: Event) {
            if (event is Cancellable) {
                event.isCancelled = true
            }
        }

        /**
         * Cancel entity damage if caused by a player entity. Does not cancel if entity is considered a monster.
         */
        private fun cancelEntityDamageEvent(listener: Listener, event: Event) {
            if (event !is EntityDamageByEntityEvent) return
            if (event.damager !is Player) return
            if (event.entity is Monster) return
            event.isCancelled = true
        }

        /**
         * Cancel inventory open events if the inventory is a chest.
         */
        private fun cancelOpenChest(listener: Listener, event: Event) {
            if (event !is InventoryOpenEvent) return
            val t = event.inventory.type
            if (t != InventoryType.CHEST &&
                t != InventoryType.SHULKER_BOX &&
                t != InventoryType.BARREL) return
            event.isCancelled = true
        }

        /**
         * Cancel inventory open events if the inventory is a furnace.
         */
        private fun cancelOpenFurnace(listener: Listener, event: Event) {
            if (event !is InventoryOpenEvent) return
            val t = event.inventory.type
            if (t != InventoryType.FURNACE &&
                t != InventoryType.BLAST_FURNACE &&
                t != InventoryType.SMOKER) return
            event.isCancelled = true
        }

        /**
         * Cancel entity interactions if the player is trading with a village.
         */
        private fun cancelVillagerTrade(listener: Listener, event: Event) {
            if (event !is PlayerInteractEntityEvent) return
            if (event.rightClicked.type == EntityType.VILLAGER ||
                event.rightClicked.type == EntityType.WANDERING_TRADER ) {
                event.isCancelled = true
            }
        }

        /**
         * Get the location of a block involved in a block event.
         */
        private fun getBlockLocation(e: Event): Location? {
            if (e !is BlockEvent) return null
            return e.block.location
        }

        /**
         * Get the player that is fertilizing a block.
         */
        private fun getBlockFertilizer(e: Event): Player? {
            if (e !is BlockFertilizeEvent) return null
            return e.player
        }

        /**
         * Get the location of a lectern that is being interacted with.
         */
        private fun getLecternLocation(e: Event): Location? {
            if (e !is PlayerTakeLecternBookEvent) return null
            return e.lectern.location
        }

        /**
         * Get the player taking a book from a lectern.
         */
        private fun getLecternPlayer(e: Event): Player? {
            if (e !is PlayerTakeLecternBookEvent) return null
            return e.player
        }

        /**
         * Get the location of an armor stand being manipulated.
         */
        private fun getArmorStandLocation(e: Event): Location? {
            if (e !is PlayerArmorStandManipulateEvent) return null
            return e.rightClicked.location
        }

        /**
         * Get the player that is manipulating an armor stand.
         */
        private fun getArmorStandManipulator(e: Event): Player? {
            if (e !is PlayerArmorStandManipulateEvent) return null
            return e.player
        }

        /**
         * Get the player that is damaging a block.
         */
        private fun getBlockDamager(e: Event): Player? {
            if (e !is BlockDamageEvent) return null
            return e.player
        }

        /**
         * Get the player of a PlayerEvent.
         */
        private fun getPlayerEventSource(e: Event): Player? {
            if (e !is PlayerEvent) return null
            return e.player
        }

        /**
         * Get the location of a block being interacted with.
         */
        private fun getPlayerInteractLocation(e: Event): Location? {
            if (e !is PlayerInteractEvent) return null
            return e.clickedBlock?.location
        }

        /**
         * Get the location of an entity being interacted with.
         */
        private fun getPlayerInteractEntityLocation(e: Event): Location? {
            if (e !is PlayerInteractEntityEvent) return null
            return e.rightClicked.location
        }

        /**
         * Get the location of an entity being damaged by another entity.
         */
        private fun getEntityDamageByEntityLocation(e: Event): Location? {
            if (e !is EntityDamageByEntityEvent) return null
            return e.entity.location
        }

        /**
         * Get the player that is damaging another entity.
         */
        private fun getEntityDamageSourcePlayer(e: Event): Player? {
            if (e !is EntityDamageByEntityEvent) return null
            if (e.damager.type != EntityType.PLAYER) return null
            return e.damager as Player
        }

        /**
         * Get the player that broke a block.
         */
        private fun getBlockBreaker(e: Event): Player? {
            if (e !is BlockBreakEvent) return null
            return e.player
        }

        /**
         * Get the location of an inventory that was opened.
         */
        private fun getInventoryBlockLocation(e: Event): Location? {
            if (e !is InventoryOpenEvent) return null
            return e.inventory.location
        }

        /**
         * Get the player that is opening an inventory.
         */
        private fun getInventoryInteractPlayer(e: Event): Player? {
            if (e !is InventoryOpenEvent) return null
            return e.player as Player
        }

        /**
         * Get the location of an entity that is being leashed by a player.
         */
        private fun getLeashEntityLocation(e: Event): Location? {
            if (e !is PlayerLeashEntityEvent) return null
            return e.entity.location
        }

        /**
         * Get the player that is leashing an entity.
         */
        private fun getLeashPlayer(e: Event): Player? {
            if (e !is PlayerLeashEntityEvent) return null
            return e.player
        }

        /**
         * Get the location of an entity that is being sheared by a player.
         */
        private fun getShearEntityLocation(e: Event): Location? {
            if (e !is PlayerShearEntityEvent) return null
            return e.entity.location
        }

        /**
         * Get the player that is shearing an entity.
         */
        private fun getShearPlayer(e: Event): Player? {
            if (e !is PlayerShearEntityEvent) return null
            return e.player
        }
    }
}