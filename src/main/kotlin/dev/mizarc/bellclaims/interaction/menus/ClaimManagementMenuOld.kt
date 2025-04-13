package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.application.services.old.ClaimService
import dev.mizarc.bellclaims.application.services.old.ClaimWorldService
import dev.mizarc.bellclaims.application.services.old.DefaultPermissionService
import dev.mizarc.bellclaims.application.services.old.FlagService
import dev.mizarc.bellclaims.application.services.old.PlayerLimitService
import dev.mizarc.bellclaims.application.services.old.PlayerPermissionService
import dev.mizarc.bellclaims.application.services.old.PlayerStateService
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.infrastructure.getClaimMoveTool
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu.Companion.openConfirmationMenu
import dev.mizarc.bellclaims.utils.*
import org.bukkit.event.inventory.ClickType
import kotlin.math.ceil

import dev.mizarc.bellclaims.utils.getLangText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class ClaimManagementMenuOld(private val claimService: ClaimService,
                             private val claimWorldService: ClaimWorldService,
                             private val flagService: FlagService,
                             private val defaultPermissionService: DefaultPermissionService,
                             private val playerPermissionService: PlayerPermissionService,
                             private val playerLimitService: PlayerLimitService,
                             private val playerStateService: PlayerStateService,
                             private val claimBuilder: Claim.Builder) {

    fun openAllPlayersMenu(claim: Claim, page: Int = 0) {
        val trustedPlayers = playerPermissionService.getByClaim(claim)

        // Create trust menu
        val gui = ChestGui(6, "All Players")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls
        val controlsPane = addControlsSection(gui) { openClaimTrustMenu(claim, 0) }
        addPaginator(controlsPane, page, ceil(trustedPlayers.count() / 36.0).toInt())

        // Add player search item
        val playerSearchItem = ItemStack(Material.NAME_TAG)
            .name(getLangText("Search"))
            .lore(getLangText("FindPlayerByName"))
        val guiPlayerSearchItem = GuiItem(playerSearchItem) { openPlayerSearchMenu(claim, false) }
        controlsPane.addItem(guiPlayerSearchItem, 3, 0)

        // Add list of players
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (player in Bukkit.getOnlinePlayers()) {
            if (player == claimBuilder.player) {
                continue
            }

            val warpItem = createHead(Bukkit.getOfflinePlayer(player.uniqueId))
                .name("${Bukkit.getOfflinePlayer(player.uniqueId).name}")
            val guiWarpItem = GuiItem(warpItem) {
                openPlayerPermissionsMenu(claim, Bukkit.getOfflinePlayer(player.uniqueId))
            }
            warpsPane.addItem(guiWarpItem, xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 8) {
                xSlot = 0
                ySlot += 1
            }
        }

        gui.show(claimBuilder.player)
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