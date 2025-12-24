package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.player.RegisterClaimMenuOpening
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
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

class ClaimInfoMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                          private var claim: Claim): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val registerClaimMenuOpening: RegisterClaimMenuOpening by inject()

    override fun open() {
        val playerId = player.uniqueId
        val gui = ChestGui(1, localizationProvider.get(playerId, LocalizationKeys.MENU_MANAGEMENT_TITLE, claim.name))
        val pane = StaticPane(0, 0, 9, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.addPane(pane)

        // Add the back button
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME))
        val guiClaimToolItem = GuiItem(exitItem) { guiEvent ->
            menuNavigator.goBack()
        }
        pane.addItem(guiClaimToolItem, 0, 0)


        // Add a claim renaming button
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_RENAME_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_RENAME_NAME))
        val guiRenamingItem = GuiItem(renamingItem) { menuNavigator.openMenu(
            ClaimRenamingMenu(menuNavigator, player, claim)) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add a claim description button
        val playerTrustItem = ItemStack(Material.BOOK)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_DESCRIPTION_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_DESCRIPTION_NAME))
        val guiPlayerTrustItem = GuiItem(playerTrustItem) {
            menuNavigator.openMenu(ClaimDescriptionMenu(menuNavigator, player, claim)) }
        pane.addItem(guiPlayerTrustItem, 5, 0)

        // Add update icon menu button
        val iconEditorItem = ItemStack(Material.BELL)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_ICON_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_INFO_ITEM_ICON_LORE))
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            menuNavigator.openMenu(ClaimIconMenu(player, menuNavigator, claim)) }
        pane.addItem(guiIconEditorItem, 7, 0)

        // Register the player being in the menu and open it
        registerClaimMenuOpening.execute(player.uniqueId, claim.id)
        gui.show(player)
    }

    override fun passData(data: Any?) {
        claim = data as? Claim ?: return
    }
}