package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ClaimPlayerSearchMenu(private val menuNavigator: MenuNavigator, private val claim: Claim,
                            private val player: Player): Menu {
    var playerDoesNotExist: Boolean = false

    override fun open() {
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
                playerDoesNotExist = true
                open()
                return@GuiItem
            }
            menuNavigator.goBackWithData(gui.renameText)
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(player)
    }
}