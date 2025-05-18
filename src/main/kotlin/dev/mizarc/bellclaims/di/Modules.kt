package dev.mizarc.bellclaims.di

import dev.mizarc.bellclaims.BellClaims
import dev.mizarc.bellclaims.application.actions.claim.CreateClaim
import dev.mizarc.bellclaims.application.actions.claim.GetClaimAtPosition
import dev.mizarc.bellclaims.application.actions.claim.IsNewClaimLocationValid
import dev.mizarc.bellclaims.application.actions.claim.IsPlayerActionAllowed
import dev.mizarc.bellclaims.application.actions.claim.IsWorldActionAllowed
import dev.mizarc.bellclaims.application.actions.claim.ListPlayerClaims
import dev.mizarc.bellclaims.application.actions.claim.anchor.BreakClaimAnchor
import dev.mizarc.bellclaims.application.actions.claim.anchor.GetClaimAnchorAtPosition
import dev.mizarc.bellclaims.application.actions.claim.anchor.MoveClaimAnchor
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.DisableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.DoesClaimHaveFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableAllClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.flag.EnableClaimFlag
import dev.mizarc.bellclaims.application.actions.claim.flag.GetClaimFlags
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimBlockCount
import dev.mizarc.bellclaims.application.actions.claim.metadata.GetClaimDetails
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimDescription
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimIcon
import dev.mizarc.bellclaims.application.actions.claim.metadata.UpdateClaimName
import dev.mizarc.bellclaims.application.actions.claim.partition.CanRemovePartition
import dev.mizarc.bellclaims.application.actions.claim.partition.CreatePartition
import dev.mizarc.bellclaims.application.actions.claim.partition.GetClaimPartitions
import dev.mizarc.bellclaims.application.actions.claim.partition.GetPartitionByPosition
import dev.mizarc.bellclaims.application.actions.claim.partition.RemovePartition
import dev.mizarc.bellclaims.application.actions.claim.partition.ResizePartition
import dev.mizarc.bellclaims.application.actions.claim.permission.GetClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GetClaimPlayerPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GetPlayersWithPermissionInClaim
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantAllClaimWidePermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantAllPlayerClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantClaimWidePermission
import dev.mizarc.bellclaims.application.actions.claim.permission.GrantPlayerClaimPermission
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokeAllClaimWidePermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokeAllPlayerClaimPermissions
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokeClaimWidePermission
import dev.mizarc.bellclaims.application.actions.claim.permission.RevokePlayerClaimPermission
import dev.mizarc.bellclaims.application.actions.claim.transfer.AcceptTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.CanPlayerReceiveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.DoesPlayerHaveTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.OfferPlayerTransferRequest
import dev.mizarc.bellclaims.application.actions.claim.transfer.WithdrawPlayerTransferRequest
import dev.mizarc.bellclaims.application.actions.player.DoesPlayerHaveClaimOverride
import dev.mizarc.bellclaims.application.actions.player.GetRemainingClaimBlockCount
import dev.mizarc.bellclaims.application.actions.player.IsPlayerInClaimMenu
import dev.mizarc.bellclaims.application.actions.player.RegisterClaimMenuOpening
import dev.mizarc.bellclaims.application.actions.player.ToggleClaimOverride
import dev.mizarc.bellclaims.application.actions.player.UnregisterClaimMenuOpening
import dev.mizarc.bellclaims.application.actions.player.visualisation.ClearVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.DisplayVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.GetVisualisedClaimBlocks
import dev.mizarc.bellclaims.application.actions.player.visualisation.GetVisualiserMode
import dev.mizarc.bellclaims.application.actions.player.visualisation.IsPlayerVisualising
import dev.mizarc.bellclaims.application.actions.player.visualisation.RefreshVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.ScheduleClearVisualisation
import dev.mizarc.bellclaims.application.actions.player.visualisation.ToggleVisualiserMode
import dev.mizarc.bellclaims.application.persistence.ClaimFlagRepository
import dev.mizarc.bellclaims.application.persistence.ClaimPermissionRepository
import dev.mizarc.bellclaims.application.persistence.ClaimRepository
import dev.mizarc.bellclaims.application.persistence.PartitionRepository
import dev.mizarc.bellclaims.application.persistence.PlayerAccessRepository
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import dev.mizarc.bellclaims.application.services.ConfigService
import dev.mizarc.bellclaims.application.services.PlayerMetadataService
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.application.services.WorldManipulationService
import dev.mizarc.bellclaims.application.services.scheduling.SchedulerService
import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimFlagRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimPermissionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.ClaimRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.claims.PlayerAccessRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.partitions.PartitionRepositorySQLite
import dev.mizarc.bellclaims.infrastructure.persistence.players.PlayerStateRepositoryMemory
import dev.mizarc.bellclaims.infrastructure.persistence.storage.SQLiteStorage
import dev.mizarc.bellclaims.infrastructure.persistence.storage.Storage
import dev.mizarc.bellclaims.infrastructure.services.ConfigServiceBukkit
import dev.mizarc.bellclaims.infrastructure.services.PlayerMetadataServiceVault
import dev.mizarc.bellclaims.infrastructure.services.VisualisationServiceBukkit
import dev.mizarc.bellclaims.infrastructure.services.WorldManipulationServiceBukkit
import dev.mizarc.bellclaims.infrastructure.services.scheduling.SchedulerServiceBukkit
import dev.mizarc.bellclaims.infrastructure.utilities.LocalizationProviderProperties
import net.milkbowl.vault.chat.Chat
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

