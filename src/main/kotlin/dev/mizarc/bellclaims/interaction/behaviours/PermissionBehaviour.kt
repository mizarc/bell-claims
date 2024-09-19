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
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent

/**
 * A data structure that contains the type of event [eventClass], the function to handle the result of the event
 * [handler], a method to obtain the locations this event occurs in [locations], and a method to obtain the player
 * causing the event [source].
 */
data class PermissionExecutor(val eventClass: Class<out Event>,
                              val handler: (listener: Listener, event: Event) -> Boolean,
                              val locations: (event: Event) -> List<Location>,
                              val source: (e: Event) -> Player?)

/**
 * A static class object to define the behaviour of event handling for events that occur within claims where the
 * origin does not have the permission to perform such actions.
 */
class PermissionBehaviour {
    @Suppress("unused")
    companion object {
        // Any block breaking
        val blockBreak = PermissionExecutor(BlockBreakEvent::class.java, Companion::cancelEvent,
            Companion::getBlockLocations, Companion::getBlockBreaker)

        // Any block placing
        val blockPlace = PermissionExecutor(BlockPlaceEvent::class.java, Companion::cancelEvent,
            Companion::getBlockLocations, Companion::getBlockMultiPlacePlayer)

        // Multi block placing (Beds are the only thing known to go under this)
        val blockMultiPlace = PermissionExecutor(BlockMultiPlaceEvent::class.java, Companion::cancelEvent,
            Companion::getBlockMultiPlaceLocations, Companion::getBlockMultiPlacePlayer)

        // Any entity placing
        val entityPlace = PermissionExecutor(EntityPlaceEvent::class.java, Companion::cancelEntityPlace,
            Companion::getEntityPlaceLocations, Companion::getEntityPlacePlayer)

        // Used for damaging static entities such as armor stands and item frames
        val specialEntityDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelSpecialEntityEvent, Companion::getEntityDamageByEntityLocations,
            Companion::getEntityDamageSourcePlayer)

        // Used for placing fluids such as water and lava
        val fluidPlace = PermissionExecutor(PlayerBucketEmptyEvent::class.java, Companion::cancelEvent,
            Companion::getPlayerBucketLocations, Companion::getBucketPlayer)

        // Used for breaking farmland
        val farmlandStep = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelFarmlandStep,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for breaking farmland with a mountable animal
        val mountFarmlandStep = PermissionExecutor(EntityInteractEvent::class.java, Companion::cancelMountFarmlandStep,
            Companion::getEntityInteractLocations, Companion::getInteractEventEntityPassengerPlayer)

        // Used for placing item frames
        val itemFramePlace = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelItemFramePlace,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for placing paintings
        val paintingPlace = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelPaintingPlace,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for breaking item frames and paintings
        val hangingEntityBreak = PermissionExecutor(HangingBreakByEntityEvent::class.java, Companion::cancelEvent,
            Companion::getHangingBreakByEntityEventLocations, Companion::getHangingBreakByEntityEventPlayer)

        // Used for plant fertilisation with bone meal
        val fertilize = PermissionExecutor(BlockFertilizeEvent::class.java, Companion::cancelEvent,
            Companion::getBlockLocations, Companion::getBlockFertilizeEventPlayer)

        // Used for inventories that either store something or will have an effect in the world from being used
        val openInventory = PermissionExecutor(InventoryOpenEvent::class.java, Companion::cancelOpenInventory,
            Companion::getInventoryOpenLocations, Companion::getInventoryInteractPlayer)

        // Used for villager trades
        val villagerTrade = PermissionExecutor(InventoryOpenEvent::class.java, Companion::cancelVillagerOpen,
            Companion::getInventoryOpenLocations, Companion::getInventoryInteractPlayer)

