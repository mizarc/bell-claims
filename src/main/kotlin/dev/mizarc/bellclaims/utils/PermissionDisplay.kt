package dev.mizarc.bellclaims.utils

import dev.mizarc.bellclaims.domain.entities.Claim
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.entities.ClaimPermission
import org.bukkit.entity.Item

/**
 * Associates claim permissions with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given permission enum.
 */
fun ClaimPermission.getIcon(): ItemStack {
    return when (this) {
        ClaimPermission.Build -> ItemStack(Material.DIAMOND_PICKAXE)
        ClaimPermission.Harvest -> ItemStack(Material.WHEAT)
        ClaimPermission.ContainerInspect -> ItemStack(Material.CHEST)
        ClaimPermission.DisplayManipulate -> ItemStack(Material.ARMOR_STAND)
        ClaimPermission.VehicleDeploy -> ItemStack(Material.MINECART)
        ClaimPermission.SignEdit -> ItemStack(Material.OAK_SIGN)
        ClaimPermission.RedstoneInteract -> ItemStack(Material.LEVER)
        ClaimPermission.DoorOpen -> ItemStack(Material.ACACIA_DOOR)
        ClaimPermission.VillagerTrade -> ItemStack(Material.EMERALD)
        ClaimPermission.Husbandry -> ItemStack(Material.LEAD)
        ClaimPermission.Detonate -> ItemStack(Material.TNT)
        ClaimPermission.EventStart -> ItemStack(Material.OMINOUS_BOTTLE)
        ClaimPermission.Sleep -> ItemStack(Material.RED_BED)
    }
}

/**
 * Display names for each permission.
 *
 * @return The set display name for the given permission enum.
 */
fun ClaimPermission.getDisplayName(): String {
    return when (this) {
        ClaimPermission.Build -> getLangText("NameClaimPermissionBuild")
        ClaimPermission.Harvest -> getLangText("NameClaimPermissionHarvest")
        ClaimPermission.ContainerInspect -> getLangText("NameClaimPermissionContainerInspect")
        ClaimPermission.DisplayManipulate -> getLangText("NameClaimPermissionDisplayManipulate")
        ClaimPermission.VehicleDeploy -> getLangText("NameClaimPermissionVehicleDeploy")
        ClaimPermission.SignEdit -> getLangText("NameClaimPermissionSignEdit")
        ClaimPermission.RedstoneInteract -> getLangText("NameClaimPermissionRedstoneInteract")
        ClaimPermission.DoorOpen -> getLangText("NameClaimPermissionDoorOpen")
        ClaimPermission.VillagerTrade -> getLangText("NameClaimPermissionVillagerTrade")
        ClaimPermission.Husbandry -> getLangText("NameClaimPermissionHusbandry")
        ClaimPermission.Detonate -> getLangText("NameClaimPermissionDetonate")
        ClaimPermission.EventStart -> getLangText("NameClaimPermissionEventStart")
        ClaimPermission.Sleep -> getLangText("NameClaimPermissionSleep")
    }
}

/**
 * Display descriptions for each permission.
 *
 * @return The set display description for the given permission enum.
 */
fun ClaimPermission.getDescription(): String {
    return when (this) {
        ClaimPermission.Build -> getLangText("DescClaimPermissionBuild")
        ClaimPermission.Harvest -> getLangText("DescClaimPermissionHarvest")
        ClaimPermission.ContainerInspect -> getLangText("DescClaimPermissionContainerInspect")
        ClaimPermission.DisplayManipulate -> getLangText("DescClaimPermissionDisplayManipulate")
        ClaimPermission.VehicleDeploy -> getLangText("DescClaimPermissionVehicleDeploy")
        ClaimPermission.SignEdit -> getLangText("DescClaimPermissionSignEdit")
        ClaimPermission.RedstoneInteract -> getLangText("DescClaimPermissionRedstoneInteract")
        ClaimPermission.DoorOpen -> getLangText("DescClaimPermissionDoorOpen")
        ClaimPermission.VillagerTrade -> getLangText("DescClaimPermissionVillagerTrade")
        ClaimPermission.Husbandry -> getLangText("DescClaimPermissionHusbandry")
        ClaimPermission.Detonate -> getLangText("DescClaimPermissionDetonate")
        ClaimPermission.EventStart -> getLangText("DescClaimPermissionEventStart")
        ClaimPermission.Sleep -> getLangText("DescClaimPermissionSleep")
    }
}