// Define your Koin module(s) - using a top-level val named appModule is common
fun appModule(plugin: BellClaims) = module {
    // --- Plugin ---
    single<Plugin> {plugin}
    single<File> { plugin.dataFolder }
    single<FileConfiguration> { plugin.config }
    single<Chat> {plugin.metadata }


    // --- Config ---
    single { get<ConfigService>().loadConfig() }


    // --- Infrastructure Layer Implementations ---

    // Storage Types
    single<Storage<*>> { SQLiteStorage(get()) }
    single { SQLiteStorage(get()) }

    // Repositories
    single<ClaimFlagRepository> { ClaimFlagRepositorySQLite(get()) }
    single<ClaimPermissionRepository> { ClaimPermissionRepositorySQLite(get()) }
    single<ClaimRepository> { ClaimRepositorySQLite(get()) }
    single<PartitionRepository> { PartitionRepositorySQLite(get()) }
    single<PlayerAccessRepository> { PlayerAccessRepositorySQLite(get()) }
    single<PlayerStateRepository> { PlayerStateRepositoryMemory() }

    // Services
    single<ConfigService> { ConfigServiceBukkit(get()) }
    single<PlayerMetadataService> { PlayerMetadataServiceVault(get(), get()) }
    single<VisualisationService> { VisualisationServiceBukkit() }
    single<WorldManipulationService> { WorldManipulationServiceBukkit() }
    single<SchedulerService> { SchedulerServiceBukkit(get()) }

    // Utilities
    single<LocalizationProvider> { LocalizationProviderProperties(get(), get()) }


    // --- Application Layer Actions ---

    // Claim
    singleOf(::CreateClaim)
    singleOf(::GetClaimAtPosition)
    singleOf(::IsNewClaimLocationValid)
    singleOf(::IsPlayerActionAllowed)
    singleOf(::IsWorldActionAllowed)
    singleOf(::ListPlayerClaims)

    // Claim / Anchor
    singleOf(::BreakClaimAnchor)
    singleOf(::GetClaimAnchorAtPosition)
    singleOf(::MoveClaimAnchor)

    // Claim / Flag
    singleOf(::DisableAllClaimFlags)
    singleOf(::DisableClaimFlag)
    singleOf(::DoesClaimHaveFlag)
    singleOf(::EnableAllClaimFlags)
    singleOf(::EnableClaimFlag)
    singleOf(::GetClaimFlags)

    // Claim / Metadata
    singleOf(::GetClaimBlockCount)
    singleOf(::GetClaimDetails)
    singleOf(::UpdateClaimDescription)
    singleOf(::UpdateClaimIcon)
    singleOf(::UpdateClaimName)

    // Claim / Partition
    singleOf(::CanRemovePartition)
    singleOf(::CreatePartition)
    singleOf(::GetClaimPartitions)
    singleOf(::GetPartitionByPosition)
    singleOf(::RemovePartition)
    singleOf(::ResizePartition)

    // Claim / Permission
    singleOf(::GetClaimPermissions)
    singleOf(::GetClaimPlayerPermissions)
    singleOf(::GetPlayersWithPermissionInClaim)
    singleOf(::GrantAllClaimWidePermissions)
    singleOf(::GrantAllPlayerClaimPermissions)
    singleOf(::GrantClaimWidePermission)
    singleOf(::GrantPlayerClaimPermission)
    singleOf(::RevokeAllClaimWidePermissions)
    singleOf(::RevokeAllPlayerClaimPermissions)
    singleOf(::RevokeClaimWidePermission)
    singleOf(::RevokePlayerClaimPermission)

    // Claim / Transfer
    singleOf(::AcceptTransferRequest)
    singleOf(::CanPlayerReceiveTransferRequest)
    singleOf(::DoesPlayerHaveTransferRequest)
    singleOf(::OfferPlayerTransferRequest)
    singleOf(::WithdrawPlayerTransferRequest)

    // Player
    singleOf(::DoesPlayerHaveClaimOverride)
    singleOf(::GetRemainingClaimBlockCount)
    singleOf(::IsPlayerInClaimMenu)
    singleOf(::RegisterClaimMenuOpening)
    singleOf(::ToggleClaimOverride)
    singleOf(::UnregisterClaimMenuOpening)

    // Player / Visualisation
    singleOf(::ClearVisualisation)
    singleOf(::DisplayVisualisation)
    singleOf(::GetVisualisedClaimBlocks)
    singleOf(::GetVisualiserMode)
    singleOf(::IsPlayerVisualising)
    singleOf(::RefreshVisualisation)
    singleOf(::ScheduleClearVisualisation)
    singleOf(::ToggleVisualiserMode)
}