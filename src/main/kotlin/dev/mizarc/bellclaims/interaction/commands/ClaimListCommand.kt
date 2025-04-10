package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimBlockCount
import dev.mizarc.bellclaims.application.actions.claim.ListPlayerClaims
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue
import kotlin.math.ceil

@CommandAlias("claimlist")
class ClaimListCommand : BaseCommand(), KoinComponent {
    private val listPlayerClaims: ListPlayerClaims by inject()
    private val getClaimBlockCount: GetClaimBlockCount by inject()

    @Default
    @CommandPermission("bellclaims.command.claimlist")
    @CommandCompletion("@nothing @players")
    @Syntax("[count] [player]")
    fun onClaimList(player: Player, @Default("1") page: Int) {
        // Retrieve the list of claims associated with the player
        val playerClaims = listPlayerClaims.execute(player.uniqueId)

        // Notify if player doesn't have any claims
        if (playerClaims.isEmpty()) {
            player.sendMessage("§cYou have no claims. Interact with a bell to get started.")
            return
        }

        // Notify if player specifies an invalid page
        if (page * 10 - 9 > playerClaims.count() || page < 1) {
            player.sendMessage("§cInvalid page specified.")
            return
        }

        // Create page listing claims and their block counts
        val chatInfo = ChatInfoBuilder("Claims")
        val totalClaims = playerClaims.size
        val startIndex = page * 10
        val endIndex = minOf(startIndex + 10, totalClaims)
        playerClaims.subList(startIndex, endIndex).forEach { claim ->
            val name = claim.name.ifEmpty { claim.id.toString().take(7) }
            val blockCount = getClaimBlockCount.execute(claim.id)
            chatInfo.addLinked(
                name,
                "<${claim.position.x}, ${claim.position.y}, ${claim.position.z} (${blockCount} Blocks)>"
            )
        }

        // Send the page of claims to player
        val totalPages = ceil(totalClaims / 10.0).toInt()
        player.sendMessage(chatInfo.createPaged(page, totalPages))
    }
}