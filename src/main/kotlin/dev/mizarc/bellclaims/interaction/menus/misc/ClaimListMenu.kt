package dev.mizarc.bellclaims.interaction.menus.misc

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.ListPlayerClaims
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.ceil

class ClaimListMenu(private val menuNavigator: MenuNavigator, private val player: Player): Menu, KoinComponent {
    private val listPlayerClaims: ListPlayerClaims by inject()

    var page = 1

    override fun open() {
        val claims = listPlayerClaims.execute(player.uniqueId)
        val gui = ChestGui(6, "Claims")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back/exit item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name("Go Back")
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add prev item
        val prevItem = ItemStack(Material.ARROW)
            .name("Prev")
        val guiPrevItem = GuiItem(prevItem) { guiEvent -> guiEvent.isCancelled = true }
        controlsPane.addItem(guiPrevItem, 6, 0)

        // Add page item
        val pageItem = ItemStack(Material.PAPER)
            .name("Page $page of ${ceil(claims.count() / 36.0).toInt()}")
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

        // Add list of warps
        val warpsPane = StaticPane(0, 2, 9, 4)
        gui.addPane(warpsPane)
        var xSlot = 0
        var ySlot = 0
        for (claim in claims) {
            val claimItem = ItemStack(Material.valueOf(claim.icon))
                .name(claim.name)
                .lore("${claim.position.x}, ${claim.position.y}, ${claim.position.z}")
            val guiWarpItem = GuiItem(claimItem) { guiEvent -> guiEvent.isCancelled = true }
            warpsPane.addItem(guiWarpItem, xSlot, ySlot)

            // Increment slot
            xSlot += 1
            if (xSlot > 8) {
                xSlot = 0
                ySlot += 1
            }
        }

        gui.show(player)
    }
}