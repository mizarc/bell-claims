package dev.mizarc.bellclaims.interaction.listeners

import com.destroystokyo.paper.MaterialTags
import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.actions.claim.IsWorldActionAllowed
import dev.mizarc.bellclaims.application.results.claim.GetClaimAtPositionResult
import dev.mizarc.bellclaims.application.results.claim.IsWorldActionAllowedResult.Denied
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Position2D
import dev.mizarc.bellclaims.domain.values.Position3D
import dev.mizarc.bellclaims.domain.values.WorldActionType
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toLocation
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition2D
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.data.Directional
import org.bukkit.entity.AbstractVillager
import org.bukkit.entity.Animals
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Arrow
import org.bukkit.entity.Blaze
import org.bukkit.entity.Creeper
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Fireball
import org.bukkit.entity.Ghast
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Monster
import org.bukkit.entity.Painting
import org.bukkit.entity.Pillager
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Snowball
import org.bukkit.entity.Snowman
import org.bukkit.entity.Wither
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockSpreadEvent
import org.bukkit.event.block.SpongeAbsorbEvent
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.EntityBreakDoorEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.event.weather.LightningStrikeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.projectiles.BlockProjectileSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WorldClaimProtectionListener: Listener, KoinComponent {
    private val isWorldActionAllowed: IsWorldActionAllowed by inject()
    private val getClaimAtPosition: GetClaimAtPosition by inject()

    @EventHandler
    fun onBlockBurn(event: BlockBurnEvent) {
        val action = WorldActionType.FIRE_BURN
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    fun onFireSpread(event: BlockSpreadEvent) {
        if (event.source.type != Material.FIRE) return
        val action = WorldActionType.FIRE_SPREAD
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    fun onMobBlockChange(event: EntityChangeBlockEvent) {
        if (event.entity is Player) return
        val action = WorldActionType.MOB_DESTROY_BLOCK
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    fun onMobBreakDoor(event: EntityBreakDoorEvent) {
        if (event.entity !is Monster) return
        if (event.entity !is Monster) return
        val action = WorldActionType.MOB_DESTROY_BLOCK
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    @Suppress("UnstableApiUsage")
    fun onEntityDamageByMobEvent(event: EntityDamageByEntityEvent) {
        if (event.damager::class !in listOf(Monster::class, Arrow::class, Fireball::class, Snowball::class)) return
        if (event.damageSource.causingEntity is Player) return
        if (event.entity !is Monster) return
        val action = WorldActionType.MOB_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onHangingBreakByMobEvent(event: HangingBreakByEntityEvent) {
        if (event.remover::class !in setOf(Skeleton::class, Blaze::class, Ghast::class, Snowman::class,
                Pillager::class, Wither::class, Creeper::class)) return
        val action = WorldActionType.MOB_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onMobPotionSplash(event: PotionSplashEvent) {
        // Filter to only projectiles thrown by monsters
        val projectile = event.entity as Projectile
        val monster = projectile.shooter as? Monster
        if (monster == null) return

        // For the splash affect, cancel out non-monster mobs that are inside the claim
        val action = WorldActionType.MOB_DAMAGE_ENTITY
        for (entity in event.affectedEntities) {
            when (isWorldActionAllowed.execute(entity.location.world.uid, entity.location.toPosition2D(), action)) {
                is Denied -> if (entity is Animals || entity is AbstractVillager) event.setIntensity(entity, 0.0)
                else -> continue
            }
        }
    }

    @EventHandler
    fun onCreeperExplodeEvent(event: EntityExplodeEvent) {
        if (event.entity !is Creeper) return
        val action = WorldActionType.MOB_DESTROY_BLOCK
        val cancelledBlocks = fetchBlocksToCancel(event.blockList(), action)
        event.blockList().removeAll(cancelledBlocks)
    }

    @EventHandler
    fun onEntityDamageByCreeperEvent(event: EntityDamageByEntityEvent) {
        if (event.damager !is Creeper) return
        if (event.entity::class !in setOf(ArmorStand::class, ItemFrame::class, Painting::class)) return
        val action = WorldActionType.MOB_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
        // Get claim that piston occupies
        val pistonClaim = when (
            val result = getClaimAtPosition.execute(event.block.world.uid, event.block.location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> null
        }

        // Get position of the piston head
        val affectedLocations = mutableSetOf<Location>()
        val direction = event.direction.direction
        affectedLocations.add(event.block.location.add(direction))

        // Get locations of all the blocks the piston will affect
        for (block in event.blocks) {
            val newBlockPosition = block.location.clone()
            newBlockPosition.add(direction)
            affectedLocations.add(newBlockPosition)
        }

        // Perform checks to see if in claim, and if the claim has piston flag
        val action = WorldActionType.PISTON_EXTEND
        for (location in affectedLocations) {
            // Get claim that the block being moved occupies
            val blockClaim = when (
                val result = getClaimAtPosition.execute(location.world.uid, location.toPosition2D())) {
                is GetClaimAtPositionResult.Success -> result.claim
                else -> null
            }

            // If they're in the same claim, bypass check
            if (blockClaim == pistonClaim) continue

            // Cancel if claim being moved into doesn't allow the action
            when (isWorldActionAllowed.execute(location.world.uid, location.toPosition2D(), action)) {
                is Denied -> {
                    event.isCancelled = true
                    return
                }
                else -> return
            }
        }
    }

    @EventHandler
    fun onBlockPistonRetract(event: BlockPistonRetractEvent) {
        // Get claim the piston is in
        val pistonClaim = when (
            val result = getClaimAtPosition.execute(event.block.world.uid, event.block.location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> null
        }

        // Perform checks to see if in claim, and if the claim has piston flag
        val action = WorldActionType.PISTON_RETRACT
        for (block in event.blocks) {
            // Get claim that the block being moved occupies
            val blockClaim = when (
                val result = getClaimAtPosition.execute(block.location.world.uid, block.location.toPosition2D())) {
                is GetClaimAtPositionResult.Success -> result.claim
                else -> null
            }

            // If they're in the same claim, bypass check
            if (blockClaim == pistonClaim) continue

            // Cancel if claim the blocks are being moved in doesn't allow the action
            when (isWorldActionAllowed.execute(block.world.uid, block.location.toPosition2D(), action)) {
                is Denied -> {
                    event.isCancelled = true
                    return
                }
                else -> return
            }
        }
    }

    @EventHandler
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        if (event.entity is Creeper) return
        val action = WorldActionType.ENTITY_EXPLOSION_DESTROY_BLOCK
        val cancelledBlocks = fetchBlocksToCancel(event.blockList(), action)
        event.blockList().removeAll(cancelledBlocks)
    }

    @EventHandler
    fun onBlockExplodeEvent(event: EntityExplodeEvent) {
        val action = WorldActionType.BLOCK_EXPLOSION_DESTROY_BLOCK
        val cancelledBlocks = fetchBlocksToCancel(event.blockList(), action)
        event.blockList().removeAll(cancelledBlocks)
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.damager is Creeper) return
        if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return
        if (event.entity !is ArmorStand && event.entity !is ItemFrame && event.entity !is Painting) return
        val action = WorldActionType.ENTITY_EXPLOSION_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onEntityDamageByBlockEvent(event: EntityDamageByBlockEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return
        if (event.entity !is ArmorStand) return
        val action = WorldActionType.BLOCK_EXPLOSION_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onHangingBreakByEntityEvent(event: HangingBreakByEntityEvent) {
        if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) return
        if (event.remover is Creeper) return
        val action = WorldActionType.ENTITY_EXPLOSION_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onHangingBreak(event: HangingBreakEvent) {
        if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) return
        val action = WorldActionType.ENTITY_EXPLOSION_DAMAGE_ENTITY
        cancelIfDisallowed(event, event.entity.location, action)
    }

    @EventHandler
    fun onBlockFromTo(event: BlockFromToEvent) {
        // Check if the location being moved to is a different claim, always allow in same claim
        val sourceClaim: Claim? = getClaimOrNull(event.block.location)
        val destinationClaim: Claim? = getClaimOrNull(event.toBlock.location)
        if (sourceClaim == destinationClaim) return

        // Cancel if fluid flow is disallowed
        val action = WorldActionType.FLUID_FLOW
        cancelIfDisallowed(event, event.toBlock.location, action)
    }

    @EventHandler
    fun onStructureGrowth(event: StructureGrowEvent) {
        val action = WorldActionType.TREE_GROWTH
        val sourceClaim: Claim? = getClaimOrNull(event.location)
        for (block in event.blocks) {
            val growthClaim = getClaimOrNull(block.location)
            if (sourceClaim == growthClaim) continue
            cancelIfDisallowed(event, block.location, action)
        }
    }

    @EventHandler
    fun onSculkSpread(event: BlockSpreadEvent) {
        // Filter to only sculk spread
        if (event.source.type != Material.SCULK_CATALYST) return

        // Check if the location being moved to is a different claim, always allow in same claim
        val sourceClaim: Claim? = getClaimOrNull(event.source.location)
        val destinationClaim: Claim? = getClaimOrNull(event.block.location)
        if (sourceClaim == destinationClaim) return

        // Cancel if spread is disallowed
        val action = WorldActionType.SPREAD
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    fun onBlockDispense(event: BlockDispenseEvent) {
        // Check if it is being dispensed into a different claim, always allow in same claim
        val directionalBlock = event.block.blockData as Directional
        val sourceClaim = getClaimOrNull(event.block.location)
        val destinationClaim = getClaimOrNull(event.block.location.add(directionalBlock.facing.direction))
        if (sourceClaim == destinationClaim) return

        // Cancel if dispense is disallowed
        val action = WorldActionType.DISPENSE
        cancelIfDisallowed(event, event.block.location, action)
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        // Filter to only potions thrown by dispensers
        val projectile = event.entity as Projectile
        val dispenser = projectile.shooter as? BlockProjectileSource
        if (dispenser == null) return

        // Check if potion being dispensed into a different claim, always allow in same claim
        val sourceClaim = getClaimOrNull(dispenser.block.location)
        val destinationClaim = getClaimOrNull(projectile.location)
        if (sourceClaim != destinationClaim) return

        // Cancel if dispense is disallowed
        val action = WorldActionType.DISPENSE
        cancelIfDisallowed(event, projectile.location, action)

        // For splash effects on the outer edge of a claim, cancel out non-monster mobs that are inside the claim
        for (entity in event.affectedEntities) {
            val entityClaim = getClaimOrNull(entity.location)
            if (sourceClaim == entityClaim) continue
            if (entity !is Monster) event.setIntensity(entity, 0.0)
        }
    }

    @EventHandler
    fun onAreaEffectCloudApply(event: AreaEffectCloudApplyEvent) {
        // Filter to only potions thrown by dispensers
        if (event.entity.source is Player || event.entity.source is Monster) return
        val potionClaim = getClaimOrNull(event.entity.location)

        // Check each entity to see if they should be affected
        val affectedEntities = event.affectedEntities.toMutableList()
        for (entity in event.affectedEntities) {
            val entityClaim = getClaimOrNull(entity.location)
            if (potionClaim == entityClaim) continue
            if (entity !is Monster) affectedEntities.remove(entity)
        }
        event.affectedEntities.clear()
        event.affectedEntities.addAll(affectedEntities)
    }

    @EventHandler
    fun onLingeringPotionSplash(event: LingeringPotionSplashEvent) {
        // Filter to only potions thrown by dispensers
        val projectile = event.entity
        val dispenser = projectile.shooter as? BlockProjectileSource
        if (dispenser == null) return

        // Cancel if dispense is disallowed
        val sourceClaim = getClaimOrNull(dispenser.block.location)
        val destinationClaim = getClaimOrNull(projectile.location)
        if (sourceClaim != destinationClaim) return

        // Cancel if dispense is disallowed
        val action = WorldActionType.DISPENSE
        cancelIfDisallowed(event, projectile.location, action)
    }

    @EventHandler
    fun onSpongeAbsorb(event: SpongeAbsorbEvent) {
        val action = WorldActionType.FLUID_ABSORB

        val sourceClaim = getClaimOrNull(event.block.location)
        val cancelledBlocks: MutableList<BlockState> = mutableListOf()
        for (block in event.blocks) {
            val affectedClaim = getClaimOrNull(block.location)
            if (sourceClaim == affectedClaim) continue
            when (isWorldActionAllowed.execute(block.location.world.uid, block.location.toPosition2D(), action)) {
                is Denied -> event.isCancelled = true
                else -> return
            }
        }
        event.blocks.removeAll(cancelledBlocks)
    }

    @EventHandler
    fun onLightningStrike(event: LightningStrikeEvent) {
        if (event.cause == LightningStrikeEvent.Cause.TRIDENT) return
        val action = WorldActionType.LIGHTNING_DAMAGE
        when (isWorldActionAllowed.execute(event.world.uid, event.lightning.location.toPosition2D(), action)) {
            is Denied -> {
                event.lightning.lifeTicks = 0
                event.lightning.flashCount = 0
            }
            else -> return
        }
    }

    @EventHandler
    fun onFallingBlockChange(event: EntityChangeBlockEvent) {
        // Filter to only falling blocks
        val fallingBlock = event.entity as? FallingBlock ?: return
        if (event.to == Material.AIR) return

        // Extract the origin location from the entity if it exists
        val originLocation = fallingBlock.getMetadata("origin_location").firstOrNull()?.value() as? Location ?: return

        // Check if block is falling into different claim, always allow in same claim
        val sourceClaim = getClaimOrNull(originLocation)
        val destinationClaim = getClaimOrNull(event.block.location)
        if (sourceClaim == destinationClaim) return

        // Drop the block as an item
        val itemStack = ItemStack(fallingBlock.blockData.material, 1)
        event.isCancelled = true
        event.block.world.dropItemNaturally(fallingBlock.location, itemStack)
    }

    @EventHandler
    fun onBlockForm(event: BlockFormEvent) {
        // Check to only prevent concrete powder and lava
        if (event.block.type !in MaterialTags.CONCRETE_POWDER.values && event.block.type != Material.LAVA) return

        // Check all directions that could cause the block form
        val formClaim = getClaimOrNull(event.block.location)
        val directions = setOf(Position2D(1, 0), Position2D(-1, 0), Position2D(0, 1), Position2D(0, -1))
        for (direction in directions) {
            val formPosition = event.block.location.toPosition3D()
            val checkPosition = Position3D(formPosition.x + direction.x, formPosition.y,
                formPosition.z + direction.z)
            val checkBlock = event.block.world.getBlockAt(checkPosition.x, checkPosition.y, checkPosition.z)

            // Check if adjacent block is water
            if (checkBlock.type != Material.WATER) continue

            // Check if water is in same claim as the block it is forming
            val checkClaim = getClaimOrNull(checkPosition.toLocation(event.block.world))
            if (formClaim == checkClaim) return
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onVehicleEnter(event: VehicleEnterEvent) {
        if (event.entered is Monster || event.entered is Player) return
        val action = WorldActionType.ANIMAL_ENTER_VEHICLE
        cancelIfDisallowed(event, event.entered.location, action)
    }

    private fun cancelIfDisallowed(event: Cancellable, location: Location, action: WorldActionType) {
        val worldId = location.world.uid
        val position = location.toPosition2D()
        when (isWorldActionAllowed.execute(worldId, position, action)) {
            is Denied -> event.isCancelled = true
            else -> return
        }
    }

    private fun fetchBlocksToCancel(blocks: List<Block>, action: WorldActionType): List<Block> {
        val deniedBlocks = mutableListOf<Block>()
        for (block in blocks) {
            val worldId = block.world.uid
            val position = block.location.toPosition2D()
            when (isWorldActionAllowed.execute(worldId, position, action)) {
                is Denied -> deniedBlocks.add(block)
                else -> {}
            }
        }
        return deniedBlocks
    }

    private fun getClaimOrNull(location: Location): Claim? {
        return when (val result = getClaimAtPosition.execute(location.world.uid, location.toPosition2D())) {
            is GetClaimAtPositionResult.Success -> result.claim
            else -> null
        }
    }
}