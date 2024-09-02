package dev.mizarc.bellclaims.interaction.behaviours

import io.papermc.paper.event.block.PlayerShearBlockEvent
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent
import io.papermc.paper.event.player.PlayerOpenSignEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World.Environment
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Farmland
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.block.data.type.Sign
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.*
import org.bukkit.entity.minecart.ExplosiveMinecart
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent

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
        val blockBreak = PermissionExecutor(BlockBreakEvent::class.java, Companion::cancelEvent, Companion::getBlockLocation, Companion::getBlockBreaker)

        // Any block placing
        val blockPlace = PermissionExecutor(BlockPlaceEvent::class.java, Companion::cancelEvent, Companion::getBlockLocation, Companion::getBlockPlacer)

        // Multi block placing (Beds are the only thing known to go under this)
        val blockMultiPlace = PermissionExecutor(BlockMultiPlaceEvent::class.java, Companion::cancelEvent, Companion::getBlockLocation, Companion::getBlockPlacer)

        // Any entity placing
        val entityPlace = PermissionExecutor(EntityPlaceEvent::class.java, Companion::cancelEntityPlace, Companion::getEntityPlaceLocation, Companion::getEntityPlacePlayer)

        // Used for damaging static entities such as armor stands and item frames
        val specialEntityDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java, Companion::cancelSpecialEntityEvent, Companion::getEntityDamageByEntityLocation, Companion::getEntityDamageSourcePlayer)

        // Used for placing fluids such as water and lava
        val fluidPlace = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelFluidPlace, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for placing fluids such as water and lava
        val farmlandStep = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelFarmlandStep, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for placing item frames
        val itemFramePlace = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelItemFramePlace, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for breaking item frames and paintings
        val hangingEntityBreak = PermissionExecutor(HangingBreakByEntityEvent::class.java, Companion::cancelEvent, Companion::getHangingBreakByEntityEventLocation, Companion::getHangingBreakByEntityEventPlayer)

        // Used for plant fertilisation with bonemeal
        val fertilize = PermissionExecutor(BlockFertilizeEvent::class.java, Companion::cancelEvent, Companion::getBlockLocation, Companion::getBlockFertilizer)

        // Used for inventories that either store something or will have an effect in the world from being used
        val openInventory = PermissionExecutor(InventoryOpenEvent::class.java, Companion::cancelOpenInventory, Companion::getInventoryLocation, Companion::getInventoryInteractPlayer)

        // Used for villager trades
        val villagerTrade = PermissionExecutor(InventoryOpenEvent::class.java, Companion::cancelVillagerOpen, Companion::getInventoryLocation, Companion::getInventoryInteractPlayer)

        // Used for damaging passive mobs
        val playerDamageEntity = PermissionExecutor(EntityDamageByEntityEvent::class.java, Companion::cancelEntityDamageEvent, Companion::getEntityDamageByEntityLocation, Companion::getEntityDamageSourcePlayer)

        // Used for editing sign text
        val signEditing = PermissionExecutor(PlayerOpenSignEvent::class.java, Companion::cancelEvent, Companion::getPlayerOpenSignLocation, Companion::getPlayerOpenSignPlayer)

        // Used for putting dyes or glow ink on a sign
        val signDyeing = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelSignDyeing, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for taking and placing armour from armour stand
        val armorStandManipulate = PermissionExecutor(PlayerArmorStandManipulateEvent::class.java, Companion::cancelEvent, Companion::getArmorStandLocation, Companion::getArmorStandManipulator)

        // Used to change the contents of a flower pot
        val flowerPotManipulate = PermissionExecutor(PlayerFlowerPotManipulateEvent::class.java, Companion::cancelFlowerPotInteract, Companion::getPlayerFlowerPotManipulateLocation, Companion::getPlayerFlowerPotManipulatePlayer)

        // Used for putting and taking items from display blocks such as flower pots and chiseled bookshelves
        val miscDisplayInteractions = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelMiscDisplayInteractions, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for putting items into entity based holders such as item frames
        val miscEntityDisplayInteractions = PermissionExecutor(PlayerInteractEntityEvent::class.java, Companion::cancelMiscEntityDisplayInteractions, Companion::getPlayerInteractEntityLocation, Companion::getPlayerInteractEntityPlayer)

        // Used for breeding passive mobs with food
        val interactAnimals = PermissionExecutor(PlayerInteractEntityEvent::class.java, Companion::cancelAnimalInteract, Companion::getAnimalInteractLocation, Companion::getAnimalInteractPlayer)

        // Used for taking items out of entities by damaging them such as with item frames
        val miscEntityDisplayDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java, Companion::cancelStaticEntityDamage, Companion::getEntityDamageByEntityLocation, Companion::getEntityDamageSourcePlayer)

        // Used for taking the book out of lecterns
        val takeLecternBook = PermissionExecutor(PlayerTakeLecternBookEvent::class.java, Companion::cancelEvent, Companion::getLecternLocation, Companion::getLecternPlayer)

        // Used for opening doors and other openable blocks
        val openDoor = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelDoorOpen, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for blocks that can activate redstone such as buttons and levers
        val redstoneInteract = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelRedstoneInteract, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for grabbing mobs with a fishing rod
        val fishingRod = PermissionExecutor(PlayerFishEvent::class.java, Companion::cancelFishingEvent, Companion::getFishingLocation, Companion::getFishingPlayer)

        // Used for taking the lead off fences
        val takeLeadFromFence = PermissionExecutor(PlayerInteractEntityEvent::class.java, Companion::cancelLeadRemoval, Companion::getPlayerInteractEntityLocation, Companion::getPlayerInteractEntityPlayer)

        // Used for breaking vehicles
        val vehicleDestroy = PermissionExecutor(VehicleDestroyEvent::class.java, Companion::cancelEvent, Companion::getVehicleDestroyLocation, Companion::getVehicleDestroyPlayer)

        // Used for placing vehicles
        val vehiclePlace = PermissionExecutor(EntityPlaceEvent::class.java, Companion::cancelVehiclePlace, Companion::getEntityPlaceLocation, Companion::getEntityPlacePlayer)

        // Used for dragon egg teleports
        val dragonEggTeleport = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelDragonEggTeleport, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for using a bucket to pick up fluids
        val bucketFill = PermissionExecutor(PlayerBucketFillEvent::class.java, Companion::cancelEvent, Companion::getBucketFillLocation, Companion::getBucketFillPlayer)

        // Used for shearing pumpkins
        val pumpkinShear = PermissionExecutor(PlayerShearBlockEvent::class.java, Companion::cancelPumpkinShear, Companion::getShearBlockLocation, Companion::getShearBlockPlayer)

        // Used for shearing beehives
        val beehiveShear = PermissionExecutor(PlayerShearBlockEvent::class.java, Companion::cancelBeehiveShear, Companion::getShearBlockLocation, Companion::getShearBlockPlayer)

        // Used for bottling honey from beehives
        val beehiveBottle = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelBeehiveBottle, Companion::getInteractEventLocation, Companion::getInteractEventPlayer)

        // Used for priming TNT by hand or burning arrow
        val primeTNT = PermissionExecutor(TNTPrimeEvent::class.java, Companion::cancelTNTPrime, Companion::getTNTPrimeLocation, Companion::getTNTPrimePlayer)

        // Used for detonating end crystals by causing damage to it
        val detonateEndCrystal = PermissionExecutor(EntityDamageByEntityEvent::class.java, Companion::cancelEndCrystalDamage, Companion::getEntityDamageByEntityLocation, Companion::getEntityDamageSourcePlayer)

        // Used for exploding beds outside of the overworld
        val detonateBed = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelBedExplode, Companion::getPlayerInteractEntityLocation, Companion::getPlayerInteractEntityPlayer)

        // Used for exploding respawn anchors outside of the nether
        val detonateRespawnAnchor = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelRespawnAnchorExplode, Companion::getPlayerInteractEntityLocation, Companion::getPlayerInteractEntityPlayer)

        // Used for exploding TNT minecarts with a flaming projectile.
        val detonateTNTMinecart = PermissionExecutor(ProjectileHitEvent::class.java, Companion::cancelTNTMinecartExplode, Companion::getProjectileHitLocation, Companion::getProjectileHitPlayer)

        // Used for events triggered by an omen status effect
        val triggerRaid = PermissionExecutor(RaidTriggerEvent::class.java, Companion::cancelEvent, Companion::getRaidTriggerLocation, Companion::getRaidTriggerPlayer)

        /**
         * Cancels any cancellable event.
         */
        private fun cancelEvent(listener: Listener, event: Event): Boolean {
            if (event is Cancellable) {
                event.isCancelled = true
                return true
            }
            return false
        }

        /**
         * Cancels the action of placing down a vehicle such as a boat or minecart.
         */
        private fun cancelVehiclePlace(listener: Listener, event: Event): Boolean {
            if (event !is EntityPlaceEvent) return false
            if (event.entity !is Vehicle) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of placing down other non-vehicle entities.
         */
        private fun cancelEntityPlace(listener: Listener, event: Event): Boolean {
            if (event !is EntityPlaceEvent) return false
            if (event.entity is Vehicle) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of breaking farmland from stepping.
         */
        private fun cancelFarmlandStep(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.PHYSICAL) return false
            val block = event.clickedBlock ?: return false
            if (block.blockData !is Farmland) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of removing a lead from a fence.
         */
        private fun cancelLeadRemoval(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEntityEvent) return false
            if (event.rightClicked !is LeashHitch) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of animal interactions such as leadingk, shearing or feeding.
         */
        private fun cancelAnimalInteract(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEntityEvent) return false
            if (event.rightClicked !is Animals) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels all other entity display interactions such as item frames.
         */
        private fun cancelMiscEntityDisplayInteractions(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEntityEvent) return false
            if (event.rightClicked.type != EntityType.ITEM_FRAME &&
                event.rightClicked.type != EntityType.GLOW_ITEM_FRAME) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of interacting with flower pots.
         */
        private fun cancelFlowerPotInteract(listener: Listener, event: Event): Boolean {
            if (event !is PlayerFlowerPotManipulateEvent) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of interacting with misc display blocks such as item frames.
         */
        private fun cancelMiscDisplayInteractions(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false
            if (block.type != Material.ITEM_FRAME &&
                block.type != Material.GLOW_ITEM_FRAME &&
                block.type != Material.CHISELED_BOOKSHELF &&
                block.type != Material.JUKEBOX &&
                block.type != Material.COMPOSTER &&
                block.type != Material.CAULDRON &&
                block.type != Material.WATER_CAULDRON &&
                block.type != Material.LAVA_CAULDRON &&
                block.type != Material.POWDER_SNOW_CAULDRON &&
                block.type != Material.CAKE) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of placing water or lava buckets.
         */
        private fun cancelFluidPlace(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val clickedBlock = event.clickedBlock ?: return false
            if (clickedBlock.type == Material.CAULDRON) return false
            val item = event.item ?: return false
            if (item.type != Material.WATER_BUCKET &&
                item.type != Material.LAVA_BUCKET) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of placing down item frames.
         */
        private fun cancelItemFramePlace(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val item = event.item ?: return false
            if (item.type != Material.ITEM_FRAME && item.type != Material.GLOW_ITEM_FRAME) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of interacting with a dragon egg, causing it to teleport.
         */
        private fun cancelDragonEggTeleport(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false
            if (block.type != Material.DRAGON_EGG) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of damaging an armor stand.
         */
        private fun cancelSpecialEntityEvent(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.entity !is ArmorStand) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of reeling in passive mobs with a fishing rod.
         */
        private fun cancelFishingEvent(listener: Listener, event: Event): Boolean {
            if (event !is PlayerFishEvent) return false
            val caught = event.caught ?: return false
            if (caught is Monster && caught is Player) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of damaging passive mobs caused by the player.
         */
        private fun cancelEntityDamageEvent(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.entity !is Animals && event.entity !is AbstractVillager) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of damaging static entities such as item frames.
         */
        private fun cancelStaticEntityDamage(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.entity !is ItemFrame && event.entity !is GlowItemFrame) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of door opening.
         */
        private fun cancelDoorOpen(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val block = event.clickedBlock ?: return false
            if (block.state.blockData !is Openable) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of redstone triggers such as buttons or levers.
         */
        private fun cancelRedstoneInteract(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action == Action.LEFT_CLICK_BLOCK) return false
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
         * Cancel the action of opening inventories such as chests and furnaces.
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
                t != InventoryType.ANVIL &&
                t != InventoryType.BEACON &&
                t != InventoryType.HOPPER &&
                t != InventoryType.BREWING &&
                t != InventoryType.DISPENSER &&
                t != InventoryType.DROPPER) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of opening the villager trading menu.
         */
        private fun cancelVillagerOpen(listener: Listener, event: Event): Boolean {
            if (event !is InventoryOpenEvent) return false
            val t = event.inventory.type
            if (t != InventoryType.MERCHANT) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of applying dye or glow ink to a sign.
         */
        private fun cancelSignDyeing(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val block = event.clickedBlock ?: return false
            if (block.blockData !is Sign) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of shearing a pumpkin.
         */
        private fun cancelPumpkinShear(listener: Listener, event: Event): Boolean {
            if (event !is PlayerShearBlockEvent) return false
            if (event.block.type != Material.PUMPKIN) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of shearing a beehive.
         */
        private fun cancelBeehiveShear(listener: Listener, event: Event): Boolean {
            if (event !is PlayerShearBlockEvent) return false
            if (event.block.type != Material.BEEHIVE && event.block.type != Material.BEE_NEST) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action putting honey in a bottle from a beehive.
         */
        private fun cancelBeehiveBottle(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val block = event.clickedBlock ?: return false
            if (block.type != Material.BEEHIVE && block.type != Material.BEE_NEST) return false
            val item = event.item ?: return false
            if (item.type != Material.GLASS_BOTTLE) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of priming tnt with flint and steel or arrows.
         */
        private fun cancelTNTPrime(listener: Listener, event: Event): Boolean {
            if (event !is TNTPrimeEvent) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of blowing up and end crystal by damaging it.
         */
        private fun cancelEndCrystalDamage(listener: Listener, event: Event): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.entity !is EnderCrystal) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of blowing up a bed by interacting with it outside of the overworld.
         */
        private fun cancelBedExplode(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val clickedBlock = event.clickedBlock ?: return false
            if (clickedBlock.blockData !is Bed) return false
            if (clickedBlock.location.world.environment == Environment.NORMAL) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of blowing up a respawn anchor by interacting with it outside of the nether.
         */
        private fun cancelRespawnAnchorExplode(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            val clickedBlock = event.clickedBlock ?: return false
            if (clickedBlock.blockData !is RespawnAnchor) return false
            if (clickedBlock.location.world.environment == Environment.NETHER) return false
            event.isCancelled = true
            return true
        }

        /**
         * Cancels the action of blowing up a TNT minecart with a projectile.
         */
        private fun cancelTNTMinecartExplode(listener: Listener, event: Event): Boolean {
            if (event !is ProjectileHitEvent) return false
            if (event.hitEntity !is ExplosiveMinecart) return false
            event.isCancelled = true
            return true
        }

        private fun getVehicleDestroyPlayer(event: Event): Player? {
            if (event !is VehicleDestroyEvent) return null
            if (event.attacker !is Player) return null
            return event.attacker as Player
        }

        private fun getVehicleDestroyLocation(event: Event): Location? {
            if (event !is VehicleDestroyEvent) return null
            return event.vehicle.location
        }

        private fun getPlayerOpenSignPlayer(event: Event): Player? {
            if (event !is PlayerOpenSignEvent) return null
            return event.player
        }

        private fun getPlayerOpenSignLocation(event: Event): Location? {
            if (event !is PlayerOpenSignEvent) return null
            return event.sign.location
        }

        private fun getHangingBreakByEntityEventPlayer(event: Event): Player? {
            if (event !is HangingBreakByEntityEvent) return null
            return event.remover as? Player ?: return null
        }

        private fun getHangingBreakByEntityEventLocation(event: Event): Location? {
            if (event !is HangingBreakByEntityEvent) return null
            return event.entity.location
        }

        private fun getPlayerInteractEntityPlayer(event: Event): Player? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.player
        }

        private fun getAnimalInteractLocation(event: Event): Location? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.rightClicked.location
        }

        private fun getAnimalInteractPlayer(event: Event): Player? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.player
        }

        private fun getPlayerInteractEntityLocation(event: Event): Location? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.rightClicked.location
        }

        private fun getPlayerFlowerPotManipulatePlayer(event: Event): Player? {
            if (event !is PlayerFlowerPotManipulateEvent) return null
            return event.player
        }

        private fun getPlayerFlowerPotManipulateLocation(event: Event): Location? {
            if (event !is PlayerFlowerPotManipulateEvent) return null
            return event.flowerpot.location
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
         * Get the location of an entity being damaged by another entity.
         */
        private fun getEntityDamageByEntityLocation(event: Event): Location? {
            if (event !is EntityDamageByEntityEvent) return null
            return event.entity.location
        }

        /**
         * Get the player that is damaging another entity.
         */
        private fun getEntityDamageSourcePlayer(event: Event): Player? {
            if (event !is EntityDamageByEntityEvent) return null
            val damagingEntity = event.damager
            if (damagingEntity is Projectile) {
                if (damagingEntity.shooter is Player) {
                    return damagingEntity.shooter as Player
                }
            }
            if (damagingEntity is Player) {
                return damagingEntity
            }
            return null
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
         * Get the player that is filling a bucket.
         */
        private fun getBucketFillPlayer(event: Event): Player? {
            if (event !is PlayerBucketFillEvent) return null
            return event.player
        }

        /**
         * Get the location of the block the bucket is filling from.
         */
        private fun getBucketFillLocation(event: Event): Location? {
            if (event !is PlayerBucketFillEvent) return null
            return event.block.location
        }

        private fun getShearBlockLocation(event: Event): Location? {
            if (event !is PlayerShearBlockEvent) return null
            return event.block.location
        }

        private fun getShearBlockPlayer(event: Event): Player? {
            if (event !is PlayerShearBlockEvent) return null
            return event.player
        }

        private fun getTNTPrimeLocation(event: Event): Location? {
            if (event !is TNTPrimeEvent) return null
            return event.block.location
        }

        private fun getTNTPrimePlayer(event: Event): Player? {
            if (event !is TNTPrimeEvent) return null
            val primingEntity = event.primingEntity ?: return null
            if (primingEntity is Projectile) {
                if (primingEntity.shooter is Player) {
                    return primingEntity.shooter as Player
                }
            }
            if (primingEntity is Player) {
                return primingEntity
            }
            return null
        }

        private fun getProjectileHitLocation(event: Event): Location? {
            if (event !is ProjectileHitEvent) return null
            val hitEntity = event.hitEntity ?: return null
            return hitEntity.location
        }

        private fun getProjectileHitPlayer(event: Event): Player? {
            if (event !is ProjectileHitEvent) return null
            if (event.entity.shooter is Player) {
                return event.entity.shooter as Player
            }
            return null
        }

        private fun getRaidTriggerLocation(event: Event): Location? {
            if (event !is RaidTriggerEvent) return null
            return event.raid.location
        }

        private fun getRaidTriggerPlayer(event: Event): Player? {
            if (event !is RaidTriggerEvent) return null
            return event.player
        }
    }
}