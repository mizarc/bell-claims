package xyz.mizarc.solidclaims.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import xyz.mizarc.solidclaims.ClaimService
import xyz.mizarc.solidclaims.claims.*
import xyz.mizarc.solidclaims.getClaimTool
import xyz.mizarc.solidclaims.listeners.ClaimPermission
import xyz.mizarc.solidclaims.listeners.ClaimRule
import xyz.mizarc.solidclaims.partitions.Area
import xyz.mizarc.solidclaims.partitions.Partition
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.partitions.Position2D
import xyz.mizarc.solidclaims.utils.*
import kotlin.concurrent.thread
import kotlin.math.ceil

class ClaimManagementMenu(private val claimRepository: ClaimRepository,
                          private val partitionRepository: PartitionRepository,
                          private val claimPermissionRepository: ClaimPermissionRepository,
                          private val playerAccessRepository: PlayerAccessRepository,
                          private val claimRuleRepository: ClaimRuleRepository,
                          private val claimService: ClaimService,
                          private val claimBuilder: Claim.Builder) {
    fun openClaimManagementMenu() {
        val existingClaim = claimRepository.getByPosition(claimBuilder.position)
        Bukkit.getLogger().info("$existingClaim")
        if (existingClaim == null) {
            openClaimCreationMenu()
            return
        }

        openClaimEditMenu(existingClaim)
    }

    fun openClaimCreationMenu() {
        val gui = ChestGui(1, "Claim Creation")
        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add warp creation icon
        val remainingClaims = claimService.getRemainingClaimCount(claimBuilder.player) ?: return
        if (remainingClaims < 1) {
            val iconEditorItem = ItemStack(Material.MAGMA_CREAM)
                .name("Cannot Create Claim")
                .lore("You have run out of claims. ")
                .lore("If you want to create a new claim, delete an existing one first.")
            val guiIconEditorItem = GuiItem(iconEditorItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiIconEditorItem, 4, 0)
            gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
            return
        }

        val iconEditorItem = ItemStack(Material.BELL)
            .name("Create Claim")
            .lore("The area around this bell will be protected from griefing.")
            .lore("You have ${claimService.getRemainingClaimCount(claimBuilder.player)} Claims remaining.")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimNamingMenu() }
        pane.addItem(guiIconEditorItem, 4, 0)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimNamingMenu(existingName: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Naming Claim")

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claimBuilder.name)
            .lore("${claimBuilder.position.x}, ${claimBuilder.position.y}, ${claimBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name("You already have a claim with that name")
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            claimBuilder.name = gui.renameText
            if (claimRepository.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimNamingMenu(existingName = true)
                return@GuiItem
            }
            val claim = claimBuilder.build()
            Bukkit.getLogger().info("$claim")
            claimRepository.add(claim)
            val partition = Partition(claim.id, Area(
                Position2D(claim.position.x - 5, claim.position.z - 5),
                Position2D(claim.position.x + 5, claim.position.z + 5)))
            partitionRepository.add(partition)
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
        gui.addPane(pane)

        // Add claim tool button
        val claimToolItem = ItemStack(Material.STICK)
            .name("Claim Tool")
            .lore("Gives you a copy of the claim tool")
            .enchantment(Enchantment.LUCK)
            .flag(ItemFlag.HIDE_ENCHANTS)
        val guiClaimToolItem = GuiItem(claimToolItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerTool(claimBuilder.player)
        }
        pane.addItem(guiClaimToolItem, 0, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(claim.icon)
            .name("Edit Claim Icon")
            .lore("Changes the icon that shows up on the claim list")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimIconMenu(claim) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("Rename Claim")
            .lore("Renames this claim")
        val guiRenamingItem = GuiItem(renamingItem) { openClaimRenamingMenu(claim) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add player trusts
        val playerTrustItem = ItemStack(Material.PLAYER_HEAD)
            .name("Trusted Players:")
            .lore("${playerAccessRepository.getByClaim(claim).count()}")
        val guiPlayerTrustItem = GuiItem(playerTrustItem) { openClaimTrustMenu(claim, 0) }
        pane.addItem(guiPlayerTrustItem, 5, 0)

        // Add claim flags
        val claimFlagsItem = ItemStack(Material.ACACIA_HANGING_SIGN)
            .name("Claim Flags")
            .lore("${claimRuleRepository.getByClaim(claim).count()}")
        val guiClaimFlagsItem = GuiItem(claimFlagsItem) { openClaimFlagMenu(claim) }
        pane.addItem(guiClaimFlagsItem, 6, 0)

        // Add warp delete icon
        val deleteItem = ItemStack(Material.REDSTONE)
            .name("Delete Claim")
        val guiDeleteItem = GuiItem(deleteItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiDeleteItem, 8, 0)

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

    fun openClaimIconMenu(claim: Claim) {
        val gui = FurnaceGui("Set Warp Icon")
        val fuelPane = StaticPane(0, 0, 1, 1)

        // Add info paper menu item
        val paperItem = ItemStack(Material.PAPER)
            .name("Place an item in the top slot to set it as the icon")
            .lore("Don't worry, you'll keep it")
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
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)

            // Set icon if item in slot
            if (newIcon != null) {
                claim.icon = newIcon.type
                claimRepository.update(claim)
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
        val gui = AnvilGui("Renaming Claim")

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claim.name)
            .lore("${claimBuilder.position.x}, ${claimBuilder.position.y}, ${claimBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name("That name has already been taken")
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (gui.renameText == claim.name) {
                openClaimEditMenu(claim)
                return@GuiItem
            }

            // Stay on menu if the name is already taken
            if (claimRepository.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimRenamingMenu(claim, existingName = true)
                return@GuiItem
            }

            claim.name = gui.renameText
            claimRepository.update(claim)
            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimFlagMenu(claim: Claim) {
        // Create claim flags menu
        val gui = ChestGui(4, "Claim Flags")

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { openClaimEditMenu(claim) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name("Deselect All")
        val guiDeselectItem = GuiItem(deselectItem) {
            for (rule in ClaimRule.values()) {
                claimRuleRepository.remove(claim, rule)
            }
            openClaimFlagMenu(claim)
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name("Select All")
        val guiSelectItem = GuiItem(selectItem) {
            for (rule in ClaimRule.values()) {
                claimRuleRepository.add(claim, rule)
            }
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
        for (slot in 0..1) {
            verticalDividerPane.addItem(guiDividerItem, 0, slot)
        }

        val enabledRules = claimRuleRepository.getByClaim(claim)
        val disabledRules = ClaimRule.values().subtract(enabledRules)

        // Add list of disabled permissions
        val disabledPermissionsPane = StaticPane(0, 2, 4, 2)
        gui.addPane(disabledPermissionsPane)
        var xSlot = 0
        var ySlot = 0
        for (rule in disabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                claimRuleRepository.add(claim, rule)
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

        val enabledPermissionsPane = StaticPane(5, 2, 4, 2)
        gui.addPane(enabledPermissionsPane)
        xSlot = 0
        ySlot = 0
        for (rule in enabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                claimRuleRepository.remove(claim, rule)
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
        val trustedPlayers = playerAccessRepository.getByClaim(claim)

        // Create trust menu
        val gui = ChestGui(6, "Trusted Players")

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { openClaimEditMenu(claim) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add default permissions button
        val defaultPermsItem = ItemStack(Material.LECTERN)
            .name("Default Permissions")
            .lore("Configures the permissions that untrusted players will have")
        val guiDefaultPermsItem = GuiItem(defaultPermsItem) { openClaimPermisionsMenu(claim) }
        controlsPane.addItem(guiDefaultPermsItem, 2, 0)

        // Add all players menu
        val allPlayersItem = ItemStack(Material.PLAYER_HEAD)
            .name("All Players")
            .lore("Find a player from a list of all online players")
        val guiAllPlayersItem = GuiItem(allPlayersItem) { openAllPlayersMenu(claim, 0) }
        controlsPane.addItem(guiAllPlayersItem, 4, 0)

        // Add prev item
        val prevItem = ItemStack(Material.ARROW)
            .name("Prev")
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER)
            .name("Page $page of ${ceil(trustedPlayers.count() / 36.0).toInt()}")
        val guiPageItem = GuiItem(pageItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPageItem, 7, 0)

        // Add next item
        val nextItem = ItemStack(Material.ARROW)
            .name("Next")
        val guiNextItem = GuiItem(nextItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiNextItem, 8, 0)

        // Add divider
        val dividerPane = StaticPane(0, 1, 9, 1)
        gui.addPane(dividerPane)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        for (slot in 0..8) {
            val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
            dividerPane.addItem(guiDividerItem, slot, 0)
        }

        // Add list of players
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (trustedPlayer in trustedPlayers) {
            val warpItem = createHead(Bukkit.getOfflinePlayer(trustedPlayer.key))
                .name("${Bukkit.getOfflinePlayer(trustedPlayer.key).name}")
                .lore("Has ${trustedPlayer.value.count()} permissions")
            val guiWarpItem = GuiItem(warpItem) {
                openPlayerPermisionsMenu(claim, Bukkit.getOfflinePlayer(trustedPlayer.key))
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

    fun openPlayerPermisionsMenu(claim: Claim, player: OfflinePlayer) {
        // Create player permissions menu
        val gui = ChestGui(6, "${player.name}'s Permissions")

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { openClaimTrustMenu(claim, 0) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add player head
        val playerItem = createHead(player)
            .name("${player.name}")
        val guiPlayerItem = GuiItem(playerItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPlayerItem, 4, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name("Deselect All")
        val guiDeselectItem = GuiItem(deselectItem) {
            for (permission in ClaimPermission.values()) {
                playerAccessRepository.removePermission(claim, player, permission)
            }
            openPlayerPermisionsMenu(claim, player)
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name("Select All")
        val guiSelectItem = GuiItem(selectItem) {
            for (permission in ClaimPermission.values()) {
                playerAccessRepository.add(claim, player, permission)
            }
            openPlayerPermisionsMenu(claim, player)
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

        val enabledPermissions = playerAccessRepository.getByPlayerInClaim(claim, player)
        val disabledPermissions = ClaimPermission.values().subtract(enabledPermissions)

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
                playerAccessRepository.add(claim, player, permission)
                openPlayerPermisionsMenu(claim, player)
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
                playerAccessRepository.removePermission(claim, player, permission)
                openPlayerPermisionsMenu(claim, player)
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

    fun openClaimPermisionsMenu(claim: Claim) {
        // Create player permissions menu
        val gui = ChestGui(6, "Default Claim Permissions")

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { openClaimTrustMenu(claim, 0) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add bell icon
        val bellItem = ItemStack(Material.BELL)
            .name("Default")
        val guiBellItem = GuiItem(bellItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiBellItem, 4, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name("Deselect All")
        val guiDeselectItem = GuiItem(deselectItem) {
            for (permission in ClaimPermission.values()) {
                claimPermissionRepository.remove(claim, permission)
            }
            openClaimPermisionsMenu(claim)
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name("Select All")
        val guiSelectItem = GuiItem(selectItem) {
            for (permission in ClaimPermission.values()) {
                claimPermissionRepository.add(claim, permission)
            }
            openClaimPermisionsMenu(claim)
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

        val enabledPermissions = claimPermissionRepository.getByClaim(claim)
        val disabledPermissions = ClaimPermission.values().subtract(enabledPermissions)

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
                claimPermissionRepository.add(claim, permission)
                openClaimPermisionsMenu(claim)
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
                claimPermissionRepository.remove(claim, permission)
                openClaimPermisionsMenu(claim)
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
        val trustedPlayers = playerAccessRepository.getByClaim(claim)

        // Create trust menu
        val gui = ChestGui(6, "All Players")

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { openClaimTrustMenu(claim, 0) }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add prev item
        val prevItem = ItemStack(Material.ARROW)
            .name("Prev")
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER)
            .name("Page $page of ${ceil(trustedPlayers.count() / 36.0).toInt()}")
        val guiPageItem = GuiItem(pageItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPageItem, 7, 0)

        // Add next item
        val nextItem = ItemStack(Material.ARROW)
            .name("Next")
        val guiNextItem = GuiItem(nextItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiNextItem, 8, 0)

        // Add divider
        val dividerPane = StaticPane(0, 1, 9, 1)
        gui.addPane(dividerPane)
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        for (slot in 0..8) {
            val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
            dividerPane.addItem(guiDividerItem, slot, 0)
        }

        // Add list of players
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (player in Bukkit.getOnlinePlayers()) {
            val warpItem = createHead(Bukkit.getOfflinePlayer(player.uniqueId))
                .name("${Bukkit.getOfflinePlayer(player.uniqueId).name}")
            val guiWarpItem = GuiItem(warpItem) {
                openPlayerPermisionsMenu(claim, Bukkit.getOfflinePlayer(player.uniqueId))
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
}