package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.HopperGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.event.inventory.ClickType

import dev.mizarc.bellclaims.utils.getLangText
import org.bukkit.entity.Player
import org.hamcrest.Description

class ConfirmationMenu {
    companion object {
        data class ConfirmationMenuParameters(val menuTitle: String,
                                              val cancelAction: () -> Unit,
                                              val cancelActionDescription: String = getLangText("TakeMeBack"),
                                              val confirmAction: () -> Unit,
                                              val confirmActionDescription: String = getLangText("PermanentActionWarning"))


        // Generic confirmation menu which takes cancel and confirm functions as argument
        fun openConfirmationMenu(player: Player, parameters: ConfirmationMenuParameters) {
            // GetLangText in function which is calling confirmationMenu to make addition
            val gui = HopperGui(parameters.menuTitle)
            val pane = StaticPane(1, 0, 3, 1)
            gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
            gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
                guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
            gui.slotsComponent.addPane(pane)

            // Add no menu item
            val noItem = ItemStack(Material.RED_CONCRETE)
                .name(getLangText("QuestionNo"))
                .lore(parameters.cancelActionDescription)

            val guiNoItem = GuiItem(noItem) { guiEvent ->
                guiEvent.isCancelled = true
                parameters.cancelAction.invoke()
            }
            pane.addItem(guiNoItem, 0, 0)

            // Add yes menu item
            val yesItem = ItemStack(Material.GREEN_CONCRETE)
                .name(getLangText("QuestionYes"))
                .lore(parameters.confirmActionDescription)
            val guiYesItem = GuiItem(yesItem) { guiEvent ->
                guiEvent.isCancelled = true
                parameters.confirmAction.invoke()
            }
            pane.addItem(guiYesItem, 2, 0)

            gui.show(player)
        }
    }
}