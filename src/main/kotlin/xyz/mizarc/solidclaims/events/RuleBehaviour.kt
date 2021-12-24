package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimContainer

data class RuleExecutor(val eventClass: Class<out Event>, val handler: (l: Listener, e: Event) -> Unit, val getClaims: (e: Event, cc: ClaimContainer) -> List<Claim>)

class RuleBehaviour {
    @Suppress("UNUSED_PARAMETER")
    companion object {
        val fireSpread = RuleExecutor(BlockIgniteEvent::class.java, ::cancelEvent, ::blockInClaim)
        val mobGriefing = RuleExecutor(EntityChangeBlockEvent::class.java, ::cancelEvent, ::entityGriefInClaim)
        val pistonExtend = RuleExecutor(BlockPistonExtendEvent::class.java, ::cancelEvent, ::pistonExtendInClaim)
        val pistonRetract = RuleExecutor(BlockPistonRetractEvent::class.java, ::cancelEvent, ::pistonRetractInClaim)
        val blockExplode = RuleExecutor(BlockExplodeEvent::class.java, ::cancelEvent, ::blockInClaim)

        private fun cancelEvent(listener: Listener, event: Event) {
            if (event is Cancellable) {
                event.isCancelled = true
            }
        }

        private fun blockInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockEvent) return listOf()
            return listOf(cc.getClaimPartitionAtLocation(e.block.location)?.claim ?: return listOf()).distinct()
        }

        private fun entityGriefInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is EntityChangeBlockEvent) return listOf()
            return listOf(cc.getClaimPartitionAtLocation(e.block.location)?.claim ?: return listOf()).distinct()
        }

        private fun pistonExtendInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonExtendEvent) return listOf()
            val blockList = ArrayList<Claim>()
            for (block in e.blocks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    blockList.add(part.claim)
                }
            }
            return blockList.distinct()
        }

        private fun pistonRetractInClaim(e: Event, cc: ClaimContainer): List<Claim> {
            if (e !is BlockPistonRetractEvent) return listOf()
            val blockList = ArrayList<Claim>()
            for (block in e.blocks) {
                val part = cc.getClaimPartitionAtLocation(block.location)
                if (part != null) {
                    blockList.add(part.claim)
                }
            }
            return blockList.distinct()
        }
    }
}