package dev.mizarc.bellclaims.interaction.behaviours

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.FlagService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.flags.Flag
import org.bukkit.Location
import org.bukkit.block.BlockState
import org.bukkit.block.data.Directional
import org.bukkit.entity.*
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.EntityBreakDoorEvent
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.weather.LightningStrikeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.inventory.ItemStack

/**
 * A data structure that contains the type of event [eventClass], the function to handle the result of the event
 * [handler], and a method to obtain all the claims that the event is affecting [getClaims].
 */
data class RuleExecutor(val eventClass: Class<out Event>,
                        val handler: (event: Event, claimService: ClaimService,
                                      partitionService: PartitionService, flagService: FlagService) -> Boolean,
                        val getClaims: (event: Event, claimService: ClaimService,
                                        partitionService: PartitionService) -> List<Claim>)

/**
 * A static class object to define the behaviour of event handling for events that affect claims which do not specify
 * rules that allow them to.
 */
class RuleBehaviour {
    @Suppress("unused")
    companion object {
        val fireBurn = RuleExecutor(BlockBurnEvent::class.java,
            Companion::cancelEvent, Companion::blockInClaim)
        val fireSpread = RuleExecutor(BlockSpreadEvent::class.java,
            Companion::cancelFireSpread, Companion::blockSpreadInClaim)
        val mobBlockChange = RuleExecutor(EntityChangeBlockEvent::class.java,
            Companion::cancelEntityBlockChange, Companion::entityGriefInClaim)
        val mobBreakDoor = RuleExecutor(EntityBreakDoorEvent::class.java,
            Companion::cancelEntityBreakDoor, Companion::entityBreakDoorInClaim)
        val mobDamageStaticEntity = RuleExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelMobEntityDamage, Companion::entityDamageInClaim)
        val mobHangingDamage = RuleExecutor(HangingBreakByEntityEvent::class.java,
            Companion::cancelMobHangingDamage, Companion::hangingBreakByEntityInClaim)
        val creeperExplode = RuleExecutor(EntityExplodeEvent::class.java,
            Companion::cancelCreeperExplode, Companion::entityExplosionInClaim)
        val creeperDamageStaticEntity = RuleExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelCreeperDamage, Companion::entityDamageInClaim)
        val creeperDamageHangingEntity = RuleExecutor(HangingBreakByEntityEvent::class.java,
            Companion::cancelCreeperHangingDamage, Companion::hangingBreakByEntityInClaim)
        val pistonExtend = RuleExecutor(BlockPistonExtendEvent::class.java,
            Companion::cancelPistonExtend, Companion::pistonExtendInClaim)
        val pistonRetract = RuleExecutor(BlockPistonRetractEvent::class.java,
            Companion::cancelPistonRetract, Companion::pistonRetractInClaim)
        val entityExplode = RuleExecutor(EntityExplodeEvent::class.java,
            Companion::preventExplosionDamage, Companion::entityExplosionInClaim)
        val blockExplode = RuleExecutor(BlockExplodeEvent::class.java,
            Companion::preventExplosionDamage, Companion::blockExplosionInClaim)
        val entityExplodeDamage = RuleExecutor(EntityDamageByEntityEvent::class.java,
            Companion::cancelEntityExplosionDamage, Companion::entityDamageInClaim)
        val blockExplodeDamage = RuleExecutor(EntityDamageByBlockEvent::class.java,
            Companion::cancelBlockExplosionDamage, Companion::blockDamageInClaim)
        val entityExplodeHangingDamage = RuleExecutor(HangingBreakByEntityEvent::class.java,
            Companion::cancelEntityExplosionHangingDamage, Companion::hangingBreakByEntityInClaim)
        val blockExplodeHangingDamage = RuleExecutor(HangingBreakEvent::class.java,
            Companion::cancelBlockExplosionHangingDamage, Companion::hangingBreakByBlockInClaim)
        val fluidFlow = RuleExecutor(BlockFromToEvent::class.java, Companion::cancelFluidFlow,
            Companion::fluidFlowSourceInClaim)
        val treeGrowth = RuleExecutor(StructureGrowEvent::class.java, Companion::cancelTreeGrowth,
            Companion::treeGrowthInClaim)
        val sculkSpread = RuleExecutor(BlockSpreadEvent::class.java, Companion::cancelSculkSpread,
            Companion::blockSpreadInClaim)
        val dispense = RuleExecutor(BlockDispenseEvent::class.java, Companion::cancelBlockDispenseEvent,
            Companion::blockDispenseInClaim)
        val dispensedSplashPotion = RuleExecutor(PotionSplashEvent::class.java,
            Companion::cancelSplashPotionEffect, Companion::potionSplashInClaim)
        val dispensedLingeringPotion = RuleExecutor(AreaEffectCloudApplyEvent::class.java,
            Companion::cancelLingeringPotionEffect, Companion::areaEffectCloudApplyInClaim)
        val spongeAbsorb = RuleExecutor(SpongeAbsorbEvent::class.java, Companion::cancelSpongeAbsorbEvent,
            Companion::spongeAbsorbInClaim)
        val lightningDamage = RuleExecutor(LightningStrikeEvent::class.java, Companion::cancelLightningStrikeEvent,
            Companion::lightningStrikeInClaim)
        val blockFall = RuleExecutor(EntityChangeBlockEvent::class.java, Companion::cancelBlockFall,
            Companion::entityChangeBlockInClaim)

        /**
         * Cancel any cancellable event.
         */
        private fun cancelEvent(event: Event, claimService: ClaimService,
                                partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event is Cancellable) {
                event.isCancelled = true
                return true
            }
            return false
        }

        private fun cancelMobHangingDamage(event: Event, claimService: ClaimService,
                                                       partitionService: PartitionService,
                                                       flagService: FlagService): Boolean {
            if (event !is HangingBreakByEntityEvent) return false
            if (event.remover !is Skeleton
                && event.remover !is Blaze
                && event.remover !is Ghast
                && event.remover !is Snowman
                && event.remover !is Pillager
                && event.remover !is Wither) return false
            if (event.entity !is ItemFrame && event.entity !is Painting) return false
            event.isCancelled = true
            return true
        }

        private fun cancelEntityExplosionHangingDamage(event: Event, claimService: ClaimService,
                                                       partitionService: PartitionService,
                                                       flagService: FlagService): Boolean {
            if (event !is HangingBreakByEntityEvent) return false
            if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) return false
            if (event.remover is Creeper) return false
            event.isCancelled = true
            return true
        }

        private fun cancelBlockExplosionHangingDamage(event: Event, claimService: ClaimService,
                                                      partitionService: PartitionService,
                                                      flagService: FlagService): Boolean {
            if (event !is HangingBreakEvent) return false
            if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) return false
            event.isCancelled = true
            return true
        }

        private fun cancelCreeperHangingDamage(event: Event, claimService: ClaimService,
                                               partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is HangingBreakByEntityEvent) return false
            if (event.remover !is Creeper) return false
            event.isCancelled = true
            return true
        }

        private fun hangingBreakByBlockInClaim(event: Event, claimService: ClaimService,
                                                partitionService: PartitionService): List<Claim> {
            if (event !is HangingBreakEvent) return listOf()
            val claimList = ArrayList<Claim>()
            val partition = partitionService.getByLocation(event.entity.location)
            if (partition != null) {
                val claim = claimService.getById(partition.claimId) ?: return listOf()
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun hangingBreakByEntityInClaim(event: Event, claimService: ClaimService,
                                                partitionService: PartitionService): List<Claim> {
            if (event !is HangingBreakByEntityEvent) return listOf()
            val claimList = ArrayList<Claim>()
            val partition = partitionService.getByLocation(event.entity.location)
            if (partition != null) {
                val claim = claimService.getById(partition.claimId) ?: return listOf()
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun cancelBlockExplosionDamage(event: Event, claimService: ClaimService,
                                               partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityDamageByBlockEvent) return false
            if (event.damager is Creeper) return false
            if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return false
            if (event.entity !is ArmorStand) return false
            event.isCancelled = true
            return true
        }

        private fun cancelEntityExplosionDamage(event: Event, claimService: ClaimService,
                                                partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.damager is Creeper) return false
            if (event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return false
            if (event.entity !is ArmorStand && event.entity !is ItemFrame && event.entity !is Painting) return false
            event.isCancelled = true
            return true
        }

        private fun cancelEntityBreakDoor(event: Event, claimService: ClaimService,
                                          partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityBreakDoorEvent) return false
            if (event.entity !is Monster) return false
            event.isCancelled = true
            return true
        }

        private fun entityBreakDoorInClaim(event: Event, claimService: ClaimService,
                                           partitionService: PartitionService): List<Claim> {
            if (event !is EntityBreakDoorEvent) return listOf()
            val claimList = ArrayList<Claim>()
            val partition = partitionService.getByLocation(event.entity.location)
            if (partition != null) {
                val claim = claimService.getById(partition.claimId) ?: return listOf()
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun blockDamageInClaim(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService): List<Claim> {
            if (event !is EntityDamageByBlockEvent) return listOf()
            val claimList = ArrayList<Claim>()
            val partition = partitionService.getByLocation(event.entity.location)
            if (partition != null) {
                val claim = claimService.getById(partition.claimId) ?: return listOf()
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun entityDamageInClaim(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService): List<Claim> {
            if (event !is EntityDamageByEntityEvent) return listOf()
            val claimList = ArrayList<Claim>()
            val partition = partitionService.getByLocation(event.entity.location)
            if (partition != null) {
                val claim = claimService.getById(partition.claimId) ?: return listOf()
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun cancelCreeperDamage(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.damager !is Creeper) return false
            if (event.entity !is ArmorStand && event.entity !is ItemFrame && event.entity !is Painting) return false
            event.isCancelled = true
            return true
        }

        private fun cancelMobEntityDamage(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityDamageByEntityEvent) return false
            if (event.damager !is Monster && event.damager !is Arrow &&
                event.damager !is Fireball && event.damager !is Snowball) return false
            if (event.damageSource.causingEntity is Player) return false
            if (event.entity is Player || event.entity is Monster) return false
            event.isCancelled = true
            return true
        }

        private fun cancelCreeperExplode(event: Event, claimService: ClaimService,
                                         partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityExplodeEvent) return false
            if (event.entity !is Creeper) return false
            val blocks = getCreeperExplosionBlocks(
                 event.blockList(), event.location.world, claimService, partitionService, flagService)
            event.blockList().removeAll(blocks)
            return true
        }

        private fun cancelEntityBlockChange(event: Event, claimService: ClaimService,
                                            partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityChangeBlockEvent) return false
            if (event.entity !is Monster) return false
            event.isCancelled = true
            return true
        }

        /**
         * Allow explosions to occur, but prevent them from destroying blocks in claims that do not explicitly allow it.
         */
        private fun preventExplosionDamage(event: Event, claimService: ClaimService,
                                           partitionService: PartitionService, flagService: FlagService): Boolean {
            val blocks: List<Block>
            if (event is EntityExplodeEvent) {
                if (event.entity is Creeper) return false
                blocks = getExplosionBlocks(event.blockList(), event.location.world, claimService,
                    partitionService, flagService)
                event.blockList().removeAll(blocks)
                return true
            }
            else if (event is BlockExplodeEvent) {
                blocks = getExplosionBlocks(event.blockList(), event.block.world, claimService,
                    partitionService, flagService)
                event.blockList().removeAll(blocks)
                return true
            }
            return false
        }

        private fun getCreeperExplosionBlocks(blocks: MutableList<Block>, world: World,
                                              claimService: ClaimService, partitionService: PartitionService,
                                              flagService: FlagService): List<Block> {
            val cancelledBlocks: MutableList<Block> = mutableListOf()
            for (block in blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                if (!flagService.doesClaimHaveFlag(claim, Flag.MobGriefing)) {
                    cancelledBlocks.add(block)
                }
            }
            return cancelledBlocks
        }

        /**
         * Edit the explosion's destruction to exclude blocks inside of claims without the rule for it.
         */
        private fun getExplosionBlocks(blocks: MutableList<Block>, world: World,
                                       claimService: ClaimService, partitionService: PartitionService,
                                       flagService: FlagService): List<Block> {
            val cancelledBlocks: MutableList<Block> = mutableListOf()
            for (block in blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                if (!flagService.doesClaimHaveFlag(claim, Flag.Explosions)) {
                    cancelledBlocks.add(block)
                }
            }
            return cancelledBlocks
        }

        /**
         * Get claims which this block resides in.
         */
        private fun blockInClaim(event: Event, claimService: ClaimService,
                                 partitionService: PartitionService): List<Claim> {
            if (event !is BlockEvent) return listOf()
            val partition = partitionService.getByLocation(event.block.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId)
            return listOf(claim ?: return listOf()).distinct()
        }

        private fun blockSpreadInClaim(event: Event, claimService: ClaimService,
                                      partitionService: PartitionService): List<Claim> {
            if (event !is BlockSpreadEvent) return listOf()
            val partition = partitionService.getByLocation(event.block.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim).distinct()
        }

        /**
         * Get claims which this explosion affects the blocks of.
         */
        private fun blockExplosionInClaim(e: Event, claimService: ClaimService,
                                          partitionService: PartitionService): List<Claim> {
            if (e !is BlockExplodeEvent) return listOf()
            return getExplosionClaims(e.blockList(), claimService, partitionService)
        }

        /**
         * Get claims which this explosion affects the blocks of.
         */
        private fun entityExplosionInClaim(e: Event, claimService: ClaimService,
                                           partitionService: PartitionService): List<Claim> {
            if (e !is EntityExplodeEvent) return listOf()
            return getExplosionClaims(e.blockList(), claimService, partitionService)
        }

        /**
         * Get claims that this explosion affects.
         */
        private fun getExplosionClaims(blocks: List<Block>, claimService: ClaimService,
                                       partitionService: PartitionService): List<Claim> {
            val claimList = ArrayList<Claim>()
            for (block in blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        /**
         * Get claims which this entity grief event resides in.
         */
        private fun entityGriefInClaim(event: Event, claimService: ClaimService,
                                       partitionService: PartitionService): List<Claim> {
            if (event !is EntityChangeBlockEvent) return listOf()
            val partition = partitionService.getByLocation(event.block.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim).distinct()
        }

        /**
         * Get claims for piston extends.
         */
        private fun pistonExtendInClaim(e: Event, claimService: ClaimService,
                                        partitionService: PartitionService): List<Claim> {
            if (e !is BlockPistonExtendEvent) return listOf()
            return getPistonClaims(e.block, e.blocks, e.direction, claimService, partitionService)
        }

        /**
         * Get claims for piston retracts.
         */
        private fun pistonRetractInClaim(e: Event, claimService: ClaimService,
                                         partitionService: PartitionService): List<Claim> {
            if (e !is BlockPistonRetractEvent) return listOf()
            return getPistonClaims(e.block, e.blocks, e.direction, claimService, partitionService)
        }

        /**
         * Get claims that this machine operates in, accounting for where the blocks will be if the piston event is
         * allowed to occur.
         */
        private fun getPistonClaims(pistonBlock: Block, blocks: List<Block>, direction: BlockFace, claimService: ClaimService,
                                    partitionService: PartitionService): List<Claim> {
            val claimList = ArrayList<Claim>()
            val checks: ArrayList<Block> = ArrayList()
            for (c in blocks) {
                checks.add(c.getRelative(direction))
            }

            // Check the piston head position
            val extendPosition = pistonBlock.location.add(direction.direction)
            partitionService.getByLocation(extendPosition)?.let { extendPartition ->
                claimService.getById(extendPartition.claimId)?.let { claim ->
                    claimList.add(claim)
                }
            }

            // Check the blocks the piston will be moving
            for (block in checks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                claimList.add(claim)
            }
            return claimList.distinct()
        }

        private fun cancelFluidFlow(event: Event, claimService: ClaimService,
                                    partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockFromToEvent) return false
            if (partitionService.getByLocation(event.block.location) != null) return false
            if (partitionService.getByLocation(event.toBlock.location) == null) return false
            event.isCancelled = true
            return true
        }

        private fun fluidFlowSourceInClaim(event: Event, claimService: ClaimService,
                                           partitionService: PartitionService): List<Claim> {
            if (event !is BlockFromToEvent) return listOf()
            val partition = partitionService.getByLocation(event.toBlock.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim).distinct()
        }

        private fun cancelPistonRetract(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockPistonRetractEvent) return false
            if (partitionService.getByLocation(event.block.location) != null) return false
            var blockInClaim = false
            for (block in event.blocks) {
                if (partitionService.getByLocation(block.location) != null) blockInClaim = true
            }
            if (!blockInClaim) return false
            event.isCancelled = true
            return true
        }

        private fun cancelPistonExtend(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockPistonExtendEvent) return false
            if (partitionService.getByLocation(event.block.location) != null) return false
            val direction = event.direction.direction

            // Check the position of the piston head
            if (partitionService.getByLocation(event.block.location.add(direction)) != null) {
                event.isCancelled = true
                return true
            }

            // Check the blocks that the piston is pushinga
            var blockInClaim = false
            for (block in event.blocks) {
                val newBlockPosition = block.location.clone()
                newBlockPosition.add(direction)
                if (partitionService.getByLocation(block.location) != null ||
                    partitionService.getByLocation(newBlockPosition) != null) blockInClaim = true
            }

            if (!blockInClaim) return false
            event.isCancelled = true
            return true
        }

        private fun cancelTreeGrowth(event: Event, claimService: ClaimService,
                             partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is StructureGrowEvent) return false
            if (partitionService.getByLocation(event.location) != null) {
                val partition = partitionService.getByLocation(event.location) ?: return false
                val claim = claimService.getById(partition.claimId) ?: return false
                for (block in event.blocks) {
                    if (partitionService.getByLocation(block.location) != null) {
                        val otherPartition = partitionService.getByLocation(block.location) ?: continue
                        val otherClaim = claimService.getById(otherPartition.claimId) ?: continue
                        if (claim.id != otherClaim.id) {
                            partitionService.getByLocation(block.location) ?: continue
                            event.isCancelled = true
                            return true
                        }
                    }
                }
                return false
            }

            for (block in event.blocks) {
                if (partitionService.getByLocation(block.location) != null) {
                    event.isCancelled = true
                    return true
                }
            }
            return false
        }

        private fun treeGrowthInClaim(event: Event, claimService: ClaimService,
                                           partitionService: PartitionService): List<Claim> {
            if (event !is StructureGrowEvent) return listOf()
            val claims = mutableListOf<Claim>()
            for (block in event.blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                claims.add(claim)
            }
            return claims.distinct()
        }

        private fun cancelSculkSpread(event: Event, claimService: ClaimService,
                              partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockSpreadEvent) return false
            if (event.source.type == Material.SCULK_CATALYST) {
                val partition = partitionService.getByLocation(event.block.location)
                val sourcePartition = partitionService.getByLocation(event.source.location)

                if (sourcePartition == partition) {
                    return false
                }

                if (sourcePartition == null) {
                    event.isCancelled = true
                    return true
                }
            }
            return false
        }

        private fun cancelFireSpread(event: Event, claimService: ClaimService,
                                      partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockSpreadEvent) return false
            if (event.source.type != Material.FIRE) return false
            event.isCancelled = true
            return true
        }

        private fun blockDispenseInClaim(event: Event, claimService: ClaimService,
                                         partitionService: PartitionService): List<Claim> {
            if (event !is BlockDispenseEvent) return listOf()
            val directionalBlock = event.block.blockData as Directional
            val placeLocation = event.block.location.add(directionalBlock.facing.direction)
            val partition = partitionService.getByLocation(placeLocation) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim)
        }

        private fun spongeAbsorbInClaim(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService): List<Claim> {
            if (event !is SpongeAbsorbEvent) return listOf()
            val claims = mutableListOf<Claim>()
            for (block in event.blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                claims.add(claim)
            }
            return claims.distinct()
        }

        private fun cancelSpongeAbsorbEvent(event: Event, claimService: ClaimService,
                                            partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is SpongeAbsorbEvent) return false
            if (partitionService.getByLocation(event.block.location) != null) return false
            val cancelledBlocks: MutableList<BlockState> = mutableListOf()
            for (block in event.blocks) {
                val partition = partitionService.getByLocation(block.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                if (!flagService.doesClaimHaveFlag(claim, Flag.Explosions)) {
                    cancelledBlocks.add(block)
                }
            }
            event.blocks.removeAll(cancelledBlocks)
            return true
        }

        private fun lightningStrikeInClaim(event: Event, claimService: ClaimService,
                                           partitionService: PartitionService): List<Claim> {
            if (event !is LightningStrikeEvent) return listOf()
            val partition = partitionService.getByLocation(event.lightning.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim)
        }

        private fun cancelLightningStrikeEvent(event: Event, claimService: ClaimService,
                                            partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is LightningStrikeEvent) return false
            if (event.cause == LightningStrikeEvent.Cause.TRIDENT) return false
            event.lightning.lifeTicks = 0
            event.lightning.flashCount = 0
            return true
        }

        private fun entityChangeBlockInClaim(event: Event, claimService: ClaimService,
                                             partitionService: PartitionService): List<Claim> {
            if (event !is EntityChangeBlockEvent) return listOf()
            val partition = partitionService.getByLocation(event.block.location) ?: return listOf()
            val claim = claimService.getById(partition.claimId) ?: return listOf()
            return listOf(claim)
        }

        private fun cancelBlockFall(event: Event, claimService: ClaimService,
                                    partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is EntityChangeBlockEvent) return false
            val fallingBlock = event.entity as? FallingBlock ?: return false
            if (event.to == Material.AIR) return false
            val partition = partitionService.getByLocation(event.block.location) ?: return false
            val claim = claimService.getById(partition.claimId) ?: return false
            val originLocation = fallingBlock.getMetadata("origin_location")
                .firstOrNull()?.value() as? Location ?: return false

            // If originated from outside, cancel event
            val originPartition = partitionService.getByLocation(originLocation)
            if (originPartition != null && claimService.getById(originPartition.claimId) == claim) {
                return false
            }
            val itemStack = ItemStack(fallingBlock.blockData.material, 1)
            event.isCancelled = true
            event.block.world.dropItemNaturally(fallingBlock.location, itemStack)
            return true
        }

        private fun potionSplashInClaim(event: Event, claimService: ClaimService,
                                        partitionService: PartitionService): List<Claim> {
            if (event !is PotionSplashEvent) return listOf()
            val affectedClaims = mutableListOf<Claim>()
            for (entity in event.affectedEntities) {
                val partition = partitionService.getByLocation(entity.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                affectedClaims.add(claim)
            }
            return affectedClaims
        }

        private fun cancelSplashPotionEffect(event: Event, claimService: ClaimService,
                                             partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is PotionSplashEvent) return false
            for (entity in event.affectedEntities) {
                if (entity !is Monster) {
                    event.setIntensity(entity, 0.0)
                }
            }
            return true
        }

        private fun areaEffectCloudApplyInClaim(event: Event, claimService: ClaimService,
                                                partitionService: PartitionService): List<Claim> {
            if (event !is AreaEffectCloudApplyEvent) return listOf()
            val affectedClaims = mutableListOf<Claim>()
            for (entity in event.affectedEntities) {
                val partition = partitionService.getByLocation(entity.location) ?: continue
                val claim = claimService.getById(partition.claimId) ?: continue
                affectedClaims.add(claim)
            }
            return affectedClaims
        }

        private fun cancelLingeringPotionEffect(event: Event, claimService: ClaimService,
                                                partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is AreaEffectCloudApplyEvent) return false
            event.affectedEntities.removeAll { it !is Monster }
            return false
        }

        private fun cancelBlockDispenseEvent(event: Event, claimService: ClaimService,
                                             partitionService: PartitionService, flagService: FlagService): Boolean {
            if (event !is BlockDispenseEvent) return false
            val directionalBlock = event.block.blockData as Directional
            val placeLocation = event.block.location.add(directionalBlock.facing.direction)
            val placePartition = partitionService.getByLocation(placeLocation) ?: return false
            val placeClaim = claimService.getById(placePartition.claimId) ?: return false

            val partition = partitionService.getByLocation(event.block.location)
            if (partition == null) {
                event.isCancelled = true
                return true
            }

            val claim = claimService.getById(partition.claimId)
            if (claim == placeClaim) {
                return false
            }
            event.isCancelled = true
            return true
        }
    }
}