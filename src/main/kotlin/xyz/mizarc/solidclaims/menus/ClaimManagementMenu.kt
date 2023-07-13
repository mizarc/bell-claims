package xyz.mizarc.solidclaims.menus

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.AnvilGui
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.gui.type.FurnaceGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimRepository
import xyz.mizarc.solidclaims.claims.ClaimRuleRepository
import xyz.mizarc.solidclaims.claims.PlayerAccessRepository
import xyz.mizarc.solidclaims.getClaimTool
import xyz.mizarc.solidclaims.partitions.Area
import xyz.mizarc.solidclaims.partitions.Partition
import xyz.mizarc.solidclaims.partitions.PartitionRepository
import xyz.mizarc.solidclaims.partitions.Position
import xyz.mizarc.solidclaims.utils.enchantment
import xyz.mizarc.solidclaims.utils.flag
import xyz.mizarc.solidclaims.utils.lore
import xyz.mizarc.solidclaims.utils.name
import kotlin.concurrent.thread

class ClaimManagementMenu(private val claimRepository: ClaimRepository,
                          private val partitionRepository: PartitionRepository,
                          private val playerAccessRepository: PlayerAccessRepository,
                          private val claimRuleRepository: ClaimRuleRepository,
                          private val claimBuilder: Claim.Builder) {
    fun openClaimManagementMenu() {
        val existingClaim = claimRepository.getByPosition(claimBuilder.position)
        Bukkit.getLogger().info("$existingClaim")
        if (existingClaim == null) {
            openClaimCreationMenu()
            return
        }

        openClaimEditMenu(existingClaim)
    }

    fun openClaimCreationMenu() {
        val gui = ChestGui(1, "Claim Creation")
        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add warp creation icon
        val iconEditorItem = ItemStack(Material.BELL)
            .name("Create Claim")
            .lore("The area around this bell will be protected from griefing")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimNamingMenu() }
        pane.addItem(guiIconEditorItem, 4, 0)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimNamingMenu(existingName: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Naming Claim")

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claimBuilder.name)
            .lore("${claimBuilder.position.x}, ${claimBuilder.position.y}, ${claimBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name("You already have a claim with that name")
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            claimBuilder.name = gui.renameText
            if (claimRepository.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimNamingMenu(existingName = true)
                return@GuiItem
            }
            val claim = claimBuilder.build()
            Bukkit.getLogger().info("$claim")
            claimRepository.add(claim)
            val partition = Partition(claim.id, Area(
                Position(claim.position.x - 5, claim.position.z - 5),
                Position(claim.position.x + 5, claim.position.z + 5)))
            partitionRepository.add(partition)
            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(claimBuilder.player)
    }

    fun openClaimEditMenu(claim: Claim) {
        val gui = ChestGui(1, "Claim '${claim.name}'")
        val pane = StaticPane(0, 0, 9, 1)
        gui.addPane(pane)

        // Add claim tool button
        val claimToolItem = ItemStack(Material.STICK)
            .name("Claim Tool")
            .lore("Gives you a copy of the claim tool")
            .enchantment(Enchantment.LUCK)
            .flag(ItemFlag.HIDE_ENCHANTS)
        val guiClaimToolItem = GuiItem(claimToolItem) { guiEvent ->
            guiEvent.isCancelled = true
            givePlayerTool(claimBuilder.player)
        }
        pane.addItem(guiClaimToolItem, 0, 0)

        // Add icon editor button
        val iconEditorItem = ItemStack(claim.icon)
            .name("Edit Claim Icon")
            .lore("Changes the icon that shows up on the claim list")
        val guiIconEditorItem = GuiItem(iconEditorItem) { openClaimIconMenu(claim) }
        pane.addItem(guiIconEditorItem, 2, 0)

        // Add renaming icon
        val renamingItem = ItemStack(Material.NAME_TAG)
            .name("Rename Claim")
            .lore("Renames this claim")
        val guiRenamingItem = GuiItem(renamingItem) { openClaimRenamingMenu(claim) }
        pane.addItem(guiRenamingItem, 3, 0)

        // Add player trusts
        val playerTrustItem = ItemStack(Material.PLAYER_HEAD)
            .name("Trusted Players:")
            .lore("${playerAccessRepository.getByClaim(claim).count()}")
        val guiPlayerTrustItem = GuiItem(playerTrustItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiPlayerTrustItem, 5, 0)

        // Add claim flags
        val claimFlagsItem = ItemStack(Material.ACACIA_HANGING_SIGN)
            .name("Claim Flags")
            .lore("${claimRuleRepository.getByClaim(claim).count()}")
        val guiClaimFlagsItem = GuiItem(claimFlagsItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiClaimFlagsItem, 6, 0)

        // Add warp delete icon
        val deleteItem = ItemStack(Material.REDSTONE)
            .name("Delete Claim")
        val guiDeleteItem = GuiItem(deleteItem) { guiEvent -> guiEvent.isCancelled = true }
        pane.addItem(guiDeleteItem, 8, 0)

        gui.show(claimBuilder.player)
    }

    private fun givePlayerTool(player: Player) {
        for (item in player.inventory.contents!!) {
            if (item == null) continue
            if (item.itemMeta != null && item.itemMeta == getClaimTool().itemMeta) {
                return
            }
        }
        player.inventory.addItem(getClaimTool())
    }

    fun openClaimIconMenu(claim: Claim) {
        val gui = FurnaceGui("Set Warp Icon")
        val fuelPane = StaticPane(0, 0, 1, 1)

        // Add info paper menu item
        val paperItem = ItemStack(Material.PAPER)
            .name("Place an item in the top slot to set it as the icon")
            .lore("Don't worry, you'll keep it")
        val guiIconEditorItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
        fuelPane.addItem(guiIconEditorItem, 0, 0)
        gui.fuelComponent.addPane(fuelPane)

        // Allow item to be placed in slot
        val inputPane = StaticPane(0, 0, 1, 1)
        inputPane.setOnClick { guiEvent ->
            guiEvent.isCancelled = true
            val temp = guiEvent.cursor
            val cursor = guiEvent.cursor?.type ?: Material.AIR

            if (cursor == Material.AIR) {
                inputPane.removeItem(0, 0)
                gui.update()
                return@setOnClick
            }

            inputPane.addItem(GuiItem(ItemStack(cursor)), 0, 0)
            gui.update()
            thread(start = true) {
                Thread.sleep(1)
                claimBuilder.player.setItemOnCursor(temp)
            }
        }
        gui.ingredientComponent.addPane(inputPane)

        // Add confirm menu item
        val outputPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            guiEvent.isCancelled = true
            val newIcon = gui.ingredientComponent.getItem(0, 0)

            // Set icon if item in slot
            if (newIcon != null) {
                claim.icon = newIcon.type
                claimRepository.update(claim)
                openClaimEditMenu(claim)
            }

            // Go back to edit menu if no item in slot
            openClaimEditMenu(claim)
        }
        outputPane.addItem(confirmGuiItem, 0, 0)
        gui.outputComponent.addPane(outputPane)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }

    fun openClaimRenamingMenu(claim: Claim, existingName: Boolean = false) {
        // Create homes menu
        val gui = AnvilGui("Renaming Claim")

        // Add lodestone menu item
        val firstPane = StaticPane(0, 0, 1, 1)
        val lodestoneItem = ItemStack(Material.BELL)
            .name(claim.name)
            .lore("${claimBuilder.position.x}, ${claimBuilder.position.y}, ${claimBuilder.position.z}")
        val guiItem = GuiItem(lodestoneItem) { guiEvent -> guiEvent.isCancelled = true }
        firstPane.addItem(guiItem, 0, 0)
        gui.firstItemComponent.addPane(firstPane)

        // Add message menu item if name is already taken
        if (existingName) {
            val secondPane = StaticPane(0, 0, 1, 1)
            val paperItem = ItemStack(Material.PAPER)
                .name("That name has already been taken")
            val guiPaperItem = GuiItem(paperItem) { guiEvent -> guiEvent.isCancelled = true }
            secondPane.addItem(guiPaperItem, 0, 0)
            gui.secondItemComponent.addPane(secondPane)
        }

        // Add confirm menu item.
        val thirdPane = StaticPane(0, 0, 1, 1)
        val confirmItem = ItemStack(Material.NETHER_STAR).name("Confirm")
        val confirmGuiItem = GuiItem(confirmItem) { guiEvent ->
            // Go back to edit menu if the name hasn't changed
            if (gui.renameText == claim.name) {
                openClaimEditMenu(claim)
                return@GuiItem
            }

            // Stay on menu if the name is already taken
            if (claimRepository.getByPlayer(claimBuilder.player).any { it.name == gui.renameText }) {
                openClaimRenamingMenu(claim, existingName = true)
                return@GuiItem
            }

            claim.name = gui.renameText
            claimRepository.update(claim)
            openClaimEditMenu(claim)
            guiEvent.isCancelled = true
        }
        thirdPane.addItem(confirmGuiItem, 0, 0)
        gui.resultComponent.addPane(thirdPane)
        gui.show(Bukkit.getPlayer(claimBuilder.player.uniqueId)!!)
    }
}