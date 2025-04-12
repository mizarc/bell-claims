package dev.mizarc.bellclaims.interaction.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.bellclaims.application.actions.claim.flags.GetClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.permissions.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.domain.entities.Claim
import dev.mizarc.bellclaims.infrastructure.getClaimMoveTool
import dev.mizarc.bellclaims.infrastructure.getClaimTool
import dev.mizarc.bellclaims.utils.enchantment
import dev.mizarc.bellclaims.utils.flag
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClaimManagementMenu(private val menuNavigator: MenuNavigator, private val player: Player,
                          private val claim: Claim): Menu, KoinComponent {
    private val getClaimFlags: GetClaimFlags by inject()
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()

    override fun open() {
        val gui = ChestGui(1, "Claim '${claim.name}'")
        val pane = StaticPane(0, 0, 9, 1)
        gui.setOnTopClick { guiEvent -> guiEvent.isCancelled = true }
        gui.setOnBottomClick { guiEvent -> if (guiEvent.click == ClickType.SHIFT_LEFT ||
            guiEvent.click == ClickType.SHIFT_RIGHT) guiEvent.isCancelled = true }
        gui.addPane(pane)

        // Add give claim tool button
        val claimToolItem = ItemStack(Material.STICK)
            .name(getLangText("ClaimTool"))
            .lore(getLangText("GivesYouClaimTool"))
            .enchantment(Enchantment.LUCK_OF_THE_SEA)
            .flag(ItemFlag.HIDE_ENCHANTS)
        val guiClaimToolItem = GuiItem(claimToolItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerTool(player)
        }
        pane.addItem(guiClaimToolItem, 0, 0)

        // Add update icon menu button
        val iconEditorItem = ItemStack(Material.valueOf(claim.icon))
            .name(getLangText("EditClaimIcon"))
            .lore(getLangText("ChangesClaimIcon"))
        val guiIconEditorItem = GuiItem(iconEditorItem) {
            menuNavigator.openMenu(ClaimIconMenu(player, menuNavigator, claim)) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add claim renaming button
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name(getLangText("RenameClaim"))
            .lore(getLangText("RenamesThisClaim"))
        val guiRenamingItem = GuiItem(renamingItem) { menuNavigator.openMenu() }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add player trusts button
        val playerTrustItem = ItemStack(Material.PLAYER_HEAD)
            .name(getLangText("TrustedPlayers"))
            .lore("${getPlayersWithPermissionInClaim.execute(claim.id).count()}")
        val guiPlayerTrustItem = GuiItem(playerTrustItem) { menuNavigator.openMenu() }
        pane.addItem(guiPlayerTrustItem, 5, 0)

        // Add claim flags button
        val claimFlagsItem = ItemStack(Material.ACACIA_HANGING_SIGN)
            .name(getLangText("ClaimFlags"))
            .lore("${getClaimFlags.execute(claim.id).count()}")
        val guiClaimFlagsItem = GuiItem(claimFlagsItem) { menuNavigator.openMenu() }
        pane.addItem(guiClaimFlagsItem, 6, 0)

        // Add claim move button
        val deleteItem = ItemStack(Material.PISTON)
            .name(getLangText("MoveClaim"))
            .lore(getLangText("PlaceItemToMoveClaim"))
        val guiDeleteItem = GuiItem(deleteItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerMoveTool(player, claim)
        }
        pane.addItem(guiDeleteItem, 8, 0)

        // Set player state that user is in claim management menu
        val playerState = playerStateService.getByPlayer(player)
        if (playerState != null) {
            playerState.isInClaimMenu = claim
        }

        gui.show(player)
    }

    private fun givePlayerTool(player: Player) {
        for (item in player.inventory.contents) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return
            }
        }
        player.inventory.addItem(getClaimTool())
    }

    private fun givePlayerMoveTool(player: Player, claim: Claim) {
        for (item in player.inventory.contents) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimMoveTool(claim).itemMeta) {
                return
            }
        }
        player.inventory.addItem(getClaimMoveTool(claim))
    }
}