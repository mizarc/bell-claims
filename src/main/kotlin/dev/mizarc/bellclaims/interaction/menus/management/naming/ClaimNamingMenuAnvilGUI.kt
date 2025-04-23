package dev.mizarc.bellclaims.interaction.menus.management.naming

import dev.mizarc.bellclaims.application.actions.claim.CreateClaim
import dev.mizarc.bellclaims.application.results.claim.CreateClaimResult
import dev.mizarc.bellclaims.infrastructure.adapters.bukkit.toPosition3D
import dev.mizarc.bellclaims.interaction.menus.MenuNavigator
import dev.mizarc.bellclaims.interaction.menus.management.ClaimManagementMenu
import dev.mizarc.bellclaims.interaction.menus.management.ClaimNamingMenu
import dev.mizarc.bellclaims.utils.getLangText
import dev.mizarc.bellclaims.utils.lore
import dev.mizarc.bellclaims.utils.name
import net.wesjd.anvilgui.AnvilGUI
import net.wesjd.anvilgui.AnvilGUI.ResponseAction
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.function.BiFunction


class ClaimNamingMenuAnvilGUI(private val player: Player, private val menuNavigator: MenuNavigator,
                              private val location: Location): ClaimNamingMenu, KoinComponent {
    private val plugin: Plugin by inject()
    private val createClaim: CreateClaim by inject()

    private var name = ""
    private var messageItem: ItemStack? = null

    override fun open() {
        val bellItem = ItemStack(Material.BELL)
            .name("")
            .lore("${location.blockX}, ${location.blockY}, ${location.blockZ}")
        val confirmItem = ItemStack(Material.NETHER_STAR).name(getLangText("Confirm1"))

        val gui = AnvilGUI.Builder()
            .itemLeft(bellItem)
            .itemOutput(confirmItem)
            .onClick(BiFunction { slot: Int?, stateSnapshot: AnvilGUI.StateSnapshot? ->
                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return@BiFunction emptyList()
                }
                name = stateSnapshot?.text ?: bellItem.displayName().toString()
                val result = createClaim.execute(player.uniqueId, name, location.toPosition3D(), location.world.uid)
                when (result) {
                    is CreateClaimResult.Success -> {
                        location.world.playSound(
                            player.location,
                            Sound.BLOCK_VAULT_OPEN_SHUTTER,
                            SoundCategory.BLOCKS,
                            1.0f,
                            1.0f
                        )
                        menuNavigator.openMenu(ClaimManagementMenu(menuNavigator, player, result.claim))
                    }

                    is CreateClaimResult.LimitExceeded -> {
                        messageItem = ItemStack(Material.PAPER)
                            .name("You've already hit your maximum warp limit")
                        open()
                    }

                    is CreateClaimResult.NameAlreadyExists -> {
                        messageItem = ItemStack(Material.PAPER)
                            .name(getLangText("AlreadyHaveClaimWithName"))
                        open()
                    }

                    is CreateClaimResult.NameCannotBeBlank -> {
                        messageItem = ItemStack(Material.PAPER)
                            .name("Name cannot be blank")
                        open()
                    }
                }
                return@BiFunction emptyList()
            })
            .text("")
            .title("Naming Claim")
            .plugin(plugin)

        if (messageItem != null) {
            gui.itemRight(messageItem)
        }
        println("hi you got here")

        gui.open(player)
    }
}