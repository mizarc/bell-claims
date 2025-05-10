package dev.mizarc.bellclaims.interaction.listeners

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import dev.mizarc.bellclaims.application.actions.claim.IsPlayerActionAllowed
import dev.mizarc.bellclaims.application.results.claim.IsPlayerActionAllowedResult.Denied
import dev.mizarc.bellclaims.domain.values.PlayerActionType
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition2D
import io.papermc.paper.event.block.PlayerShearBlockEvent
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent
import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World.Environment
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.DecoratedPot
import org.bukkit.block.data.type.Farmland
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.block.data.type.Sign
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.AbstractVillager
import org.bukkit.entity.Animals
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LeashHitch
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Vehicle
import org.bukkit.entity.Villager
import org.bukkit.entity.minecart.ExplosiveMinecart
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFertilizeEvent
import org.bukkit.event.block.BlockMultiPlaceEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.TNTPrimeEvent
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerHarvestBlockEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.raid.RaidTriggerEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.event.weather.LightningStrikeEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class PlayerClaimProtectionListener: Listener, KoinComponent {
    private val isPlayerActionAllowed: IsPlayerActionAllowed by inject()

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val action = PlayerActionType.BREAK_BLOCK
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val action = PlayerActionType.PLACE_BLOCK
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onBlockMultiPlace(event: BlockMultiPlaceEvent) {
        val action = PlayerActionType.PLACE_BLOCK
        for (block in event.replacedBlockStates) {
            cancelIfDisallowed(event, event.player, block.location, action)
        }
    }

    @EventHandler
    fun onEntityPlace(event: EntityPlaceEvent) {
        val action = PlayerActionType.PLACE_ENTITY
        val player = event.player ?: return
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.entity !is ItemFrame && event.entity !is GlowItemFrame) return

        // Get the entity as a player, or if entity is projectile get the projectile's shooter if it's a player
        val hitBy = event.damager
        val player: Player? = when (hitBy) {
            is Player -> hitBy
            is Projectile -> hitBy.shooter as? Player
            else -> null
        }
        if (player == null) return


        val action = PlayerActionType.DAMAGE_STATIC_ENTITY
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @Suppress("UnstableApiUsage")
    @EventHandler
    fun onEntityDeathEvent(event: EntityDeathEvent) {
        if (event.entity !is ArmorStand) return
        val player = event.damageSource.causingEntity as? Player ?: return
        val action = PlayerActionType.DAMAGE_STATIC_ENTITY
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val action = PlayerActionType.PLACE_FLUID
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onPlayerFarmlandStep(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return
        val block = event.clickedBlock ?: return
        if (block.blockData !is Farmland) return
        val action = PlayerActionType.STEP_ON_FARMLAND
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onEntityFarmlandStep(event: EntityInteractEvent) {
        // Check if action is stepping on farmland
        if (event.block.type != Material.FARMLAND) return

        // Check if player is on rideable entity
        val player: Player = when (event.entity) {
            is LivingEntity -> event.entity.vehicle?.passengers?.firstOrNull() as? Player ?: return
            else -> return
        }

        val action = PlayerActionType.STEP_ON_FARMLAND
        cancelIfDisallowed(event, player, event.block.location, action)
    }

    @EventHandler
    fun onPlayerPlaceInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        if (item.type != Material.PAINTING && item.type != Material.ITEM_FRAME
            && item.type != Material.GLOW_ITEM_FRAME) return
        val clickedBlock = event.clickedBlock ?: return
        val action = PlayerActionType.PLACE_ENTITY
        cancelIfDisallowed(event, event.player, clickedBlock.location, action)
    }

    @EventHandler
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        val player = event.remover as? Player ?: return
        val action = PlayerActionType.DAMAGE_STATIC_ENTITY
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onBlockFertilizeEvent(event: BlockFertilizeEvent) {
        val cropMaterials = setOf(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.COCOA, Material.SWEET_BERRIES, Material.GLOW_BERRIES,
            Material.MELON_STEM, Material.PUMPKIN_STEM
        )
        if (event.block.type in cropMaterials) return
        val player = event.player ?: return
        val action = PlayerActionType.FERTILIZE_LAND
        cancelIfDisallowed(event, player, event.block.location, action)
    }

    @EventHandler
    fun onInventoryOpenEvent(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val location = event.inventory.location ?: return

        // Filter to blocks that hold state (or break like anvils)
        val types = setOf(
            InventoryType.CHEST, InventoryType.SHULKER_BOX, InventoryType.BARREL, InventoryType.FURNACE,
            InventoryType.BLAST_FURNACE, InventoryType.SMOKER, InventoryType.ANVIL, InventoryType.BEACON,
            InventoryType.HOPPER, InventoryType.BREWING, InventoryType.DISPENSER, InventoryType.DROPPER)
        val inventoryType = event.inventory.type
        if (inventoryType !in types) return

        // Special check for anvil from custom GUIs implementing the anvils interface. Location is possibly set to
        // (0, 0, 0) instead of null, which conflicts with claims created there. Anvil is not a tile entity so this
        // shouldn't be a problem for performance.
        if (inventoryType == InventoryType.ANVIL && event.inventory.holder == null) return

        val action = PlayerActionType.OPEN_CONTAINER
        cancelIfDisallowed(event, player, location, action)
    }

    @EventHandler
    fun onVillagerOpenEvent(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        val location = event.inventory.location ?: return
        val type = event.inventory.type
        if (type != InventoryType.MERCHANT) return
        val action = PlayerActionType.TRADE_VILLAGER
        cancelIfDisallowed(event, player, location, action)
    }

    @EventHandler
    fun onAnimalDamageByPlayerEvent(event: EntityDamageByEntityEvent) {
        if (event.entity !is Animals && event.entity !is AbstractVillager) return

        // Get the entity as a player, or if entity is projectile get the projectile's shooter if it's a player
        val hitBy = event.damager
        val player: Player? = when (hitBy) {
            is Player -> hitBy
            is Projectile -> hitBy.shooter as? Player
            else -> null
        }
        if (player == null) return

        val action = PlayerActionType.DAMAGE_ANIMAL
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onPlayerOpenSign(event: PlayerOpenSignEvent) {
        val action = PlayerActionType.EDIT_SIGN
        cancelIfDisallowed(event, event.player, event.sign.location, action)
    }

    @EventHandler
    fun onSignInteractEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.blockData !is Sign) return
        val action = PlayerActionType.DYE_SIGN
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onPlayerArmorStandManipulate(event: PlayerArmorStandManipulateEvent) {
        val action = PlayerActionType.MODIFY_STATIC_ENTITY
        cancelIfDisallowed(event, event.player, event.rightClicked.location, action)
    }

    @EventHandler
    fun onPlayerFlowerPotManipulate(event: PlayerFlowerPotManipulateEvent) {
        val action = PlayerActionType.MODIFY_BLOCK
        cancelIfDisallowed(event, event.player, event.flowerpot.location, action)
    }

    @EventHandler
    fun onPlayerModifyInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val types = setOf(Material.ITEM_FRAME,Material.GLOW_ITEM_FRAME, Material.CHISELED_BOOKSHELF, Material.JUKEBOX,
            Material.COMPOSTER, Material.CAULDRON, Material.WATER_CAULDRON, Material.LAVA_CAULDRON,
            Material.POWDER_SNOW_CAULDRON, Material.CAKE, Material.DECORATED_POT)
        if (block.type !in types) return
        val action = PlayerActionType.MODIFY_BLOCK
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onPlayerInteractEntityModifyInteract(event: PlayerInteractEntityEvent) {
        val types = setOf(EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME)
        if (event.rightClicked.type !in types) return
        val action = PlayerActionType.MODIFY_STATIC_ENTITY
        cancelIfDisallowed(event, event.player, event.rightClicked.location, action)
    }

    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked !is Animals) return
        val action = PlayerActionType.INTERACT_WITH_ANIMAL
        cancelIfDisallowed(event, event.player, event.rightClicked.location, action)
    }

    @EventHandler
    fun onPlayerTakeLecternBook(event: PlayerTakeLecternBookEvent) {
        val action = PlayerActionType.TAKE_LECTERN_BOOK
        cancelIfDisallowed(event, event.player, event.lectern.location, action)
    }

    @EventHandler
    fun onPlayerDoorInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (block.state.blockData !is Openable) return
        val action = PlayerActionType.OPEN_DOOR
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onPlayerRedstoneInteract(event: PlayerInteractEvent) {
        if (event.action == Action.LEFT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return

        // Block is of type switch, analogue powerable, or a powerable that isn't a door
        if (!(block.state.blockData is Switch
                    || (block.state.blockData is Powerable && block.state.blockData !is Openable)
                    || block.state.blockData is AnaloguePowerable)) {
            return
        }

        val action = PlayerActionType.USE_REDSTONE
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onPlayerFishEvent(event: PlayerFishEvent) {
        val caught = event.caught ?: return
        if (caught is Monster || caught is Player) return
        val action = PlayerActionType.ROD_ANIMAL
        cancelIfDisallowed(event, event.player, caught.location, action)
    }

    @EventHandler
    fun onPlayerInteractLeadEvent(event: PlayerInteractEntityEvent) {
        if (event.rightClicked !is LeashHitch) return
        val action = PlayerActionType.DETACH_LEAD
        cancelIfDisallowed(event, event.player, event.rightClicked.location, action)
    }

    @EventHandler
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        val player = event.attacker as? Player ?: return
        val action = PlayerActionType.DESTROY_VEHICLE
        cancelIfDisallowed(event, player, event.vehicle.location, action)
    }

    @EventHandler
    fun onVehiclePlace(event: EntityPlaceEvent) {
        if (event.entity !is Vehicle) return
        val player = event.player ?: return
        val action = PlayerActionType.PLACE_VEHICLE
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onPlayerDragonEggInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (block.type != Material.DRAGON_EGG) return
        val action = PlayerActionType.TELEPORT_DRAGON_EGG
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onPlayerBucketFill(event: PlayerBucketFillEvent) {
        val action = PlayerActionType.FILL_BUCKET
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onPlayerShearPumpkinEvent(event: PlayerShearBlockEvent) {
        if (event.block.type != Material.PUMPKIN) return
        val action = PlayerActionType.SHEAR_PUMPKIN
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onPlayerShearBeehiveEvent(event: PlayerShearBlockEvent) {
        if (event.block.type != Material.BEEHIVE && event.block.type != Material.BEE_NEST) return
        val action = PlayerActionType.USE_BEEHIVE
        cancelIfDisallowed(event, event.player, event.block.location, action)
    }

    @EventHandler
    fun onPlayerBeehiveInteractEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type != Material.BEEHIVE && block.type != Material.BEE_NEST) return
        val item = event.item ?: return
        if (item.type != Material.GLASS_BOTTLE) return
        val action = PlayerActionType.USE_BEEHIVE
        cancelIfDisallowed(event, event.player, block.location, action)
    }

    @EventHandler
    fun onTNTPrime(event: TNTPrimeEvent) {
        val player = event.primingEntity as? Player ?: return
        val action = PlayerActionType.PRIME_TNT
        cancelIfDisallowed(event, player, event.block.location, action)
    }

    @EventHandler
    fun onEndCrystalDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity !is EnderCrystal) return
        val player = event.damager as? Player ?: return
        val action = PlayerActionType.DETONATE_ENTITY
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onPlayerBedInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.blockData !is Bed) return
        if (clickedBlock.location.world.environment == Environment.NORMAL) return
        val action = PlayerActionType.DETONATE_BLOCK
        cancelIfDisallowed(event, event.player, clickedBlock.location, action)
    }

    @EventHandler
    fun onPlayerRespawnAnchorInteract(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.blockData !is RespawnAnchor) return
        if (clickedBlock.location.world.environment == Environment.NETHER) return
        val action = PlayerActionType.DETONATE_BLOCK
        cancelIfDisallowed(event, event.player, clickedBlock.location, action)
    }

    @EventHandler
    fun onProjectileHitEvent(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        if (hitEntity !is ExplosiveMinecart) return
        val player = event.entity.shooter as? Player ?: return
        val action = PlayerActionType.DETONATE_ENTITY
        cancelIfDisallowed(event, player, hitEntity.location, action)
    }

    @EventHandler
    fun onRaidTrigger(event: RaidTriggerEvent) {
        val action = PlayerActionType.TRIGGER_RAID
        cancelIfDisallowed(event, event.player, event.raid.location, action)
    }

    @EventHandler
    fun onEntityChangeBlock(event: EntityChangeBlockEvent) {
        if (event.block.blockData !is DecoratedPot) return

        // Get the entity as a player, or if entity is projectile get the projectile's shooter if it's a player
        val player: Player? = when (event.entity) {
            is Player -> event.entity as Player
            is Projectile -> {
                val projectile = event.entity as Projectile
                projectile.shooter as Player
            }
            else -> null
        }
        if (player == null) return

        val action = PlayerActionType.BREAK_BLOCK
        cancelIfDisallowed(event, player, event.block.location, action)
    }

    @EventHandler
    fun onArmorStandKnockbackByEntity(event: EntityKnockbackByEntityEvent) {
        if (event.entity !is ArmorStand) return

        // Get the entity as a player, or if entity is projectile get the projectile's shooter if it's a player
        val hitBy = event.hitBy
        val player: Player? = when (hitBy) {
            is Player -> hitBy
            is Projectile -> hitBy.shooter as? Player
            else -> null
        }
        if (player == null) return

        val action = PlayerActionType.PUSH_ARMOUR_STAND
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onAnimalKnockbackByEntity(event: EntityKnockbackByEntityEvent) {
        if (event.entity !is Animals && event.entity !is Villager) return

        // Get the entity as a player, or if entity is projectile get the projectile's shooter if it's a player
        val hitBy = event.hitBy
        val player: Player? = when (hitBy) {
            is Player -> hitBy
            is Projectile -> hitBy.shooter as? Player
            else -> null
        }
        if (player == null) return

        val action = PlayerActionType.DAMAGE_ANIMAL
        cancelIfDisallowed(event, player, event.entity.location, action)
    }

    @EventHandler
    fun onPlayerHarvestBlock(event: PlayerHarvestBlockEvent) {
        val action = PlayerActionType.HARVEST_CROP
        cancelIfDisallowed(event, event.player, event.harvestedBlock.location, action)
    }

    @EventHandler
    fun onCropFertilize(event: BlockFertilizeEvent) {
        val cropMaterials = setOf(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.COCOA, Material.SWEET_BERRIES, Material.GLOW_BERRIES,
            Material.MELON_STEM, Material.PUMPKIN_STEM
        )
        if (event.block.type !in cropMaterials) return
        val player = event.player ?: return

        val action = PlayerActionType.HARVEST_CROP
        cancelIfDisallowed(event, player, event.block.location, action)
    }


    @EventHandler
    fun onPlayerBedEnter(event: PlayerBedEnterEvent) {
        val action = PlayerActionType.SLEEP_IN_BED
        cancelIfDisallowed(event, event.player, event.bed.location, action)
    }

    @EventHandler
    fun onPlayerSetSpawn(event: PlayerSetSpawnEvent) {
        val location = event.location ?: return
        if (event.cause != PlayerSetSpawnEvent.Cause.BED &&
            event.cause != PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) return
        val action = PlayerActionType.SLEEP_IN_BED
        cancelIfDisallowed(event, event.player, location, action)
    }

    /**
     * Cancels the action of lightning attacks using a trident.
     *
     * This does not output an alert to the player when the action is performed as it could get annoying for the
     * alert to appear every time the player throw their trident, which still does projectile damage.
     */
    @EventHandler
    fun onLightningStrike(event: LightningStrikeEvent) {
        val player = event.lightning.causingPlayer ?: return
        val action = PlayerActionType.STRIKE_LIGHTNING
        when (isPlayerActionAllowed.execute(event.world.uid, player.uniqueId,
                event.lightning.location.toPosition2D(), action)) {
            is Denied -> {
                event.lightning.flashCount = 0
                event.lightning.lifeTicks = 0
            }
            else -> return
        }
    }

    /**
     * Cancels the effect of splash potions on passive mobs such as animals and villagers.
     *
     * This does not output an alert to the player when the action is performed as it could get annoying for the
     * alert to appear every time the player throws a potion at a group of mixed mobs.
     */
    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        val player = event.potion.shooter as? Player ?: return
        val action = PlayerActionType.POTION_ANIMAL
        for (entity in event.affectedEntities) {
            if (entity is Monster || entity is Player) return
            when (isPlayerActionAllowed.execute(event.entity.world.uid, player.uniqueId,
                entity.location.toPosition2D(), action)) {
                is Denied -> event.setIntensity(entity, 0.0)
                else -> continue
            }
        }
    }

    /**
     * Cancels the effect of lingering potions on passive mobs such as animals and villagers.
     *
     * This does not output an alert to the player when the action is performed as it could get annoying for the
     * alert to appear every time the player throws a potion at a group of mixed mobs.
     */
    @EventHandler
    fun onAreaEffectCloudApply(event: AreaEffectCloudApplyEvent) {
        val player = event.entity.source as? Player ?: return
        val cancelledEntities = mutableListOf<Entity>()
        val action = PlayerActionType.POTION_ANIMAL
        for (entity in event.affectedEntities) {
            if (entity is Monster || entity is Player) return
            when (isPlayerActionAllowed.execute(event.entity.world.uid, player.uniqueId,
                entity.location.toPosition2D(), action)) {
                is Denied -> cancelledEntities.add(entity)
                else -> continue
            }
        }
        event.affectedEntities.removeAll(cancelledEntities)
    }

    private fun cancelIfDisallowed(event: Cancellable, player: Player, location: Location, action: PlayerActionType) {
        val worldId = location.world.uid
        val position = location.toPosition2D()
        when (val result = isPlayerActionAllowed.execute(player.uniqueId, worldId, position, action)) {
            is Denied -> {
                event.isCancelled = true
                val playerName = Bukkit.getOfflinePlayer(result.claim.playerId).name ?: "(Name not found)"
                player.sendActionBar(
                    Component.text("You can't do that in ${playerName}'s claim!") .color(TextColor.color(255, 85, 85)))
            }
            else -> return
        }
    }
}