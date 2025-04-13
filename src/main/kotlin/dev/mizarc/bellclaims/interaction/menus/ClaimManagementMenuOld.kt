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


    fun openTransferOfferMenu(claim: Claim, player: Player) {
        val cancelAction: () -> Unit = {
            player.closeInventory()
        }

        val confirmAction: () -> Unit = {
            openTransferNamingMenu(claim)
        }

        val parameters = ConfirmationMenu.Companion.ConfirmationMenuParameters(
            menuTitle = getLangText("AcceptTransferClaim"),
            cancelAction = cancelAction,
            confirmAction = confirmAction,
            confirmActionDescription = getLangText("AcceptTransferClaimConfirmDescription")
        )

        openConfirmationMenu(claimBuilder.player, parameters)
    }

    fun openTransferNamingMenu(claim: Claim, existingName: Boolean = false, claimLimitReached: Boolean = false, blockLimitReached: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Naming Claim")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claimBuilder.name)
            .lore("${claimBuilder.location.blockX}, ${claimBuilder.location.blockY}, " +
                    "${claimBuilder.location.blockZ}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name(getLangText("AlreadyHaveClaimWithName"))
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add message menu item if claim limit was reached
        if (claimLimitReached) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.MAGMA_CREAM)
                .name(getLangText("YouHaveRunOutOfClaims"))
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add message menu item if claim block limit was reached
        if (blockLimitReached) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.MAGMA_CREAM)
                .name(getLangText("YouHaveRunOutOfClaimBlocks"))
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm1"))
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            claimBuilder.name = gui.renameText
            if (claimService.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openTransferNamingMenu(claim, existingName = true)
                return@GuiItem
            }

            // Do last claim limit check
            if (playerLimitService.getRemainingClaimCount(claimBuilder.player) < 1) {
                openTransferNamingMenu(claim, claimLimitReached = true)
                return@GuiItem
            }

            // Do last claim block size check
            val playerClaimMaxBlockCount = playerLimitService.getTotalClaimBlockCount(claimBuilder.player)
            val playerClaimedBlocksCount = playerLimitService.getUsedClaimBlockCount(claimBuilder.player)
            val claimBlockCount = claimService.getBlockCount(claim);

            val playerBlockLimitReached = (playerClaimMaxBlockCount - playerClaimedBlocksCount - claimBlockCount) < 0;

            if (playerBlockLimitReached) {
                openTransferNamingMenu(claim, blockLimitReached = true)
                return@GuiItem
            }

            // Do final check for claim transfer request
            if (!claimService.playerHasTransferRequest(claim, claimBuilder.player)) {
                claimBuilder.player.closeInventory()
                claimBuilder.player.sendActionBar(
                    Component.text("The claim transfer request was cancelled")
                    .color(TextColor.color(255, 85, 85)))

                return@GuiItem
            }

            // Close current owners inventory to kick them from menu if they were in it
            val playerState = playerStateService.getByPlayer(claim.owner)
            if (playerState != null && playerState.isInClaimMenu == claim) {
                playerState.isInClaimMenu = null

                claim.owner.player?.closeInventory();

                claim.owner.player?.sendActionBar(
                    Component.text(getLangText("ClaimHasBeenTransferred"))
                        .color(TextColor.color(255, 85, 85)))
            }

            // Rename claim name
            claim.name = claimBuilder.name

            claimService.transferClaim(claim, claimBuilder.player)

            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(claimBuilder.player)
    }

    fun openClaimPermissionsMenu(claim: Claim) {
        // Create player permissions menu
        val gui = ChestGui(6, "Default Claim Permissions")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls
        val controlsPane = addControlsSection(gui) { openClaimTrustMenu(claim, 0) }

        val deselectAction: () -> Unit = {
            defaultPermissionService.removeAll(claim)
            openClaimPermissionsMenu(claim)
        }

        val selectAction: () -> Unit = {
            defaultPermissionService.addAll(claim)
            openClaimPermissionsMenu(claim)
        }

        addSelector(controlsPane, ItemStack(Material.BELL).name(getLangText("Default"))
, deselectAction, selectAction)

        // Add horizontal divider
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }

        // Add vertical divider
        val verticalDividerPane = StaticPane(4, 2, 1, 6)
        gui.addPane(verticalDividerPane)
        for (slot in 0..3) {
            verticalDividerPane.addItem(guiDividerItem, 0, slot)
        }

        val enabledPermissions = defaultPermissionService.getByClaim(claim)
        val disabledPermissions = ClaimPermission.entries.toTypedArray().subtract(enabledPermissions)

        // Add list of disabled permissions
        val disabledPermissionsPane = StaticPane(0, 2, 4, 4)
        gui.addPane(disabledPermissionsPane)
        var xSlot = 0
        var ySlot = 0
        for (permission in disabledPermissions) {
            val permissionItem = permission.getIcon()
                .name(permission.getDisplayName())
                .lore(permission.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                defaultPermissionService.add(claim, permission)
                openClaimPermissionsMenu(claim)
            }

            disabledPermissionsPane.addItem(guiPermissionItem , xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 3) {
                xSlot = 0
                ySlot += 1
            }
        }

        val enabledPermissionsPane = StaticPane(5, 2, 4, 4)
        gui.addPane(enabledPermissionsPane)
        xSlot = 0
        ySlot = 0
        for (permission in enabledPermissions) {
            val permissionItem = permission.getIcon()
                .name(permission.getDisplayName())
                .lore(permission.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                defaultPermissionService.remove(claim, permission)
                openClaimPermissionsMenu(claim)
            }

            enabledPermissionsPane.addItem(guiPermissionItem , xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 3) {
                xSlot = 0
                ySlot += 1
            }
        }

        gui.show(claimBuilder.player)
    }

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

    fun openPlayerSearchMenu(claim: Claim, playerDoesNotExist: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui(getLangText("SearchForPlayer"))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val headItem = ItemStack(Material.PLAYER_HEAD)
            .name(getLangText("Player"))
        val guiHeadItem = GuiItem(headItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiHeadItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (playerDoesNotExist) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name(getLangText("PlayerDoesNotExist"))
                .lore(getLangText("PlayerMustHaveLoggedIn"))

            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm4"))
        val confirmGuiItem = GuiItem(confirmItem) { _ ->
            val player = Bukkit.getOfflinePlayer(gui.renameText)
            if (!player.hasPlayedBefore()) {
                openPlayerSearchMenu(claim, true)
                return@GuiItem
            }
            openPlayerPermissionsMenu(claim, player)
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
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