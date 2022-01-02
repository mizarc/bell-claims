package xyz.mizarc.solidclaims.events

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer

/**
 * A data structure that contains the type of event [eventClass], the function to handle the result of the event [handler],
 * and a method to obtain all the claims that the event is affecting [getClaims].
 */
data class RuleExecutor(val eventClass: Class<out Event>, val handler: (e: Event, cc: ClaimContainer) -> Unit, val getClaims: (e: Event, cc: ClaimContainer) -> List<Claim>)

/**
 * A static class object to define the behaviour of event handling for events that affect claims which do not specify
 * rules that allow them to.
 */
class RuleBehaviour {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        val fireBurn = RuleExecutor(BlockBurnEvent::class.java, ::cancelEvent, ::blockInClaim)
        val fireSpread = RuleExecutor(BlockSpreadEvent::class.java, ::cancelEvent, ::blockInClaim)
        val mobGriefing = RuleExecutor(EntityChangeBlockEvent::class.java, ::cancelEvent, ::entityGriefInClaim)
        val pistonExtend = RuleExecutor(BlockPistonExtendEvent::class.java, ::cancelEvent, ::pistonExtendInClaim)
        val pistonRetract = RuleExecutor(BlockPistonRetractEvent::class.java, ::cancelEvent, ::pistonRetractInClaim)
        val entityExplode = RuleExecutor(EntityExplodeEvent::class.java, ::preventExplosionDamage, ::entityExplosionInClaim)
        val blockExplode = RuleExecutor(BlockExplodeEvent::class.java, ::preventExplosionDamage, ::blockExplosionInClaim)

        /**
         * Cancel any cancellable event.
         */
        private fun cancelEvent(event: Event, cc: ClaimContainer) {
            if (event is Cancellable) {
                event.isCancelled = true
            }
        }

        /**
         * Allow explosions to occur, but prevent them from destroying blocks in claims that do not explicitly allow it.
         */
        private fun preventExplosionDamage(event: Event, cc: ClaimContainer) {
            if (event is EntityExplodeEvent) {
                handleExplosionBlocks(event.blockList(), cc)
            }
            if (event is BlockExplodeEvent) {
                handleExplosionBlocks(event.blockList(), cc)
            }
        }

        /**
         * Edit the explosion's destruction to exclude blocks inside of claims without the rule for it.
         */
        private fun handleExplosionBlocks(blocks: MutableList<Block>, cc: ClaimContainer) {
            val result: ArrayList<Block> = ArrayList()
            for (block in blocks) {
                val claim = cc.getClaimPartitionAtLocation(block.location)?.claim
                if (claim == null || claim.rules.contains(ClaimRule.Explosions)) {
                    result.add(block)
                }
            }
            blocks.clear()
            blocks.addAll(result)
        }

        /**
         * Get claims which this block resides in.
         */
        private fun blockInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockEvent) return listOf()
            return listOf(cc.getClaimPartitionAtLocation(e.block.location)?.claim ?: return listOf()).distinct()
        }

        /**
         * Get claims which this explosion affects the blocks of.
         */
        private fun blockExplosionInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockExplodeEvent) return listOf()
            return getExplosionClaims(e.blockList(), cc)
        }

        /**
         * Get claims which this explosion affects the blocks of.
         */
        private fun entityExplosionInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is EntityExplodeEvent) return listOf()
            return getExplosionClaims(e.blockList(), cc)
        }

        /**
         * Get claims that this explosion affects.
         */
        private fun getExplosionClaims(blocks: List<Block>, cc: ClaimContainer): List<Claim> {
            val claimList = ArrayList<Claim>()
            for (block in blocks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    claimList.add(part.claim)
                }
            }
            return claimList.distinct()
        }

        /**
         * Get claims which this entity grief event resides in.
         */
        private fun entityGriefInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is EntityChangeBlockEvent) return listOf()
            return listOf(cc.getClaimPartitionAtLocation(e.block.location)?.claim ?: return listOf()).distinct()
        }

        /**
         * Get claims for piston extends.
         */
        private fun pistonExtendInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonExtendEvent) return listOf()
            return getPistonClaims(e.blocks, e.direction, cc)
        }

        /**
         * Get claims for piston retracts.
         */
        private fun pistonRetractInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonRetractEvent) return listOf()
            return getPistonClaims(e.blocks, e.direction, cc)
        }

        /**
         * Get claims that this machine operates in, accounting for where the blocks will be if the piston event is
         * allowed to occur.
         */
        private fun getPistonClaims(blocks: List<Block>, direction: BlockFace, cc: ClaimContainer): List<Claim> {
            val claimList = ArrayList<Claim>()
            val checks: ArrayList<Block> = ArrayList()
            for (c in blocks) {
                checks.add(c.getRelative(direction))
            }
            for (block in checks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    claimList.add(part.claim)
                }
            }
            return claimList.distinct()
        }
    }
}