package dev.mizarc.bellclaims.interaction.menus.misc

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimBlockCount
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.partition.CanRemovePartition
import dev.mizarc.bellclaims.application.actions.claim.partition.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.claim.partition.RemovePartition
import dev.mizarc.bellclaims.application.actions.player.RegisterClaimMenuOpening
import dev.mizarc.bellclaims.application.actions.player.visualisation.ClearVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.DisplayVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.GetVisualiserMode
import dev.mizarc.bellclaims.application.actions.player.visualisation.ToggleVisualiserMode
import dev.mizarc.bellclaims.application.events.PartitionModificationEvent
import dev.mizarc.bellclaims.application.results.claim.partition.CanRemovePartitionResult
import dev.mizarc.bellclaims.application.results.player.visualisation.GetVisualiserModeResult
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Partition
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.common.ConfirmationMenu
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditToolMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                   private val partition: Partition? = null): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getVisualiserMode: GetVisualiserMode by inject()
    private val toggleVisualiserMode: ToggleVisualiserMode by inject()
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimBlockCount: GetClaimBlockCount by inject()
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val displayVisualisation: DisplayVisualisation by inject()
    private val clearVisualisation: ClearVisualisation by inject()
    private val removePartition: RemovePartition by inject()
    private val registerClaimMenuOpening: RegisterClaimMenuOpening by inject()
    private val canRemovePartition: CanRemovePartition by inject()

    override fun open() {
        val title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_TITLE)
        val gui = ChestGui(1, title)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Get visualiser mode
        val visualiserMode = when (val result = getVisualiserMode.execute(player.uniqueId)) {
            GetVisualiserModeResult.StorageError -> 0
            is GetVisualiserModeResult.Success -> result.visualiserMode
        }

        // Add mode switch icon
        val modeSwitchItem = ItemStack(Material.SPYGLASS)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CHANGE_MODE_NAME))
        val guiModeSwitchItem: GuiItem
        if (visualiserMode == 0) {
            modeSwitchItem.lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CHANGE_MODE_LORE_VIEW_ACTIVE))
            modeSwitchItem.lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CHANGE_MODE_LORE_EDIT))

            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                toggleVisualiserMode.execute(player.uniqueId)
                clearVisualisation.execute(player.uniqueId)
                displayVisualisation.execute(player.uniqueId, player.location.toPosition3D())
                open()
            }
        }
        else {
            modeSwitchItem.lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CHANGE_MODE_LORE_VIEW))
            modeSwitchItem.lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CHANGE_MODE_LORE_EDIT_ACTIVE))
            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                toggleVisualiserMode.execute(player.uniqueId)
                clearVisualisation.execute(player.uniqueId)
                displayVisualisation.execute(player.uniqueId, player.location.toPosition3D())
                open()
            }
        }

        pane.addItem(guiModeSwitchItem, 0, 0)

        // Add divider
        val dividerItem = ItemStack(Material.BLACK_STAINED_GLASS_PANE).name(" ")
        val guiDividerItem = GuiItem(dividerItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiDividerItem, 1, 0)

        // Add message item if selection is out of any claim
        if (partition == null) {
            val messageItem = ItemStack(Material.COAL)
                .name(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_NO_CLAIM_NAME))
                .lore(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_NO_CLAIM_LORE))

            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        // Add message if player doesn't own claim
        val claim = getClaimDetails.execute(partition.claimId) ?: return
        if (claim.playerId != player.uniqueId) {
            val messageItem = ItemStack(Material.COAL)
                .name(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_NO_PERMISSION_NAME))
                .lore(localizationProvider.get(
                    player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_NO_PERMISSION_LORE))

            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        // Add claim information item
        val partitions = getClaimPartitions.execute(claim.id)
        val blockCount = getClaimBlockCount.execute(claim.id)
        val claimItem = ItemStack(Material.BELL)
            .name(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CLAIM_NAME))
            .lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CLAIM_LORE_CLAIM_NAME, claim.name))
            .lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CLAIM_LORE_LOCATION,
                claim.position.x, claim.position.y, claim.position.z))
            .lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CLAIM_LORE_PARTITIONS, partitions.count()))
            .lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CLAIM_LORE_BLOCKS, blockCount))
        val guiClaimItem = GuiItem(claimItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiClaimItem, 3, 0)

        // Add partition information item
        val partitionItem = ItemStack(Material.PAPER)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_PARTITION_NAME))
            .lore(localizationProvider.get(
                player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_PARTITION_LORE_LOCATION,
                partition.area.lowerPosition2D.x, partition.area.lowerPosition2D.z,
                partition.area.upperPosition2D.x, partition.area.upperPosition2D.z))
            .lore(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_PARTITION_LORE_BLOCKS,
                partition.area.getBlockCount()))
        val guiPartitionItem = GuiItem(partitionItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPartitionItem, 5, 0)

        // Change button depending on if partition can be removed
        when (canRemovePartition.execute(partition.id)) {
            CanRemovePartitionResult.Success -> {
                val deleteItem = ItemStack(Material.REDSTONE)
                    .name(localizationProvider.get( player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_DELETE_NAME))
                val deleteTitle = localizationProvider.get(
                    player.uniqueId, LocalizationKeys.MENU_CONFIRM_PARTITION_DELETE_TITLE)
                val confirmAction: () -> Unit = {
                    removePartition.execute(partition.id)
                    val event = PartitionModificationEvent(partition)
                    event.callEvent()
                    player.closeInventory()
                }
                val guiDeleteItem = GuiItem(deleteItem) {
                    menuNavigator.openMenu(ConfirmationMenu(menuNavigator, player, deleteTitle, confirmAction)) }
                pane.addItem(guiDeleteItem, 7, 0)
            }
            else -> {
                val deleteItem = ItemStack(Material.GUNPOWDER)
                    .name(localizationProvider.get(
                        player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CANNOT_DELETE_NAME))
                    .lore(localizationProvider.get(
                        player.uniqueId, LocalizationKeys.MENU_EDIT_TOOL_ITEM_CANNOT_DELETE_LORE))
                val guiDeleteItem = GuiItem(deleteItem) { guiEvent -> guiEvent.isCancelled = true }
                pane.addItem(guiDeleteItem, 7, 0)
            }

        }

        registerClaimMenuOpening.execute(player.uniqueId, claim.id)
        gui.show(player)
    }
}