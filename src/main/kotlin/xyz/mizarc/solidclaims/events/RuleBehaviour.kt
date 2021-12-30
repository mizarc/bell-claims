package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer

/**
 * A data structure that contains the type of an event [eventClass], the function to handle the result of the event [handler],
 * and a method to obtain all the claims that the event is affecting [getClaims].
 */
data class RuleExecutor(val eventClass: Class<out Event>, val handler: (l: Listener, e: Event) -> Unit, val getClaims: (e: Event, cc: ClaimContainer) -> List<Claim>)

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
        val entityExplode = RuleExecutor(EntityExplodeEvent::class.java, ::cancelEvent, ::explosionInClaim)

        /**
         * Cancel any cancellable event.
         */
        private fun cancelEvent(listener: Listener, event: Event) {
            if (event is Cancellable) {
                event.isCancelled = true
            }
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
        private fun explosionInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is EntityExplodeEvent) return listOf()
            val claimList = ArrayList<Claim>()
            for (block in e.blockList()) {
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
         * Get claims that this piston machine operates in.
         */
        private fun pistonExtendInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonExtendEvent) return listOf()
            val claimList = ArrayList<Claim>()
            for (block in e.blocks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    claimList.add(part.claim)
                }
            }
            return claimList.distinct()
        }

        /**
         * Get claims that this piston machine operates in.
         */
        private fun pistonRetractInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonRetractEvent) return listOf()
            val claimList = ArrayList<Claim>()
            for (block in e.blocks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    claimList.add(part.claim)
                }
            }
            return claimList.distinct()
        }
    }
}