package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.values.ClaimPermission

/**
 * Associates claim permissions with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given permission enum.
 */
fun ClaimPermission.getIcon(): ItemStack {
    return when (this) {
        ClaimPermission.BUILD -> ItemStack(Material.DIAMOND_PICKAXE)
        ClaimPermission.HARVEST -> ItemStack(Material.WHEAT)
        ClaimPermission.CONTAINER -> ItemStack(Material.CHEST)
        ClaimPermission.DISPLAY -> ItemStack(Material.ARMOR_STAND)
        ClaimPermission.VEHICLE -> ItemStack(Material.MINECART)
        ClaimPermission.SIGN -> ItemStack(Material.OAK_SIGN)
        ClaimPermission.REDSTONE -> ItemStack(Material.LEVER)
        ClaimPermission.DOOR -> ItemStack(Material.ACACIA_DOOR)
        ClaimPermission.TRADE -> ItemStack(Material.EMERALD)
        ClaimPermission.HUSBANDRY -> ItemStack(Material.LEAD)
        ClaimPermission.EXPLOSIONS -> ItemStack(Material.TNT)
        ClaimPermission.EVENT -> ItemStack(Material.OMINOUS_BOTTLE)
        ClaimPermission.SLEEP -> ItemStack(Material.RED_BED)
    }
}

/**
 * Display names for each permission.
 *
 * @return The set display name for the given permission enum.
 */
fun ClaimPermission.getDisplayName(): String {
    return when (this) {
        ClaimPermission.BUILD -> getLangText("NameClaimPermissionBuild")
        ClaimPermission.HARVEST -> getLangText("NameClaimPermissionHarvest")
        ClaimPermission.CONTAINER -> getLangText("NameClaimPermissionContainerInspect")
        ClaimPermission.DISPLAY -> getLangText("NameClaimPermissionDisplayManipulate")
        ClaimPermission.VEHICLE -> getLangText("NameClaimPermissionVehicleDeploy")
        ClaimPermission.SIGN -> getLangText("NameClaimPermissionSignEdit")
        ClaimPermission.REDSTONE -> getLangText("NameClaimPermissionRedstoneInteract")
        ClaimPermission.DOOR -> getLangText("NameClaimPermissionDoorOpen")
        ClaimPermission.TRADE -> getLangText("NameClaimPermissionVillagerTrade")
        ClaimPermission.HUSBANDRY -> getLangText("NameClaimPermissionHusbandry")
        ClaimPermission.EXPLOSIONS -> getLangText("NameClaimPermissionDetonate")
        ClaimPermission.EVENT -> getLangText("NameClaimPermissionEventStart")
        ClaimPermission.SLEEP -> getLangText("NameClaimPermissionSleep")
    }
}

/**
 * Display descriptions for each permission.
 *
 * @return The set display description for the given permission enum.
 */
fun ClaimPermission.getDescription(): String {
    return when (this) {
        ClaimPermission.BUILD -> getLangText("DescClaimPermissionBuild")
        ClaimPermission.HARVEST -> getLangText("DescClaimPermissionHarvest")
        ClaimPermission.CONTAINER -> getLangText("DescClaimPermissionContainerInspect")
        ClaimPermission.DISPLAY -> getLangText("DescClaimPermissionDisplayManipulate")
        ClaimPermission.VEHICLE -> getLangText("DescClaimPermissionVehicleDeploy")
        ClaimPermission.SIGN -> getLangText("DescClaimPermissionSignEdit")
        ClaimPermission.REDSTONE -> getLangText("DescClaimPermissionRedstoneInteract")
        ClaimPermission.DOOR -> getLangText("DescClaimPermissionDoorOpen")
        ClaimPermission.TRADE -> getLangText("DescClaimPermissionVillagerTrade")
        ClaimPermission.HUSBANDRY -> getLangText("DescClaimPermissionHusbandry")
        ClaimPermission.EXPLOSIONS -> getLangText("DescClaimPermissionDetonate")
        ClaimPermission.EVENT -> getLangText("DescClaimPermissionEventStart")
        ClaimPermission.SLEEP -> getLangText("DescClaimPermissionSleep")
    }
}