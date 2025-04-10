package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.GetClaimBlockCount
import dev.mizarc.bellclaims.application.actions.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.GetClaimFlags
import dev.mizarc.bellclaims.application.actions.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.GetClaimPermissions
import dev.mizarc.bellclaims.application.actions.GetPlayersWithPermissionInClaim
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import dev.mizarc.bellclaims.utils.getDisplayName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


@CommandAlias("claim")
class InfoCommand : ClaimCommand(), KoinComponent {
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimFlags: GetClaimFlags by inject()
    private val getClaimPermissions: GetClaimPermissions by inject()
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val getClaimBlockCount: GetClaimBlockCount by inject()
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()

    @Subcommand("info")
    @CommandPermission("bellclaims.command.claim.info")
    fun onClaimInfo(player: Player) {
        // Get partition at current location with associated claim
        val partition = getPartitionAtPlayer(player) ?: return
        val claimId = partition.claimId
        val claim = getClaimDetails.execute(claimId) ?: return

        // Format datetime for creation date
        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())

        // Add header and description
        val chatInfo = ChatInfoBuilder("${claim.name} Summary")
        if (claim.description.isNotEmpty()) chatInfo.addParagraph("${claim.description}\n")

        // Add metadata values
        chatInfo.addLinked("Owner", claim.owner.name.toString())
        chatInfo.addLinked("Creation Date", dateTimeFormatter.format(claim.creationTime))
        chatInfo.addLinked("Partition Count", getClaimPartitions.execute(claimId).count().toString())
        chatInfo.addLinked("Block Count", getClaimBlockCount.execute(claimId).toString())
        chatInfo.addLinked("Flags", getClaimFlags.execute(claimId).map { it.getDisplayName() }.toString())
        chatInfo.addLinked("Default Permissions",
            getClaimPermissions.execute(claimId).map { it.getDisplayName() }.toString())
        chatInfo.addLinked("Trusted Users", getPlayersWithPermissionInClaim.execute(claimId).count().toString())
        chatInfo.addSpace()

        // Output to player
        player.sendMessage(chatInfo.create())
    }
}