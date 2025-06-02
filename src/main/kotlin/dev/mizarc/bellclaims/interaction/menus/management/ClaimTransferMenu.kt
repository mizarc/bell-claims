package dev.mizarc.bellclaims.interaction.menus.management

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class ClaimTransferMenu(private val menuNavigator: MenuNavigator, private val claim: Claim,
                        private val player: Player): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    override fun open() {
        val playerId = player.uniqueId
        val confirmAction: () -> Unit = {
            menuNavigator.openMenu(ClaimTransferNamingMenu(menuNavigator, claim, player))
        }
        menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player,
            localizationProvider.get(playerId, LocalizationKeys.MENU_TRANSFER_TITLE), confirmAction))
    }
}