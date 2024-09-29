package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.api.*
import dev.mizarc.bellclaims.domain.claims.*
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.infrastructure.getClaimMoveTool
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import dev.mizarc.bellclaims.domain.flags.Flag
import dev.mizarc.bellclaims.interaction.menus.ConfirmationMenu.Companion.openConfirmationMenu
import dev.mizarc.bellclaims.utils.*
import org.bukkit.event.inventory.ClickType
import kotlin.concurrent.thread
import kotlin.math.ceil

import dev.mizarc.bellclaims.utils.getLangText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class ClaimManagementMenu(private val claimService: ClaimService,
                          private val claimWorldService: ClaimWorldService,
                          private val flagService: FlagService,
                          private val defaultPermissionService: DefaultPermissionService,
                          private val playerPermissionService: PlayerPermissionService,
                          private val playerLimitService: PlayerLimitService,
                          private val playerState: PlayerStateService,
                          private val claimBuilder: Claim.Builder) {
    fun openClaimManagementMenu() {
        val existingClaim = claimWorldService.getByLocation(claimBuilder.location)
        if (existingClaim == null) {
            openClaimCreationMenu()
            return
        }

        openClaimEditMenu(existingClaim)
    }

    fun openClaimCreationMenu() {
        val gui = ChestGui(1, "Claim Creation")
        val pane = StaticPane(0, 0, 9, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.addPane(pane)

        // Check if player doesn't have enough claims
        if (playerLimitService.getRemainingClaimCount(claimBuilder.player) < 1) {
            val iconEditorItem = ItemStack(Material.MAGMA_CREAM)
                .name(getLangText("CannotCreateClaim1"))
                .lore(getLangText("YouHaveRunOutOfClaims"))
                .lore(getLangText("DeleteExistingClaim"))
            val guiIconEditorItem = GuiItem(iconEditorItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiIconEditorItem, 4, 0)
            gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
            return
        }

        // Check if created claim area would overlap
        if (!claimWorldService.isNewLocationValid(claimBuilder.location)) {
            val iconEditorItem = ItemStack(Material.MAGMA_CREAM)
                .name(getLangText("CannotCreateClaim2"))
                .lore(getLangText("OverlapAnotherClaim"))
                .lore(getLangText("PlaceBellElsewhere"))
            val guiIconEditorItem = GuiItem(iconEditorItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiIconEditorItem, 4, 0)
            gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
            return
        }

        // Add warp creation icon
        val iconEditorItem = ItemStack(Material.BELL)
            .name(getLangText("CreateClaim"))
            .lore(getLangText("ProtectedFromGriefing"))
            .lore(getLangText("RemainingClaims1") + "${playerLimitService.getRemainingClaimCount(claimBuilder.player)}" + getLangText("RemainingClaims2"))
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimNamingMenu() }
        pane.addItem(guiIconEditorItem, 4, 0)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimNamingMenu(existingName: Boolean = false) {
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

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm1"))
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            claimBuilder.name = gui.renameText
            if (claimService.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimNamingMenu(existingName = true)
                return@GuiItem
            }
            claimWorldService.create(gui.renameText, claimBuilder.location, claimBuilder.player)
            val claim = claimWorldService.getByLocation(claimBuilder.location) ?: return@GuiItem
            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(claimBuilder.player)
    }

    fun openClaimEditMenu(claim: Claim) {
        val gui = ChestGui(1, "Claim '${claim.name}'")
        val pane = StaticPane(0, 0, 9, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.addPane(pane)

        // Add claim tool button
        val claimToolItem = ItemStack(Material.STICK)
            .name(getLangText("ClaimTool"))
            .lore(getLangText("GivesYouClaimTool"))
            .enchantment(Enchantment.LUCK_OF_THE_SEA)
            .flag(ItemFlag.HIDE_ENCHANTS)
        val guiClaimToolItem = GuiItem(claimToolItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerTool(claimBuilder.player)
        }
        pane.addItem(guiClaimToolItem, 0, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(claim.icon)
            .name(getLangText("EditClaimIcon"))
            .lore(getLangText("ChangesClaimIcon"))
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimIconMenu(claim) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name(getLangText("RenameClaim"))
            .lore(getLangText("RenamesThisClaim"))
        val guiRenamingItem = GuiItem(renamingItem) { openClaimRenamingMenu(claim) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add player trusts
        val playerTrustItem = ItemStack(Material.PLAYER_HEAD)
            .name(getLangText("TrustedPlayers"))
            .lore("${playerPermissionService.getByClaim(claim).count()}")
        val guiPlayerTrustItem = GuiItem(playerTrustItem) { openClaimTrustMenu(claim, 0) }
        pane.addItem(guiPlayerTrustItem, 5, 0)

        // Add claim flags
        val claimFlagsItem = ItemStack(Material.ACACIA_HANGING_SIGN)
            .name(getLangText("ClaimFlags"))
            .lore("${flagService.getByClaim(claim).count()}")
        val guiClaimFlagsItem = GuiItem(claimFlagsItem) { openClaimFlagMenu(claim) }
        pane.addItem(guiClaimFlagsItem, 6, 0)

        // Add warp delete icon
        val deleteItem = ItemStack(Material.PISTON)
            .name(getLangText("MoveClaim"))
            .lore(getLangText("PlaceItemToMoveClaim"))
        val guiDeleteItem = GuiItem(deleteItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerMoveTool(claimBuilder.player, claim)
        }
        pane.addItem(guiDeleteItem, 8, 0)

        // Set player state that user is in claim management menu
        val playerState = playerState.getByPlayer(claimBuilder.player)
        if (playerState != null) {
            playerState.isInClaimMenu = claim
        }

        gui.show(claimBuilder.player)
    }

    private fun givePlayerTool(player: Player) {
        for (item in player.inventory.contents!!) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return
            }
        }
        player.inventory.addItem(getClaimTool())
    }

    private fun givePlayerMoveTool(player: Player, claim: Claim) {
        for (item in player.inventory.contents!!) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimMoveTool(claim).itemMeta) {
                return
            }
        }
        player.inventory.addItem(getClaimMoveTool(claim))
    }

    fun openClaimIconMenu(claim: Claim) {
        val gui = FurnaceGui(getLangText("SetWarpIcon"))
        val fuelPane = StaticPane(0, 0, 1, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add info paper menu item
        val paperItem = ItemStack(Material.PAPER)
            .name(getLangText("PlaceItemTopSlot"))
            .lore(getLangText("KeepItemMessage"))
        val guiIconEditorItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
        fuelPane.addItem(guiIconEditorItem, 0, 0)
        gui.fuelComponent.addPane(fuelPane)

        // Allow item to be placed in slot
        val inputPane = StaticPane(0, 0, 1, 1)
        inputPane.setOnClick { guiEvent ->
            guiEvent.isCancelled = true
            val temp = guiEvent.cursor
            val cursor = guiEvent.cursor?.type ?: Material.AIR

            if (cursor == Material.AIR) {
                inputPane.removeItem(0, 0)
                gui.update()
                return@setOnClick
            }

            inputPane.addItem(GuiItem(ItemStack(cursor)), 0, 0)
            gui.update()
            thread(start = true) {
                Thread.sleep(1)
                claimBuilder.player.setItemOnCursor(temp)
            }
        }
        gui.ingredientComponent.addPane(inputPane)

        // Add confirm menu item
        val outputPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm2"))
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)

            // Set icon if item in slot
            if (newIcon != null) {
                claimService.changeIcon(claim, newIcon.type)
                openClaimEditMenu(claim)
            }

            // Go back to edit menu if no item in slot
            openClaimEditMenu(claim)
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimRenamingMenu(claim: Claim, existingName: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui(getLangText("RenamingClaim"))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claim.name)
            .lore("${claimBuilder.location.blockX}, ${claimBuilder.location.blockY}, " +
                    "${claimBuilder.location.blockZ}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name(getLangText("NameAlreadyTaken"))
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm3"))
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (gui.renameText == claim.name) {
                openClaimEditMenu(claim)
                return@GuiItem
            }

            // Stay on menu if the name is already taken
            if (claimService.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimRenamingMenu(claim, existingName = true)
                return@GuiItem
            }

            claimService.changeName(claim, gui.renameText)
            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimFlagMenu(claim: Claim) {
        // Create claim flags menu
        val gui = ChestGui(6, "Claim Flags")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(getLangText("GoBack1"))
        val guiExitItem = GuiItem(exitItem) { openClaimEditMenu(claim) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name(getLangText("DeselectAll1"))
        val guiDeselectItem = GuiItem(deselectItem) {
            flagService.removeAll(claim)
            openClaimFlagMenu(claim)
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name(getLangText("SelectAll1"))
        val guiSelectItem = GuiItem(selectItem) {
            flagService.addAll(claim)
            openClaimFlagMenu(claim)
        }
        controlsPane.addItem(guiSelectItem, 6, 0)

        // Add horizontal divider
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        val horizontalDividerPane = StaticPane(0, 1, 9, 1)
        gui.addPane(horizontalDividerPane)
        for (slot in 0..8) {
            horizontalDividerPane.addItem(guiDividerItem, slot, 0)
        }

        // Add vertical divider
        val verticalDividerPane = StaticPane(4, 2, 1, 6)
        gui.addPane(verticalDividerPane)
        for (slot in 0..3) {
            verticalDividerPane.addItem(guiDividerItem, 0, slot)
        }

        val enabledRules = flagService.getByClaim(claim)
        val disabledRules = Flag.entries.toTypedArray().subtract(enabledRules)

        // Add list of disabled permissions
        val disabledPermissionsPane = StaticPane(0, 2, 4, 3)
        gui.addPane(disabledPermissionsPane)
        var xSlot = 0
        var ySlot = 0
        for (rule in disabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                flagService.add(claim, rule)
                openClaimFlagMenu(claim)
            }

            disabledPermissionsPane.addItem(guiPermissionItem , xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 3) {
                xSlot = 0
                ySlot += 1
            }
        }

        val enabledPermissionsPane = StaticPane(5, 2, 4, 3)
        gui.addPane(enabledPermissionsPane)
        xSlot = 0
        ySlot = 0
        for (rule in enabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                flagService.remove(claim, rule)
                openClaimFlagMenu(claim)
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

    fun openClaimTrustMenu(claim: Claim, page: Int) {
        val trustedPlayers = playerPermissionService.getByClaim(claim)

        // Create trust menu
        val gui = ChestGui(6, "Trusted Players")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = addControlsSection(gui) { openClaimEditMenu(claim) }
        addPaginator(controlsPane, page, ceil(trustedPlayers.count() / 36.0).toInt())

        // Add default permissions button
        val defaultPermsItem = ItemStack(Material.LECTERN)
            .name(getLangText("DefaultPermissions"))
            .lore(getLangText("ConfiguresUntrustedPermissions"))
        val guiDefaultPermsItem = GuiItem(defaultPermsItem) { openClaimPermissionsMenu(claim) }
        controlsPane.addItem(guiDefaultPermsItem, 2, 0)

        // Add all players menu
        val allPlayersItem = ItemStack(Material.PLAYER_HEAD)
            .name("All Players")
            .lore("Find a player from a list of all online players")
        val guiAllPlayersItem = GuiItem(allPlayersItem) { openAllPlayersMenu(claim, 0) }
        controlsPane.addItem(guiAllPlayersItem, 4, 0)

        // Add list of players
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (trustedPlayer in trustedPlayers) {
            val warpItem = createHead(Bukkit.getOfflinePlayer(trustedPlayer.key.uniqueId))
                .name("${Bukkit.getOfflinePlayer(trustedPlayer.key.uniqueId).name}")
                .lore(getLangText("HasPermissions1") + "${trustedPlayer.value.count()}" + getLangText("HasPermissions2"))
            val guiWarpItem = GuiItem(warpItem) {
                openPlayerPermissionsMenu(claim, Bukkit.getOfflinePlayer(trustedPlayer.key.uniqueId))
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

    fun openPlayerPermissionsMenu(claim: Claim, player: OfflinePlayer) {
        // Create player permissions menu
        val gui = ChestGui(6, "${player.name}'s Permissions")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = addControlsSection(gui) { openClaimTrustMenu(claim, 0) }

        val deselectAction: () -> Unit = {
            playerPermissionService.removeAllForPlayer(claim, player)
            openPlayerPermissionsMenu(claim, player)
        }

        val selectAction: () -> Unit = {
            playerPermissionService.addAllForPlayer(claim, player)
            openPlayerPermissionsMenu(claim, player)
        }

        addSelector(controlsPane, createHead(player).name("${player.name}"), deselectAction, selectAction)

        if (claimService.playerHasTransferRequest(claim, player)) {
            // Transfer requests for player, show cancel button
            val cancelTransferClaimAction: () -> Unit = {
                claimService.deleteTransferRequest(claim, player)
                openPlayerPermissionsMenu(claim, player)
            }

            val transferClaimItem = ItemStack(Material.BARRIER)
                .name("§4${getLangText("CancelTransferClaim")}")
                .lore(getLangText("CancelTransferClaimDescription"))
            val guiSelectItem = GuiItem(transferClaimItem) { cancelTransferClaimAction() };

            controlsPane.addItem(guiSelectItem, 8, 0)
        } else {
            // No transfer request exists for player
            val transferClaimAction: () -> Unit = {
                val cancelAction: () -> Unit = {
                    openPlayerPermissionsMenu(claim, player)
                }

                val confirmAction: () -> Unit = {
                    claimService.addTransferRequest(claim, player)
                    openPlayerPermissionsMenu(claim, player)
                }

                val parameters = ConfirmationMenu.Companion.ConfirmationMenuParameters(
                    menuTitle = getLangText("TransferClaimQuestion"),
                    cancelAction = cancelAction,
                    confirmAction = confirmAction,
                    confirmActionDescription = getLangText("TransferClaimConfirmQuestionDescription")
                )

                openConfirmationMenu(claimBuilder.player, parameters)
            }

            val playerClaimLimitReached = playerLimitService.getRemainingClaimCount(player) < 1;

            val playerClaimMaxBlockCount = playerLimitService.getTotalClaimBlockCount(player)
            val playerClaimedBlocksCount = playerLimitService.getUsedClaimBlockCount(player)
            val claimBlockCount = claimService.getBlockCount(claim);

            val playerBlockLimitReached = (playerClaimMaxBlockCount - playerClaimedBlocksCount - claimBlockCount) < 0;

            val guiSelectItem: GuiItem
            if (playerClaimLimitReached) {
                // Player cannot receive transfer
                val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                    .name("§4${getLangText("CannotTransferClaim")}")
                    .lore(getLangText("PlayerHasRunOutOfClaims"))
                guiSelectItem = GuiItem(transferClaimItem) { };
            } else if (playerBlockLimitReached) {
                // Player cannot receive transfer
                val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                    .name("§4${getLangText("CannotTransferClaim")}")
                    .lore(getLangText("PlayerClaimBlockLimit"))
                guiSelectItem = GuiItem(transferClaimItem) { };
            } else {
                val transferClaimItem = ItemStack(Material.BELL)
                    .name("§4${getLangText("TransferClaim")}")
                    .lore("This will transfer the current claim to ${player.name}!")
                guiSelectItem = GuiItem(transferClaimItem) { transferClaimAction() };
            }

            controlsPane.addItem(guiSelectItem, 8, 0)
        }

        // Add vertical divider
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        val verticalDividerPane = StaticPane(4, 2, 1, 6)
        gui.addPane(verticalDividerPane)
        for (slot in 0..3) {
            verticalDividerPane.addItem(guiDividerItem, 0, slot)
        }

        val enabledPermissions = playerPermissionService.getByPlayer(claim, player)
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
                playerPermissionService.addForPlayer(claim, player, permission)
                openPlayerPermissionsMenu(claim, player)
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
                playerPermissionService.removeForPlayer(claim, player, permission)
                openPlayerPermissionsMenu(claim, player)
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

            // Close current owners inventory to kick him from menu if he was in it
            val playerState = playerState.getByPlayer(claim.owner)
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

    private fun addSelector(controlsPane: StaticPane, displayItem: ItemStack,
                            deselectAction: () -> Unit, selectAction: () -> Unit) {
        // Add display item
        val guiDisplayItem = GuiItem(displayItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiDisplayItem, 4, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK).name(getLangText("DeselectAll2"))
        val guiDeselectItem = GuiItem(deselectItem) { deselectAction() }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK).name(getLangText("SelectAll2"))
        val guiSelectItem = GuiItem(selectItem) { selectAction() }
        controlsPane.addItem(guiSelectItem, 6, 0)
    }
}