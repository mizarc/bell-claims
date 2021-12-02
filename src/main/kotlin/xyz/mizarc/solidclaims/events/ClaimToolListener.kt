package xyz.mizarc.solidclaims.events

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import xyz.mizarc.solidclaims.getClaimTool

class ClaimToolListener : Listener {
    @EventHandler
    fun onUseClaimTool(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.item == null) return
        if (event.item!!.itemMeta != getClaimTool()) return

        event.player.sendMessage("test")
    }
}