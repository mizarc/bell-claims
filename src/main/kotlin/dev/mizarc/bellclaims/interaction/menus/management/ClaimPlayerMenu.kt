package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.permission.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.createHead
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

class ClaimPlayerMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                      private val claim: Claim): Menu, KoinComponent {
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()

    private var page = 0

    override fun open() {
        val trustedPlayers = getPlayersWithPermissionInClaim.execute(claim.id)

        // Create trust menu
        val gui = ChestGui(6, "All Players")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls
        val controlsPane = addControlsSection(gui) { menuNavigator.goBack() }
        addPaginator(controlsPane, page, ceil(trustedPlayers.count() / 36.0).toInt())

        // Add player search item
        val playerSearchItem = ItemStack(Material.NAME_TAG)
            .name(getLangText("Search"))
            .lore(getLangText("FindPlayerByName"))
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

    private fun addControlsSection(gui: ChestGui, backButtonAction: () -> Unit): StaticPane {
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
            .name(getLangText("GoBack2"))

        val guiExitItem = GuiItem(exitItem) { backButtonAction() }
        controlsPane.addItem(guiExitItem, 0, 0)
        return controlsPane
    }

    private fun addPaginator(controlsPane: StaticPane, currentPage: Int, totalPages: Int) {
        // Add prev item
        val prevItem = ItemStack(Material.ARROW).name(getLangText("Prev"))
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER).name(getLangText("PageInfo1") + "$currentPage" + getLangText("PageInfo2") + "$totalPages")
        val guiPageItem = GuiItem(pageItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPageItem, 7, 0)

        // Add next item
        val nextItem = ItemStack(Material.ARROW).name(getLangText("Next"))
        val guiNextItem = GuiItem(nextItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiNextItem, 8, 0)
    }
}