package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.permission.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.createHead
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.math.ceil

class ClaimPlayerMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                      private val claim: Claim): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()

    private var page = 0

    override fun open() {
        // Create trust menu
        val playerId = player.uniqueId
        val gui = ChestGui(6, localizationProvider.get(playerId, LocalizationKeys.MENU_ALL_PLAYERS_TITLE))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls
        val controlsPane = addControlsSection(playerId, gui) { menuNavigator.goBack() }
        val trustedPlayers = getPlayersWithPermissionInClaim.execute(claim.id)
        addPaginator(playerId, controlsPane, page, ceil(trustedPlayers.count() / 36.0).toInt())

        // Add player search item
        val playerSearchItem = ItemStack(Material.NAME_TAG)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_ALL_PLAYERS_ITEM_SEARCH_NAME))
            .lore(localizationProvider.get(playerId, LocalizationKeys.MENU_ALL_PLAYERS_ITEM_SEARCH_LORE))
        val guiPlayerSearchItem = GuiItem(playerSearchItem) {
            menuNavigator.openMenu(ClaimPlayerSearchMenu(menuNavigator, claim, player)) }
        controlsPane.addItem(guiPlayerSearchItem, 3, 0)

        // Add list of players
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (targetPlayer in Bukkit.getOnlinePlayers()) {
            if (targetPlayer.uniqueId == claim.playerId) {
                continue
            }

            val warpItem = createHead(Bukkit.getOfflinePlayer(targetPlayer.uniqueId))
                .name("${Bukkit.getOfflinePlayer(targetPlayer.uniqueId).name}")
            val guiWarpItem = GuiItem(warpItem) {
                menuNavigator.openMenu(ClaimPlayerPermissionsMenu(menuNavigator, player, claim, targetPlayer))
            }
            warpsPane.addItem(guiWarpItem, xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 8) {
                xSlot = 0
                ySlot += 1
            }
        }

        gui.show(player)
    }

    private fun addControlsSection(playerId: UUID, gui: ChestGui, backButtonAction: () -> Unit): StaticPane {
        // Add divider
        val dividerPane = StaticPane(0, 1, 9, 1)
        gui.addPane(dividerPane)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        for (slot in 0..8) {
            val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
            dividerPane.addItem(guiDividerItem, slot, 0)
        }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME))

        val guiExitItem = GuiItem(exitItem) { backButtonAction() }
        controlsPane.addItem(guiExitItem, 0, 0)
        return controlsPane
    }

    private fun addPaginator(playerId: UUID, controlsPane: StaticPane, currentPage: Int, totalPages: Int) {
        // Add prev item
        val prevItem = ItemStack(Material.ARROW)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_PREV_NAME))
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_PAGE_NAME,
                currentPage, totalPages))
        val guiPageItem = GuiItem(pageItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPageItem, 7, 0)

        // Add next item
        val nextItem = ItemStack(Material.ARROW)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_NEXT_NAME))
        val guiNextItem = GuiItem(nextItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiNextItem, 8, 0)
    }
}