package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.api.ClaimService
import dev.mizarc.bellclaims.api.PartitionService
import dev.mizarc.bellclaims.api.PlayerStateService
import dev.mizarc.bellclaims.api.events.PartitionModificationEvent
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.interaction.visualisation.Visualiser
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.event.inventory.ClickType

import dev.mizarc.bellclaims.utils.getLangText

class EditToolMenu(private val claimService: ClaimService, private val partitionService: PartitionService,
                   private val playerStateService: PlayerStateService, private val player: Player,
                   private val visualiser: Visualiser, private val partition: Partition? = null) {
    fun openEditToolMenu() {
        val gui = ChestGui(1, getLangText("ClaimTool2"))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add mode switch icon
        val modeSwitchItem = ItemStack(Material.SPYGLASS)
            .name(getLangText("ChangeMode"))

        val playerState = playerStateService.getByPlayer(player) ?: return
        val guiModeSwitchItem: GuiItem
        if (playerState.claimToolMode == 0) {
            modeSwitchItem.lore(getLangText("ViewMode1"))
            modeSwitchItem.lore(getLangText("EditMode"))

            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                playerState.claimToolMode = 1
                visualiser.refresh(player)
                openEditToolMenu()
            }
        }
        else {
            modeSwitchItem.lore(getLangText("ViewMode2"))
            modeSwitchItem.lore(getLangText("ActiveEditMode"))
            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                playerState.claimToolMode = 0
                visualiser.refresh(player)
                openEditToolMenu()
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
                .name(getLangText("NoClaimHere"))
                .lore(getLangText("SelectAreaForMoreOptions"))

            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        // Add message if player doesn't own claim
        val claim = claimService.getById(partition.claimId) ?: return
        if (claim.owner.uniqueId != player.uniqueId) {
            val messageItem = ItemStack(Material.COAL)
                .name(getLangText("NotYourClaim"))
                .lore(getLangText("SelectYourClaimForMoreOptions"))

            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        val partitions = partitionService.getByClaim(claim)
        val claimItem = ItemStack(Material.BELL)
            .name(getLangText("Claim"))
            .lore(getLangText("Name") + "${claim.name}")
            .lore(getLangText("Location") + "${claim.position.x}, ${claim.position.y}, ${claim.position.z}")
            .lore(getLangText("Partitions") + "${partitions.count()}")
            .lore(getLangText("ClaimBlocks") + "${claimService.getBlockCount(claim)}")
        val guiClaimItem = GuiItem(claimItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiClaimItem, 3, 0)

        val partitionItem = ItemStack(Material.PAPER)
            .name(getLangText("Partition"))
            .lore(getLangText("PartitionLocation") + "${partition.area.lowerPosition2D.x}, ${partition.area.lowerPosition2D.z} / " +
                    "${partition.area.upperPosition2D.x}, ${partition.area.upperPosition2D.z}")
            .lore(getLangText("PartitionBlocks") + "${partition.area.getBlockCount()}")
        val guiPartitionItem = GuiItem(partitionItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPartitionItem, 5, 0)

        val primaryPartition = partitionService.getPrimary(claim) ?: return
        if (partitionService.isRemoveAllowed(partition) ||
                partition.id == primaryPartition.id) {
            val deleteItem = ItemStack(Material.GUNPOWDER)
                .name(getLangText("CantDeletePartition"))
                .lore(getLangText("FragmentedClaimWarning"))

            val guiDeleteItem = GuiItem(deleteItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiDeleteItem, 7, 0)
        }
        else {
            val deleteItem = ItemStack(Material.REDSTONE)
                .name(getLangText("DeletePartition"))
            val guiDeleteItem = GuiItem(deleteItem) { openDeleteMenu(partition) }
            pane.addItem(guiDeleteItem, 7, 0)
        }

        playerState.isInClaimMenu = claim
        gui.show(player)
    }

    fun openDeleteMenu(partition: Partition) {
        val gui = HopperGui(getLangText("DeletePartitionQuestion"))
        val pane = StaticPane(1, 0, 3, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.slotsComponent.addPane(pane)

        // Add no menu item
        val noItem = ItemStack(Material.RED_CONCRETE)
            .name(getLangText("QuestionNo"))
            .lore(getLangText("TakeMeBack"))
        val guiNoItem = GuiItem(noItem) { guiEvent ->
            guiEvent.isCancelled = true
            openEditToolMenu()
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name(getLangText("QuestionYes"))
            .lore(getLangText("PermanentActionWarning"))
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            guiEvent.isCancelled = true
            partitionService.delete(partition)
            val event = PartitionModificationEvent(partition)
            event.callEvent()
            player.closeInventory()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(player)
    }
}