        // Used for damaging passive mobs
        val playerDamageEntity = PermissionExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelEntityDamageEvent, Companion::getEntityDamageByEntityLocations,
            Companion::getEntityDamageSourcePlayer)

        // Used for editing sign text
        val signEditing = PermissionExecutor(PlayerOpenSignEvent::class.java, Companion::cancelEvent,
            Companion::getPlayerOpenSignLocations, Companion::getPlayerOpenSignPlayer)

        // Used for putting dyes or glow ink on a sign
        val signDyeing = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelSignDyeing,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for taking and placing armour from armour stand
        val armorStandManipulate = PermissionExecutor(PlayerArmorStandManipulateEvent::class.java,
            Companion::cancelEvent, Companion::getPlayerArmorStandManipulateLocations, Companion::getArmorStandManipulator)

        // Used to change the contents of a flower pot
        val flowerPotManipulate = PermissionExecutor(PlayerFlowerPotManipulateEvent::class.java,
            Companion::cancelFlowerPotInteract, Companion::getPlayerFlowerPotManipulateLocations,
            Companion::getPlayerFlowerPotManipulatePlayer)

        // Used for putting and taking items from display blocks such as flower pots and chiseled bookshelves
        val miscDisplayInteractions = PermissionExecutor(PlayerInteractEvent::class.java,
            Companion::cancelMiscDisplayInteractions, Companion::getPlayerInteractLocations,
            Companion::getInteractEventPlayer)

        // Used for putting items into entity based holders such as item frames
        val miscEntityDisplayInteractions = PermissionExecutor(PlayerInteractEntityEvent::class.java,
            Companion::cancelMiscEntityDisplayInteractions, Companion::getPlayerInteractEntityLocations,
            Companion::getPlayerInteractEntityPlayer)

        // Used for breeding passive mobs with food
        val interactAnimals = PermissionExecutor(PlayerInteractEntityEvent::class.java, Companion::cancelAnimalInteract,
            Companion::getPlayerInteractEntityLocations, Companion::getAnimalInteractPlayer)

        // Used for taking items out of entities by damaging them such as with item frames
        val miscEntityDisplayDamage = PermissionExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelStaticEntityDamage, Companion::getEntityDamageByEntityLocations,
            Companion::getEntityDamageSourcePlayer)

        // Used for taking the book out of lecterns
        val takeLecternBook = PermissionExecutor(PlayerTakeLecternBookEvent::class.java, Companion::cancelEvent,
            Companion::getPlayerTakeLecternBookLocations, Companion::getLecternPlayer)

        // Used for opening doors and other openable blocks
        val openDoor = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelDoorOpen,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for blocks that can activate redstone such as buttons and levers
        val redstoneInteract = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelRedstoneInteract,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for grabbing mobs with a fishing rod
        val fishingRod = PermissionExecutor(PlayerFishEvent::class.java, Companion::cancelFishingEvent,
            Companion::getPlayerFishLocations, Companion::getFishingPlayer)

        // Used for taking the lead off fences
        val takeLeadFromFence = PermissionExecutor(PlayerInteractEntityEvent::class.java, Companion::cancelLeadRemoval,
            Companion::getPlayerInteractEntityLocations, Companion::getPlayerInteractEntityPlayer)

        // Used for breaking vehicles
        val vehicleDestroy = PermissionExecutor(VehicleDestroyEvent::class.java, Companion::cancelEvent,
            Companion::getVehicleDestroyLocations, Companion::getVehicleDestroyPlayer)

        // Used for placing vehicles
        val vehiclePlace = PermissionExecutor(EntityPlaceEvent::class.java, Companion::cancelVehiclePlace,
            Companion::getEntityPlaceLocations, Companion::getEntityPlacePlayer)

        // Used for dragon egg teleports
        val dragonEggTeleport = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelDragonEggTeleport,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for using a bucket to pick up fluids
        val bucketFill = PermissionExecutor(PlayerBucketFillEvent::class.java, Companion::cancelEvent,
            Companion::getPlayerBucketFillLocations, Companion::getBucketFillPlayer)

        // Used for shearing pumpkins
        val pumpkinShear = PermissionExecutor(PlayerShearBlockEvent::class.java, Companion::cancelPumpkinShear,
            Companion::getPlayerShearBlockLocations, Companion::getShearBlockPlayer)

        // Used for shearing beehives
        val beehiveShear = PermissionExecutor(PlayerShearBlockEvent::class.java, Companion::cancelBeehiveShear,
            Companion::getPlayerShearBlockLocations, Companion::getShearBlockPlayer)

        // Used for bottling honey from beehives
        val beehiveBottle = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelBeehiveBottle,
            Companion::getPlayerInteractLocations, Companion::getInteractEventPlayer)

        // Used for priming TNT by hand or burning arrow
        val primeTNT = PermissionExecutor(TNTPrimeEvent::class.java, Companion::cancelTNTPrime,
            Companion::getTNTPrimeLocations, Companion::getTNTPrimePlayer)

        // Used for detonating end crystals by causing damage to it
        val detonateEndCrystal = PermissionExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelEndCrystalDamage, Companion::getEntityDamageByEntityLocations,
            Companion::getEntityDamageSourcePlayer)

        // Used for exploding beds outside of the overworld
        val detonateBed = PermissionExecutor(PlayerInteractEvent::class.java, Companion::cancelBedExplode,
            Companion::getPlayerInteractEntityLocations, Companion::getPlayerInteractEntityPlayer)

        // Used for exploding respawn anchors outside of the nether
        val detonateRespawnAnchor = PermissionExecutor(PlayerInteractEvent::class.java,
            Companion::cancelRespawnAnchorExplode, Companion::getPlayerInteractEntityLocations,
            Companion::getPlayerInteractEntityPlayer)

        // Used for exploding TNT minecarts with a flaming projectile.
        val detonateTNTMinecart = PermissionExecutor(ProjectileHitEvent::class.java,
            Companion::cancelTNTMinecartExplode, Companion::getProjectileHitLocations, Companion::getProjectileHitPlayer)

        // Used for events triggered by an omen status effect
        val triggerRaid = PermissionExecutor(RaidTriggerEvent::class.java, Companion::cancelEvent,
            Companion::getRaidTriggerLocations, Companion::getRaidTriggerPlayer)

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
         * Cancels the action of breaking farmland from stepping on with a mountable animal like a horse.
         */
        private fun cancelMountFarmlandStep(listener: Listener, event: Event): Boolean {
            if (event !is EntityInteractEvent) return false
            if (event.block.type != Material.FARMLAND) return false
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
         * Cancels the action of animal interactions such as leading, shearing or feeding.
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
         * Cancels the action of placing down paintings.
         */
        private fun cancelPaintingPlace(listener: Listener, event: Event): Boolean {
            if (event !is PlayerInteractEvent) return false
            if (event.action != Action.RIGHT_CLICK_BLOCK) return false
            val item = event.item ?: return false
            if (item.type != Material.PAINTING) return false
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

        /**
         * Gets the affected locations of the VehicleDestroyEvent.
         */
        private fun getVehicleDestroyLocations(event: Event): List<Location> {
            if (event !is VehicleDestroyEvent) return listOf()
            return listOf(event.vehicle.location)
        }

        /**
         * Gets the affected locations of the RaidTriggerEvent.
         */
        private fun getRaidTriggerLocations(event: Event): List<Location> {
            if (event !is RaidTriggerEvent) return listOf()
            return listOf(event.raid.location)
        }

        /**
         * Gets the affected locations of the PlayerOpenSignEvent.
         */
        private fun getPlayerOpenSignLocations(event: Event): List<Location> {
            if (event !is PlayerOpenSignEvent) return listOf()
            return listOf(event.sign.location)
        }

        /**
         * Gets the affected locations of the HangingBreakByEntityEvent.
         */
        private fun getHangingBreakByEntityEventLocations(event: Event): List<Location> {
            if (event !is HangingBreakByEntityEvent) return listOf()
            return listOf(event.entity.location)
        }

        /**
         * Gets the affected locations of the PlayerInteractEntityEvent.
         */
        private fun getPlayerInteractEntityLocations(event: Event): List<Location> {
            if (event !is PlayerInteractEntityEvent) return listOf()
            return listOf(event.rightClicked.location)
        }

        /**
         * Gets the affected locations of the PlayerFlowerPotManipulateEvent.
         */
        private fun getPlayerFlowerPotManipulateLocations(event: Event): List<Location> {
            if (event !is PlayerFlowerPotManipulateEvent) return listOf()
            return listOf(event.flowerpot.location)
        }

        /**
         * Gets the affected locations of the PlayerInteractEvent.
         */
        private fun getPlayerInteractLocations(event: Event): List<Location> {
            if (event !is PlayerInteractEvent) return listOf()
            val block = event.clickedBlock ?: return listOf()
            return listOf(block.location)
        }

        /**
         * Gets the affected locations of the EntityInteractEvent.
         */
        private fun getEntityInteractLocations(event: Event): List<Location> {
            if (event !is EntityInteractEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the EntityPlaceEvent.
         */
        private fun getEntityPlaceLocations(event: Event): List<Location> {
            if (event !is EntityPlaceEvent) return listOf()
            return listOf(event.entity.location)
        }

        /**
         * Gets the affected locations of the PlayerFishEvent.
         */
        private fun getPlayerFishLocations(event: Event): List<Location> {
            if (event !is PlayerFishEvent) return listOf()
            val caught = event.caught ?: return listOf()
            return listOf(caught.location)
        }

        /**
         * Get the affected locations of the PlayerBucketEvent.
         */
        private fun getPlayerBucketLocations(event: Event): List<Location> {
            if (event !is PlayerBucketEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the BlockEvent.
         */
        private fun getBlockLocations(event: Event): List<Location> {
            if (event !is BlockEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the BlockMultiPlaceEvent.
         */
        private fun getBlockMultiPlaceLocations(event: Event): List<Location> {
            if (event !is BlockMultiPlaceEvent) return listOf()
            return event.replacedBlockStates.map { it.location }.distinct()
        }

        /**
         * Gets the affected locations of the PlayerTakeLecternBookEvent.
         */
        private fun getPlayerTakeLecternBookLocations(event: Event): List<Location> {
            if (event !is PlayerTakeLecternBookEvent) return listOf()
            return listOf(event.lectern.location)
        }

        /**
         * Gets the affected locations of the PlayerArmorStandManipulateEvent.
         */
        private fun getPlayerArmorStandManipulateLocations(event: Event): List<Location> {
            if (event !is PlayerArmorStandManipulateEvent) return listOf()
            return listOf(event.rightClicked.location)
        }

        /**
         * Gets the affected locations of the EntityDamageByEntityEvent.
         */
        private fun getEntityDamageByEntityLocations(event: Event): List<Location> {
            if (event !is EntityDamageByEntityEvent) return listOf()
            return listOf(event.entity.location)
        }

        /**
         * Gets the affected locations of the InventoryOpenEvent.
         */
        private fun getInventoryOpenLocations(event: Event): List<Location> {
            if (event !is InventoryOpenEvent) return listOf()
            val location = event.inventory.location ?: return listOf()
            return listOf(location)
        }

        /**
         * Gets the affected locations of the PlayerBucketFillEvent.
         */
        private fun getPlayerBucketFillLocations(event: Event): List<Location> {
            if (event !is PlayerBucketFillEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the PlayerShearBlockEvent.
         */
        private fun getPlayerShearBlockLocations(event: Event): List<Location> {
            if (event !is PlayerShearBlockEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the TNTPrimeEvent.
         */
        private fun getTNTPrimeLocations(event: Event): List<Location> {
            if (event !is TNTPrimeEvent) return listOf()
            return listOf(event.block.location)
        }

        /**
         * Gets the affected locations of the ProjectileHitEvent.
         */
        private fun getProjectileHitLocations(event: Event): List<Location> {
            if (event !is ProjectileHitEvent) return listOf()
            val hitEntity = event.hitEntity ?: return listOf()
            return listOf(hitEntity.location)
        }

        /**
         * Gets the player that is triggering the ProjectileHitEvent.
         */
        private fun getProjectileHitPlayer(event: Event): Player? {
            if (event !is ProjectileHitEvent) return null
            if (event.entity.shooter is Player) {
                return event.entity.shooter as Player
            }
            return null
        }

        /**
         * Gets the player that is triggering the PlayerBucketEvent.
         */
        private fun getBucketPlayer(event: Event): Player? {
            if (event !is PlayerBucketEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the RaidTriggerEvent.
         */
        private fun getRaidTriggerPlayer(event: Event): Player? {
            if (event !is RaidTriggerEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the VehicleDestroyEvent.
         */
        private fun getVehicleDestroyPlayer(event: Event): Player? {
            if (event !is VehicleDestroyEvent) return null
            if (event.attacker !is Player) return null
            return event.attacker as Player
        }

        /**
         * Gets the player that is triggering the PlayerOpenSignEvent.
         */
        private fun getPlayerOpenSignPlayer(event: Event): Player? {
            if (event !is PlayerOpenSignEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the HangingBreakByEntityEvent.
         */
        private fun getHangingBreakByEntityEventPlayer(event: Event): Player? {
            if (event !is HangingBreakByEntityEvent) return null
            return event.remover as? Player
        }

        /**
         * Gets the player that is triggering the PlayerInteractEntityEvent.
         */
        private fun getPlayerInteractEntityPlayer(event: Event): Player? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the BlockFertilizeEvent.
         */
        private fun getBlockFertilizeEventPlayer(e: Event): Player? {
            if (e !is BlockFertilizeEvent) return null
            return e.player
        }

        /**
         * Gets the player that is triggering the EntityPlaceEvent.
         */
        private fun getEntityPlacePlayer(event: Event): Player? {
            if (event !is EntityPlaceEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the PlayerInteractEntityEvent.
         */
        private fun getAnimalInteractPlayer(event: Event): Player? {
            if (event !is PlayerInteractEntityEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the PlayerFlowerPotManipulateEvent.
         */
        private fun getPlayerFlowerPotManipulatePlayer(event: Event): Player? {
            if (event !is PlayerFlowerPotManipulateEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the PlayerInteractEvent.
         */
        private fun getInteractEventPlayer(event: Event): Player? {
            if (event !is PlayerInteractEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the EntityInteractEvent.
         */
        private fun getInteractEventEntityPassengerPlayer(event: Event): Player? {
            if (event !is EntityInteractEvent) return null
            if (event.entity.passengers.isEmpty()) return null
            if (event.entity.passengers.size > 1) return null
            if (event.entity.passengers[0].type != EntityType.PLAYER) return null
            return event.entity.passengers[0] as Player?
        }

        /**
         * Gets the player that is triggering the PlayerFishEvent.
         */
        private fun getFishingPlayer(event: Event): Player? {
            if (event !is PlayerFishEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the PlayerTakeLecternBookEvent.
         */
        private fun getLecternPlayer(e: Event): Player? {
            if (e !is PlayerTakeLecternBookEvent) return null
            return e.player
        }

        /**
         * Gets the player that is triggering the TNTPrimeEvent.
         */
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

        /**
         * Gets the player that is triggering the PlayerArmorStandManipulateEvent.
         */
        private fun getArmorStandManipulator(e: Event): Player? {
            if (e !is PlayerArmorStandManipulateEvent) return null
            return e.player
        }

        /**
         * Gets the player that is triggering the PlayerShearBlockEvent.
         */
        private fun getShearBlockPlayer(event: Event): Player? {
            if (event !is PlayerShearBlockEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the InventoryOpenEvent.
         */
        private fun getInventoryInteractPlayer(e: Event): Player? {
            if (e !is InventoryOpenEvent) return null
            return e.player as Player
        }

        /**
         * Gets the player that is triggering the PlayerBucketFillEvent.
         */
        private fun getBucketFillPlayer(event: Event): Player? {
            if (event !is PlayerBucketFillEvent) return null
            return event.player
        }

        /**
         * Gets the player that is triggering the BlockPlaceEvent.
         */
        private fun getBlockMultiPlacePlayer(e: Event): Player? {
            if (e !is BlockPlaceEvent) return null
            return e.player
        }

        /**
         * Gets the player that is triggering the BlockBreakEvent.
         */
        private fun getBlockBreaker(e: Event): Player? {
            if (e !is BlockBreakEvent) return null
            return e.player
        }

        /**
         * Gets the player that is triggering the EntityDamageByEntityEvent.
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
    }
}