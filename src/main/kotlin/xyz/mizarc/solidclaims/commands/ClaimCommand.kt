package xyz.mizarc.solidclaims.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Syntax
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import xyz.mizarc.solidclaims.SolidClaims
import xyz.mizarc.solidclaims.getClaimTool


open class ClaimCommand : BaseCommand() {
    @Dependency
    lateinit var plugin : SolidClaims

    @CommandAlias("claim")
    @CommandPermission("solidclaims.command.claim")
    @Syntax("claim")
    fun onClaim(player: Player) {
        if (isItemInInventory(player.inventory)) {
            player.sendMessage("§cYou already have the claim tool in your inventory.")
            return
        }

        player.inventory.addItem(getClaimTool())
        player.sendMessage("§aYou have been given the claim tool")
    }

    /**
     * Check if item is already in the player's inventory
     * @param inventory The provided inventory
     * @return True if the item exists in the inventory
     */
    fun isItemInInventory(inventory: PlayerInventory) : Boolean {
        for (item in inventory.contents) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return true
            }
        }
        return false
    }
}