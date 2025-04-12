package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent

class ClaimNamingMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                      private val location: Location): Menu, KoinComponent {

    override fun open() {
        // Create homes menu
        val gui = AnvilGui("Naming Claim")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add bell menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val bellItem = ItemStack(Material.BELL)
            .name(claimBuilder.name)
            .lore("${claimBuilder.location.blockX}, ${claimBuilder.location.blockY}, " +
                    "${claimBuilder.location.blockZ}")
        val guiItem = GuiItem(bellItem) { guiEvent -> guiEvent.isCancelled = true }
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
}