package dev.mizarc.bellclaims.interaction.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimBlockCount
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.flag.GetClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.partition.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.claim.permission.GetClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.domain.values.LocalizationKeys
import org.bukkit.entity.Player
import dev.mizarc.bellclaims.infrastructure.ChatInfoBuilder
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


@CommandAlias("claim")
class InfoCommand : ClaimCommand(), KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val getClaimDetails: GetClaimDetails by inject()
    private val getClaimFlags: GetClaimFlags by inject()
    private val getClaimPermissions: GetClaimPermissions by inject()
    private val getClaimPartitions: GetClaimPartitions by inject()
    private val getClaimBlockCount: GetClaimBlockCount by inject()
    private val getPlayersWithPermissionInClaim: GetPlayersWithPermissionInClaim by inject()

    @Subcommand("info")
    @CommandPermission("bellclaims.command.claim.info")
    fun onInfo(player: Player) {
        // Get partition at current location with associated claim
        val partition = getPartitionAtPlayer(player) ?: return
        val claimId = partition.claimId
        val claim = getClaimDetails.execute(claimId) ?: return

        // Format datetime for creation date
        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(Locale.UK)
            .withZone(ZoneId.systemDefault())

        // Add header and description
        val chatInfo = ChatInfoBuilder(localizationProvider, player.uniqueId,
            localizationProvider.get(player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_HEADER, claim.name))
        if (claim.description.isNotEmpty()) chatInfo.addParagraph("${claim.description}\n")

        // Add metadata values
        val ownerName = Bukkit.getOfflinePlayer(player.uniqueId).name ?: localizationProvider.get(
            player.uniqueId, LocalizationKeys.GENERAL_NAME_ERROR)
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_OWNER, ownerName))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_CREATION_DATE,
            dateTimeFormatter.format(claim.creationTime)))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_PARTITION_COUNT,
            getClaimPartitions.execute(claimId).count().toString()))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_BLOCK_COUNT,
            getClaimBlockCount.execute(claimId).toString()))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_FLAGS,
            getClaimFlags.execute(claimId).map { localizationProvider.get(player.uniqueId, it.nameKey) }))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_DEFAULT_PERMISSIONS,
            getClaimPermissions.execute(claimId).map { localizationProvider.get(player.uniqueId, it.nameKey) }))
        chatInfo.addRow(localizationProvider.get(
            player.uniqueId, LocalizationKeys.COMMAND_CLAIM_INFO_ROW_TRUSTED_USERS,
            getPlayersWithPermissionInClaim.execute(claimId).count().toString()))
        chatInfo.addSpace()

        // Output to player
        player.sendMessage(chatInfo.create())
    }
}