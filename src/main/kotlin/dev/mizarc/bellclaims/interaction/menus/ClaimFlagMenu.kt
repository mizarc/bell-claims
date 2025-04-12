package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.GetClaimFlags
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.utils.getDescription
import dev.mizarc.bellclaims.utils.getDisplayName
import dev.mizarc.bellclaims.utils.getIcon
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimFlagMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                    private val claim: Claim): Menu, KoinComponent {
    private val getClaimFlags: GetClaimFlags by inject()
    private val enableClaimFlag: EnableClaimFlag by inject()
    private val disableClaimFlag: DisableClaimFlag by inject()
    private val disableAllClaimFlags: DisableAllClaimFlags by inject()
    private val enableAllClaimFlags: EnableAllClaimFlags by inject()


    override fun open() {
        // Create claim flags menu
        val gui = ChestGui(6, "Claim Flags")
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(getLangText("GoBack1"))
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name(getLangText("DeselectAll1"))
        val guiDeselectItem = GuiItem(deselectItem) {
            disableAllClaimFlags.execute(claim.id)
            open()
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name(getLangText("SelectAll1"))
        val guiSelectItem = GuiItem(selectItem) {
            enableAllClaimFlags.execute(claim.id)
            open()
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

        val enabledRules = getClaimFlags.execute(claim.id)
        val disabledRules = Flag.entries.toTypedArray().subtract(enabledRules)

        // Add list of disabled permissions
        val disabledPermissionsPane = StaticPane(0, 2, 4, 3)
        gui.addPane(disabledPermissionsPane)
        var xSlot = 0
        var ySlot = 0
        for (rule in disabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                enableClaimFlag.execute(rule, claim.id)
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

        val enabledPermissionsPane = StaticPane(5, 2, 4, 3)
        gui.addPane(enabledPermissionsPane)
        xSlot = 0
        ySlot = 0
        for (rule in enabledRules) {
            val permissionItem = rule.getIcon()
                .name(rule.getDisplayName())
                .lore(rule.getDescription())

            val guiPermissionItem = GuiItem(permissionItem) {
                disableClaimFlag.execute(rule, claim.id)
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
}