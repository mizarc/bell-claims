package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.transfer.AcceptTransferRequest
import dev.mizarc.bellclaims.application.actions.player.IsPlayerInClaimMenu
import dev.mizarc.bellclaims.application.results.claim.transfer.AcceptTransferRequestResult
import dev.mizarc.bellclaims.application.results.player.IsPlayerInClaimMenuResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimTransferNamingMenu(private val menuNavigator: MenuNavigator, private val claim: Claim,
                              private val player: Player): Menu, KoinComponent {
    private val acceptTransferRequest: AcceptTransferRequest by inject()
    private val isPlayerInClaimMenu: IsPlayerInClaimMenu by inject()

    var name = ""
    var previousResult: AcceptTransferRequestResult? = null

    override fun open() {
        // Create homes menu
        val gui = AnvilGui("Naming Claim")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(name)
            .lore("${claim.position.x}, ${claim.position.y}, ${claim.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        val secondPane = StaticPane(0, 0, 1, 1)
        gui.secondItemComponent.addPane(secondPane)
        when (previousResult) {
            AcceptTransferRequestResult.NoActiveTransferRequest -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name("There is no longer an active transfer request")
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.ClaimNotFound -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name("The claim you are trying to accept can no longer be found")
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.BlockLimitExceeded -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name(getLangText("YouHaveRunOutOfClaimBlocks"))
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.ClaimLimitExceeded -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name(getLangText("YouHaveRunOutOfClaims"))
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.NameAlreadyExists -> {
                val paperItem = ItemStack(Material.PAPER)
                    .name(getLangText("AlreadyHaveClaimWithName"))
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.PlayerOwnsClaim -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name("You already own this claim.")
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            AcceptTransferRequestResult.StorageError -> {
                val paperItem = ItemStack(Material.MAGMA_CREAM)
                    .name("An internal error has occurred, contact your local administrator for support.")
                val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
                secondPane.addItem(guiPaperItem, 0, 0)
            }
            else -> {}
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm1"))
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            val previousOwnerId = claim.playerId

            previousResult = acceptTransferRequest.execute(claim.id, player.uniqueId, gui.renameText)
            when (previousResult) {
                AcceptTransferRequestResult.Success -> {
                    // Close previous owner's inventory if they were in it
                    val claimMenuResult = isPlayerInClaimMenu.execute(player.uniqueId, claim.id)
                    when (claimMenuResult) {
                        is IsPlayerInClaimMenuResult.Success ->  {
                            if (claimMenuResult.isInClaimMenu) {
                                val previousOwner = Bukkit.getPlayer(previousOwnerId)
                                previousOwner?.closeInventory()
                                previousOwner?.sendActionBar(
                                    Component.text(getLangText("ClaimHasBeenTransferred"))
                                        .color(TextColor.color(255, 85, 85)))
                            }
                        }
                        is IsPlayerInClaimMenuResult.StorageError -> {}
                    }

                    // Navigate to next menu
                    ClaimManagementMenu(menuNavigator, player, claim).open()
                }
                else -> {
                    open()
                }
            }
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}