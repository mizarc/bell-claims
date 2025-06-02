package dev.mizarc.bellclaims.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class ConfirmationMenu(val menuNavigator: MenuNavigator, val player: Player, val title: String,
                       val callbackAction: () -> Unit): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()

    override fun open() {
        val gui = HopperGui(title)
        val pane = StaticPane(1, 0, 3, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.slotsComponent.addPane(pane)
        val playerId = player.uniqueId

        // Add no menu item
        val noItem = ItemStack(Material.RED_CONCRETE)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_CONFIRMATION_ITEM_NO_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_CONFIRMATION_ITEM_NO_LORE))

        val guiNoItem = GuiItem(noItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_CONFIRMATION_ITEM_YES_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_CONFIRMATION_ITEM_YES_LORE))
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            callbackAction()
            menuNavigator.goBack()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(player)
    }
}