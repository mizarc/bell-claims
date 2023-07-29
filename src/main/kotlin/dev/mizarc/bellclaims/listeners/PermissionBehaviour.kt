package dev.mizarc.bellclaims.listeners

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.*
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*

/**
 * A data structure that contains the type of event [eventClass], the function to handle the result of the event [handler],
 * a method to obtain the location of the event [location], and a method to obtain the player causing the event [source].
 */
data class PermissionExecutor(val eventClass: Class<out Event>, val handler: (l: Listener, e: Event) -> Boolean, val location: (e: Event) -> Location?, val source: (e: Event) -> Player?)

/**
 * A static class object to define the behaviour of event handling for events that occur within claims where the
 * origin does not have the permission to perform such actions.
 */
class PermissionBehaviour {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        // Any block breaking
        val blockBreak = PermissionExecutor(BlockBreakEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockBreaker)

        // Any block placing
        val blockPlace = PermissionExecutor(BlockPlaceEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockPlacer)

        // Any entity placing
        val entityPlace = PermissionExecutor(EntityPlaceEvent::class.java, ::cancelEvent, ::getEntityPlaceLocation, ::getEntityPlacePlayer)

        // Used for damaging static entities such as armor stands and item frames
        val specialEntityDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java, ::cancelSpecialEntityEvent, ::getPlayerDamageSpecialLocation, ::getPlayerDamageSpecialPlayer)

        // Used for placing fluids such as water and lava
        val fluidPlace = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelFluidPlace, ::getInteractEventLocation, ::getInteractEventPlayer)

        // Used for placing item frames
        val itemFramePlace = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelItemFramePlace, ::getInteractEventLocation, ::getInteractEventPlayer)

        // Used for breaking item frames and paintings
        val hangingEntityBreak = PermissionExecutor(HangingBreakByEntityEvent::class.java, ::cancelEvent, ::getHangingBreakByEntityEventLocation, ::getHangingBreakByEntityEventPlayer)

        // Used for plant fertilisation with bonemeal
        val fertilize = PermissionExecutor(BlockFertilizeEvent::class.java, ::cancelEvent, ::getBlockLocation, ::getBlockFertilizer)

        // Used for inventories that either store something or will have an effect in the world from being used
        val openInventory = PermissionExecutor(InventoryOpenEvent::class.java, ::cancelOpenInventory, ::getInventoryLocation, ::getInventoryInteractPlayer)

        // Used for villager trades
        val villagerTrade = PermissionExecutor(InventoryOpenEvent::class.java, ::cancelVillagerOpen, ::getInventoryLocation, ::getInventoryInteractPlayer)

        // Used for damaging passive mobs
        val playerDamageEntity = PermissionExecutor(EntityDamageByEntityEvent::class.java, ::cancelEntityDamageEvent, ::getEntityDamageByEntityLocation, ::getEntityDamageSourcePlayer)

        // Used for leashing passive mobs with lead
        val leashEntity = PermissionExecutor(PlayerLeashEntityEvent::class.java, ::cancelEvent, ::getLeashEntityLocation, ::getLeashPlayer)

        // Used for shearing mobs with a shear
        val shearEntity = PermissionExecutor(PlayerShearEntityEvent::class.java, ::cancelEvent, ::getShearEntityLocation, ::getShearPlayer)

        // Used for taking and placing armour from armour stand
        val armorStandManipulate = PermissionExecutor(PlayerArmorStandManipulateEvent::class.java, ::cancelEvent, ::getArmorStandLocation, ::getArmorStandManipulator)

        // Used for putting and taking items from display blocks such as flower pots and chiseled bookshelves
        val miscDisplayInteractions = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelMiscDisplayInteractions, ::getInteractEventLocation, ::getInteractEventPlayer)

        // Used for putting items into entity based holders such as item frames
        val miscEntityDisplayInteractions = PermissionExecutor(PlayerInteractEntityEvent::class.java, ::cancelMiscEntityDisplayInteractions, ::getPlayerInteractEntityLocation, ::getPlayerInteractEntityPlayer)

        // Used for taking items out of entities by damaging them such as with item frames
        val miscEntityDisplayDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java, ::cancelStaticEntityDamage, ::getEntityDamageByEntityLocation, ::getEntityDamageSourcePlayer)

        // Used for taking the book out of lecterns
        val takeLecternBook = PermissionExecutor(PlayerTakeLecternBookEvent::class.java, ::cancelEvent, ::getLecternLocation, ::getLecternPlayer)

        // Used for opening doors and other openable blocks
        val openDoor = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelDoorOpen, ::getInteractEventLocation, ::getInteractEventPlayer)

        // Used for blocks that can activate redstone such as buttons and levers
        val redstoneInteract = PermissionExecutor(PlayerInteractEvent::class.java, ::cancelRedstoneInteract, ::getInteractEventLocation, ::getInteractEventPlayer)

        // Used for grabbing mobs with a fishing rod
        val fishingRod = PermissionExecutor(PlayerFishEvent::class.java, ::cancelFishingEvent, ::getFishingLocation, ::getFishingPlayer)

        /**
         * Cancel any cancellable event.
         */
        private fun cancelEvent(listener: Listener, event: Event): Boolean {
            if (event is Cancellable) {
                event.isCancelled = true
                return true
            }
            return false
        }

        private fun getHangingBreakByEntityEventPlayer(event: Event): Player? {
            if (event !is HangingBreakByEntityEvent) return null
            return event.remover as? Player ?: return null
        }

        private fun getHangingBreakByEntityEventLocation(event: Event): Location? {
            if (event !is HangingBreakByEntityEvent) return null
            return event.entity.location
        }

        private fun cancelMiscEntityDisplayInteractions(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEntityEvent) return false
            if (event.rightClicked.type != EntityType.ITEM_FRAME) return false
            event.isCancelled = true
            return true
        }

        private fun getPlayerInteractEntityPlayer(event: Event): Player? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.player
        }

        private fun getPlayerInteractEntityLocation(event: Event): Location? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.rightClicked.location
        }

        /**
         * Get the location of an entity being placed.
         */
        private fun cancelMiscDisplayInteractions(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false
            if (block.type != Material.ITEM_FRAME &&
                block.type != Material.FLOWER_POT &&
                block.type != Material.CHISELED_BOOKSHELF) return false
            event.isCancelled = true
            return true
        }

        /**
         * Get the location of an entity being placed.
         */
        private fun cancelFluidPlace(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val item = event.item ?: return false
            if (item.type != Material.WATER_BUCKET &&
                item.type != Material.LAVA_BUCKET) return false
            event.isCancelled = true
            return true
        }

        private fun cancelItemFramePlace(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val item = event.item ?: return false
            if (item.type != Material.ITEM_FRAME) return false
            event.isCancelled = true
            return true
        }

        /**
         * Get the location of an entity being placed.
         */
        private fun getInteractEventLocation(event: Event): Location? {
            if (event !is PlayerInteractEvent) return null
            val block = event.clickedBlock ?: return null
            return block.location
        }

        /**
         * Get the player that placed the entity.
         */
        private fun getInteractEventPlayer(event: Event): Player? {
            if (event !is PlayerInteractEvent) return null
            return event.player
        }

        private fun cancelSpecialEntityEvent(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.entity is ArmorStand) {
                event.isCancelled = true
                return true
            }

            return false
        }

        /**
         * Get the location of an entity being placed.
         */
        private fun getPlayerDamageSpecialLocation(event: Event): Location? {
            if (event !is EntityDamageByEntityEvent) return null
            return event.entity.location
        }

        /**
         * Get the player that placed the entity.
         */
        private fun getPlayerDamageSpecialPlayer(event: Event): Player? {
            if (event !is EntityDamageByEntityEvent) return null
            if (event.damager !is Player) return null
            return event.damager as Player
        }

        /**
         * Get the location of an entity being placed.
         */
        private fun getEntityPlaceLocation(event: Event): Location? {
            if (event !is EntityPlaceEvent) return null
            return event.entity.location
        }

        /**
         * Get the player that placed the entity.
         */
        private fun getEntityPlacePlayer(event: Event): Player? {
            if (event !is EntityPlaceEvent) return null
            return event.player
        }

        private fun cancelFishingEvent(listener: Listener, event: Event): Boolean {
            if (event !is PlayerFishEvent) return false
            val caught = event.caught ?: return false
            if (caught is Monster && caught is Player) return false
            event.isCancelled = true
            return true
        }

        private fun getFishingLocation(event: Event): Location? {
            if (event !is PlayerFishEvent) return null
            val caught = event.caught ?: return null
            return caught.location
        }

        private fun getFishingPlayer(event: Event): Player? {
            if (event !is PlayerFishEvent) return null
            return event.player
        }

        /**
         * Cancel entity damage if caused by a player entity. Does not cancel if entity is considered a monster.
         */
        private fun cancelEntityDamageEvent(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.damager !is Player) return false
            if (event.entity !is Player && event.entity !is Animals && event.entity !is AbstractVillager) return false
            event.isCancelled = true
            return true
        }

        private fun cancelStaticEntityDamage(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.damager !is Player) return false
            if (event.entity !is ItemFrame && event.entity !is GlowItemFrame) return false
            event.isCancelled = true
            return true
        }

        private fun cancelDoorOpen(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false
            if (block.state.blockData !is Openable) return false
            event.isCancelled = true
            return true
        }

        private fun cancelRedstoneInteract(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false

            // Block is of type switch, analogue powerable, or a powerable that isn't a door
            if (block.state.blockData is Switch ||
                (block.state.blockData is Powerable && block.state.blockData !is Openable) ||
                block.state.blockData is AnaloguePowerable) {
                event.isCancelled = true
                return true
            }
            return false
        }

        /**
         * Cancel inventory open events if the inventory is a chest.
         */
        private fun cancelOpenInventory(listener: Listener, event: Event): Boolean {
            if (event !is InventoryOpenEvent) return false
            val t = event.inventory.type
            if (t != InventoryType.CHEST &&
                t != InventoryType.SHULKER_BOX &&
                t != InventoryType.BARREL &&
                t != InventoryType.FURNACE &&
                t != InventoryType.BLAST_FURNACE &&
                t != InventoryType.SMOKER &&
                t != InventoryType.ANVIL) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancel entity interactions if the player is trading with a village.
         */
        private fun cancelVillagerOpen(listener: Listener, event: Event): Boolean {
            if (event !is InventoryOpenEvent) return false
            val t = event.inventory.type
            if (t != InventoryType.MERCHANT) return false
            event.isCancelled = true
            return true
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
         * Get the player that placed a block.
         */
        private fun getBlockPlacer(e: Event): Player? {
            if (e !is BlockPlaceEvent) return null
            return e.player
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
        private fun getInventoryLocation(e: Event): Location? {
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