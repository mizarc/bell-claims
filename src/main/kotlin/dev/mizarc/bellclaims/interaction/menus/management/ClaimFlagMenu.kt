package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.GetClaimFlags
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.domain.values.Flag
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.getIcon
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimFlagMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                    private val claim: Claim): Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getClaimFlags: GetClaimFlags by inject()
    private val enableClaimFlag: EnableClaimFlag by inject()
    private val disableClaimFlag: DisableClaimFlag by inject()
    private val disableAllClaimFlags: DisableAllClaimFlags by inject()
    private val enableAllClaimFlags: EnableAllClaimFlags by inject()


    override fun open() {
        // Create claim flags menu
        val playerId = player.uniqueId
        val gui = ChestGui(6, localizationProvider.get(playerId, LocalizationKeys.MENU_FLAGS_TITLE))
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }

        // Add controls pane
        val controlsPane = StaticPane(0, 0, 9, 1)
        gui.addPane(controlsPane)

        // Add go back item
        val exitItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME))
        val guiExitItem = GuiItem(exitItem) { menuNavigator.goBack() }
        controlsPane.addItem(guiExitItem, 0, 0)

        // Add deselect all button
        val deselectItem = ItemStack(Material.HONEY_BLOCK)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_DESELECT_ALL_NAME))
        val guiDeselectItem = GuiItem(deselectItem) {
            disableAllClaimFlags.execute(claim.id)
            open()
        }
        controlsPane.addItem(guiDeselectItem, 2, 0)

        // Add select all button
        val selectItem = ItemStack(Material.SLIME_BLOCK)
            .name(localizationProvider.get(playerId, LocalizationKeys.MENU_COMMON_ITEM_SELECT_ALL_NAME))
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

        val enabledFlags = getClaimFlags.execute(claim.id)
        val disabledFlags = Flag.entries.toTypedArray().subtract(enabledFlags)

        // Add list of disabled permissions
        val disabledPermissionsPane = StaticPane(0, 2, 4, 3)
        gui.addPane(disabledPermissionsPane)
        var xSlot = 0
        var ySlot = 0
        for (flag in disabledFlags) {
            val permissionItem = flag.getIcon(localizationProvider)

            val guiPermissionItem = GuiItem(permissionItem) {
                enableClaimFlag.execute(flag, claim.id)
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
        for (flag in enabledFlags) {
            val permissionItem = flag.getIcon(localizationProvider)

            val guiPermissionItem = GuiItem(permissionItem) {
                disableClaimFlag.execute(flag, claim.id)
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