package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.ClaimService
import dev.mizarc.bellclaims.infrastructure.PartitionService
import dev.mizarc.bellclaims.interaction.listeners.ClaimVisualiser
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.infrastructure.players.PlayerStateRepository
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name

class EditToolMenu(private val player: Player, private val claimService: ClaimService,
                   private val partitionService: PartitionService, private val playerStateRepo: PlayerStateRepository,
                   private val claimVisualiser: ClaimVisualiser, private val partition: Partition? = null) {
    fun openEditToolMenu() {
        val gui = ChestGui(1, "Claim Tool")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }

        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add mode switch icon
        val modeSwitchItem = ItemStack(Material.SPYGLASS)
            .name("Change Mode")

        val playerState = playerStateRepo.get(player) ?: return
        val guiModeSwitchItem: GuiItem
        if (playerState.claimToolMode == 0) {
            modeSwitchItem.lore("> View Mode")
            modeSwitchItem.lore("Edit Mode")
            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                playerState.claimToolMode = 1
                claimVisualiser.showVisualisation(player)
                openEditToolMenu()
            }
        }
        else {
            modeSwitchItem.lore("View Mode")
            modeSwitchItem.lore("> Edit Mode")
            guiModeSwitchItem = GuiItem(modeSwitchItem) {
                playerState.claimToolMode = 0
                claimVisualiser.showVisualisation(player)
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
                .name("No Claim Here")
                .lore("Select an area in a claim to see more options.")
            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        // Add message if player doesn't own claim
        val claim = claimService.getById(partition.claimId) ?: return
        if (claim.owner.uniqueId != player.uniqueId) {
            val messageItem = ItemStack(Material.COAL)
                .name("Not Your Claim")
                .lore("Select an area in your claim to see more options.")
            val guiMessageItem = GuiItem(messageItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiMessageItem, 5, 0)
            gui.show(player)
            return
        }

        val partitions = partitionService.getByClaim(claim)
        val claimItem = ItemStack(Material.BELL)
            .name("Claim")
            .lore("Name: ${claim.name}")
            .lore("Location: ${claim.position.x}, ${claim.position.y}, ${claim.position.z}")
            .lore("Partitions: ${partitions.count()}")
            .lore("Claim Blocks: ${claimService.getBlockCount(claim)}")
        val guiClaimItem = GuiItem(claimItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiClaimItem, 3, 0)

        val partitionItem = ItemStack(Material.PAPER)
            .name("Partition")
            .lore("Location: ${partition.area.lowerPosition2D.x}, ${partition.area.lowerPosition2D.z} / " +
                    "${partition.area.upperPosition2D.x}, ${partition.area.upperPosition2D.z}")
            .lore("Blocks: ${partition.area.getBlockCount()}")
        val guiPartitionItem = GuiItem(partitionItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPartitionItem, 5, 0)

        if (partitionService.isRemoveResultInAnyDisconnected(partition) ||
                partition.id == partitionService.getPrimaryPartition(claim).id) {
            val deleteItem = ItemStack(Material.GUNPOWDER)
                .name("Can't Delete Partition")
                .lore("Deleting this would result in your claim being fragmented.")
            val guiDeleteItem = GuiItem(deleteItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiDeleteItem, 7, 0)
        }
        else {
            val deleteItem = ItemStack(Material.REDSTONE)
                .name("Delete Partition")
            val guiDeleteItem = GuiItem(deleteItem) { openDeleteMenu(partition) }
            pane.addItem(guiDeleteItem, 7, 0)
        }

        gui.show(player)
    }

    fun openDeleteMenu(partition: Partition) {
        val gui = HopperGui("Delete Partition?")
        val pane = StaticPane(1, 0, 3, 1)
        gui.slotsComponent.addPane(pane)

        // Add no menu item
        val noItem = ItemStack(Material.RED_CONCRETE)
            .name("No")
            .lore("Take me back")
        val guiNoItem = GuiItem(noItem) { guiEvent ->
            guiEvent.isCancelled = true
            openEditToolMenu()
        }
        pane.addItem(guiNoItem, 0, 0)

        // Add yes menu item
        val yesItem = ItemStack(Material.GREEN_CONCRETE)
            .name("Yes")
            .lore("Warning, this is a permanent action")
        val guiYesItem = GuiItem(yesItem) { guiEvent ->
            guiEvent.isCancelled = true
            partitionService.removePartition(partition)
            val claim = claimService.getById(partition.claimId) ?: return@GuiItem
            claimVisualiser.registerClaimUpdate(claim)
            player.closeInventory()
        }
        pane.addItem(guiYesItem, 2, 0)

        gui.show(player)
    }
}