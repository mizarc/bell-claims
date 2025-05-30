package dev.mizarc.bellclaims.interaction.menus.management

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.IsNewClaimLocationValid
import dev.mizarc.bellclaims.application.actions.claim.ListPlayerClaims
import dev.mizarc.bellclaims.application.results.claim.IsNewClaimLocationValidResult
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition2D
import dev.mizarc.bellclaims.interaction.menus.Menu
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class ClaimCreationMenu(private val player: Player, private val menuNavigator: MenuNavigator,
                        private val location: Location): Menu, KoinComponent {
    private val playerMetadataService: PlayerMetadataService by inject()
    private val listPlayerClaims: ListPlayerClaims by inject()
    private val isNewClaimLocationValid: IsNewClaimLocationValid by inject()

    override fun open() {
        val gui = ChestGui(1, "Claim Creation")
        val pane = StaticPane(0, 0, 9, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.addPane(pane)

        // Notify if player doesn't have enough claims
        val playerClaimCount = listPlayerClaims.execute(player.uniqueId).count()
        if (playerClaimCount >=
                playerMetadataService.getPlayerClaimLimit(player.uniqueId)) {
            val iconEditorItem = ItemStack(Material.MAGMA_CREAM)
                .name(getLangText("CannotCreateClaim1"))
                .lore(getLangText("YouHaveRunOutOfClaims"))
                .lore(getLangText("DeleteExistingClaim"))
            val guiIconEditorItem = GuiItem(iconEditorItem) { guiEvent -> guiEvent.isCancelled = true }
            pane.addItem(guiIconEditorItem, 4, 0)
            gui.show(player)
            return
        }

        // Change the button depending on whether the player is able to create the claim or not
        when (isNewClaimLocationValid.execute(location.toPosition2D(), location.world.uid)) {
            IsNewClaimLocationValidResult.Valid -> {
                val iconEditorItem = ItemStack(Material.BELL)
                    .name(getLangText("CreateClaim"))
                    .lore(getLangText("ProtectedFromGriefing"))
                    .lore(getLangText("RemainingClaims1")
                            + "${playerMetadataService.getPlayerClaimLimit(player.uniqueId) - playerClaimCount}"
                            + getLangText("RemainingClaims2"))
                val guiIconEditorItem = GuiItem(iconEditorItem) {
                    menuNavigator.openMenu(ClaimNamingMenu(player, menuNavigator, location))
                }
                pane.addItem(guiIconEditorItem, 4, 0)
                gui.show(player)
            }
            IsNewClaimLocationValidResult.Overlap -> {
                val iconEditorItem = ItemStack(Material.MAGMA_CREAM)
                    .name(getLangText("CannotCreateClaim2"))
                    .lore(getLangText("OverlapAnotherClaim"))
                    .lore(getLangText("PlaceBellElsewhere"))
                val guiIconEditorItem = GuiItem(iconEditorItem) { guiEvent -> guiEvent.isCancelled = true }
                pane.addItem(guiIconEditorItem, 4, 0)
                gui.show(player)
                return
            }
            IsNewClaimLocationValidResult.StorageError ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")
            else ->
                player.sendMessage("An internal error has occurred, contact your administrator for support.")

        }
    }
}