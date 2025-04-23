package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimName
import dev.mizarc.bellclaims.application.results.claim.metadata.UpdateClaimNameResult
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class ClaimRenamingMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                        private val claim: Claim): Menu, KoinComponent {
    private val updateClaimName: UpdateClaimName by inject()

    private var name = ""
    private var isConfirming = false

    override fun open() {
        // Create homes menu
        val gui = AnvilGui(getLangText("RenamingClaim"))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.setOnNameInputChanged { newName ->
            if (!isConfirming) {
                name = newName
            } else {
                isConfirming = false
            }
        }

        // Add bell menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claim.name)
            .lore("${claim.position.x}, ${claim.position.y}, ${claim.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        val secondPane = StaticPane(0, 0, 1, 1)
        gui.secondItemComponent.addPane(secondPane)

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (name == claim.name) {
                menuNavigator.goBack()
                return@GuiItem
            }

            // Attempt renaming
            val result = updateClaimName.execute(claim.id, name)
            when (result) {
                is UpdateClaimNameResult.Success -> menuNavigator.goBackWithData(result.claim)
                UpdateClaimNameResult.ClaimNotFound -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("The claim you are trying to rename does not exist anymore")
                    val guiPaperItem = GuiItem(paperItem)
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                UpdateClaimNameResult.NameAlreadyExists -> {
                    val paperItem = ItemStack(Material.PAPER)
                        .name("That name has already been taken")
                    val guiPaperItem = GuiItem(paperItem) {guiEvent ->
                        secondPane.removeItem(0, 0)
                        lodestoneItem.name(name)
                        isConfirming = true
                        gui.update()
                    }
                    secondPane.addItem(guiPaperItem, 0, 0)
                    lodestoneItem.name(name)
                    isConfirming = true
                    gui.update()
                }
                else -> menuNavigator.goBack()
            }
        }

        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}