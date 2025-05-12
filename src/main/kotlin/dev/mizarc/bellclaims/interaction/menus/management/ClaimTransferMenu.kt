package dev.mizarc.bellclaims.interaction.menus.management

import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu
import dev.mizarc.bellclaims.utils.getLangText
import org.bukkit.entity.Player

class ClaimTransferMenu(private val menuNavigator: MenuNavigator, private val claim: Claim,
                        private val player: Player): Menu {
    override fun open() {
        val confirmAction: () -> Unit = {
            menuNavigator.openMenu(ClaimTransferNamingMenu(menuNavigator, claim, player))
        }
        menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player,
            getLangText("AcceptTransferClaim"), confirmAction))
    }
}