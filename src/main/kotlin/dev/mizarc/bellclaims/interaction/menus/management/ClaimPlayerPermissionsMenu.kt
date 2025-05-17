package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.permission.GetClaimPlayerPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantAllPlayerClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantPlayerClaimPermission
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokeAllPlayerClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokePlayerClaimPermission
import dev.mizarc.bellclaims.application.actions.claim.transfer.CanPlayerReceiveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.DoesPlayerHaveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.OfferPlayerTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.WithdrawPlayerTransferRequest
import dev.mizarc.bellclaims.application.results.claim.transfer.CanPlayerReceiveTransferRequestResult
import dev.mizarc.bellclaims.application.results.claim.transfer.DoesPlayerHaveTransferRequestResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.ClaimPermission
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu
import dev.mizarc.bellclaims.utils.createHead
import dev.mizarc.bellclaims.utils.getDescription
import dev.mizarc.bellclaims.utils.getDisplayName
import dev.mizarc.bellclaims.utils.getIcon
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimPlayerPermissionsMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                                 private val claim: Claim, private val targetPlayer: OfflinePlayer
): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getPlayerClaimPermissions: GetClaimPlayerPermissions by inject()
    private val grantAllPlayerClaimPermissions: GrantAllPlayerClaimPermissions by inject()
    private val grantPlayerClaimPermission: GrantPlayerClaimPermission by inject()
    private val revokePlayerClaimPermission: RevokePlayerClaimPermission by inject()
    private val revokeAllPlayerClaimPermissions: RevokeAllPlayerClaimPermissions by inject()
    private val canPlayerReceiveTransferRequest: CanPlayerReceiveTransferRequest by inject()
    private val doesPlayerHaveTransferRequest: DoesPlayerHaveTransferRequest by inject()
    private val offerPlayerTransferRequest: OfferPlayerTransferRequest by inject()
    private val withdrawPlayerTransferRequest: WithdrawPlayerTransferRequest by inject()

    override fun open() {
        // Create player permissions menu
        val gui = ChestGui(6, localizationProvider.get(LocalizationKeys.MENU_PLAYER_PERMISSIONS_TITLE,
            targetPlayer.name))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = addControlsSection(gui) { menuNavigator.goBack() }

        val deselectAction: () -> Unit = {
            revokeAllPlayerClaimPermissions.execute(claim.id, targetPlayer.uniqueId)
            open()
        }

        val selectAction: () -> Unit = {
            grantAllPlayerClaimPermissions.execute(claim.id, targetPlayer.uniqueId)
            open()
        }

        addSelector(controlsPane, createHead(targetPlayer).name(targetPlayer.name ?:
            localizationProvider.get(LocalizationKeys.GENERAL_NAME_ERROR)), deselectAction, selectAction)

        val transferRequestResult = doesPlayerHaveTransferRequest.execute(claim.id, targetPlayer.uniqueId)

        val guiTransferRequestItem: GuiItem
        when (transferRequestResult) {
            is DoesPlayerHaveTransferRequestResult.ClaimNotFound -> {
                val transferRequestItem = ItemStack(Material.MAGMA_CREAM)
                    .name(LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME)
                    .lore(LocalizationKeys.SEND_TRANSFER_CONDITION_EXIST)
                guiTransferRequestItem = GuiItem(transferRequestItem)
            }
            is DoesPlayerHaveTransferRequestResult.StorageError -> {
                val transferRequestItem = ItemStack(Material.MAGMA_CREAM)
                    .name(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_ERROR_NAME))
                    .lore(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_ERROR_LORE))
                guiTransferRequestItem = GuiItem(transferRequestItem)
            }
            is DoesPlayerHaveTransferRequestResult.Success -> {
                guiTransferRequestItem = createTransferButton(transferRequestResult.hasRequest)
            }
        }
        controlsPane.addItem(guiTransferRequestItem, 8, 0)

        // Add vertical divider
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        val verticalDividerPane = StaticPane(4, 2, 1, 6)
        gui.addPane(verticalDividerPane)
        for (slot in 0..3) {
            verticalDividerPane.addItem(guiDividerItem, 0, slot)
        }

        val enabledPermissions = getPlayerClaimPermissions.execute(claim.id, targetPlayer.uniqueId)
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
                grantPlayerClaimPermission.execute(claim.id, targetPlayer.uniqueId, permission)
                open()
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
                revokePlayerClaimPermission.execute(claim.id, targetPlayer.uniqueId, permission)
                open()
            }

            enabledPermissionsPane.addItem(guiPermissionItem , xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 3) {
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
            .name(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME))

        val guiExitItem = GuiItem(exitItem) { backButtonAction() }
        controlsPane.addItem(guiExitItem, 0, 0)
        return controlsPane
    }

    private fun addSelector(controlsPane: StaticPane, displayItem: ItemStack,
                            deselectAction: () -> Unit, selectAction: () -> Unit) {
        // Add display item
        val guiDisplayItem = GuiItem(displayItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiDisplayItem, 4, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_DESELECT_ALL_NAME))
        val guiDeselectItem = GuiItem(deselectItem) { deselectAction() }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_SELECT_ALL_NAME))
        val guiSelectItem = GuiItem(selectItem) { selectAction() }
        controlsPane.addItem(guiSelectItem, 6, 0)
    }

    private fun createTransferButton(hasRequest: Boolean): GuiItem {
        val guiTransferRequestItem: GuiItem
        if (hasRequest) {
            // Cancel the transfer request if it is pending
            val transferClaimItem = ItemStack(Material.BARRIER)
                .name(localizationProvider.get(LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANCEL_TRANSFER_NAME))
                .lore(localizationProvider.get(LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANCEL_TRANSFER_LORE))
            guiTransferRequestItem = GuiItem(transferClaimItem) {
                withdrawPlayerTransferRequest.execute(claim.id, targetPlayer.uniqueId)
                open()
            }
        } else {
            // Send the transfer request if there is none pending
            val transferClaimAction: () -> Unit = {
                val confirmAction: () -> Unit = {
                    offerPlayerTransferRequest.execute(claim.id, targetPlayer.uniqueId)
                    open()
                }

                menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player,
                    getLangText("TransferClaimQuestion"), confirmAction))
            }
            when (canPlayerReceiveTransferRequest.execute(claim.id, targetPlayer.uniqueId)) {
                CanPlayerReceiveTransferRequestResult.Success -> {
                    val transferClaimItem = ItemStack(Material.BELL)
                        .name(localizationProvider.get(LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_TRANSFER_NAME))
                        .lore(localizationProvider.get(LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_TRANSFER_LORE,
                            targetPlayer.name))
                    guiTransferRequestItem = GuiItem(transferClaimItem) { transferClaimAction() }
                }
                CanPlayerReceiveTransferRequestResult.ClaimLimitExceeded -> {
                    val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                        .name(localizationProvider.get(
                            LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME))
                        .lore(LocalizationKeys.SEND_TRANSFER_CONDITION_CLAIMS)
                    guiTransferRequestItem = GuiItem(transferClaimItem)
                }
                CanPlayerReceiveTransferRequestResult.BlockLimitExceeded -> {
                    val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                        .name(localizationProvider.get(
                            LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME))
                        .lore(LocalizationKeys.SEND_TRANSFER_CONDITION_BLOCKS)
                    guiTransferRequestItem = GuiItem(transferClaimItem)
                }
                CanPlayerReceiveTransferRequestResult.ClaimNotFound -> {
                    val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                        .name(localizationProvider.get(
                            LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME))
                        .lore(LocalizationKeys.SEND_TRANSFER_CONDITION_EXIST)
                    guiTransferRequestItem = GuiItem(transferClaimItem)
                }
                CanPlayerReceiveTransferRequestResult.PlayerOwnsClaim -> {
                    val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                        .name(localizationProvider.get(
                            LocalizationKeys.MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME))
                        .lore(LocalizationKeys.SEND_TRANSFER_CONDITION_OWNER)
                    guiTransferRequestItem = GuiItem(transferClaimItem)
                }
                CanPlayerReceiveTransferRequestResult.StorageError -> {
                    val transferClaimItem = ItemStack(Material.MAGMA_CREAM)
                        .name(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_ERROR_NAME))
                        .lore(localizationProvider.get(LocalizationKeys.MENU_COMMON_ITEM_ERROR_LORE))
                    guiTransferRequestItem = GuiItem(transferClaimItem)
                }
            }
        }
        return guiTransferRequestItem
    }
}