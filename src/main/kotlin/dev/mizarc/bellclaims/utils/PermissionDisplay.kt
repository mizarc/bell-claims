package dev.mizarc.bellclaims.utils

import dev.mizarc.bellclaims.domain.claims.Claim
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.permissions.ClaimPermission
import org.bukkit.entity.Item

/**
 * Associates claim permissions with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given permission enum.
 */
fun ClaimPermission.getIcon(): ItemStack {
    return when (this) {
        ClaimPermission.Build -> ItemStack(Material.DIAMOND_PICKAXE)
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
    }
}

/**
 * Display names for each permission.
 *
 * @return The set display name for the given permission enum.
 */
fun ClaimPermission.getDisplayName(): String {
    return when (this) {
        ClaimPermission.Build -> "Build"
        ClaimPermission.ContainerInspect -> "Inspect Containers"
        ClaimPermission.DisplayManipulate -> "Manipulate"
        ClaimPermission.VehicleDeploy -> "Deploy Vehicles"
        ClaimPermission.SignEdit -> "Edit Signs"
        ClaimPermission.RedstoneInteract -> "Trigger Redstone"
        ClaimPermission.DoorOpen -> "Open Doors"
        ClaimPermission.VillagerTrade -> "Trade Villagers"
        ClaimPermission.Husbandry -> "Husbandry"
        ClaimPermission.Detonate -> "Detonate"
        ClaimPermission.EventStart -> "Raid"
    }
}

/**
 * Display descriptions for each permission.
 *
 * @return The set display description for the given permission enum.
 */
fun ClaimPermission.getDescription(): String {
    return when (this) {
        ClaimPermission.Build -> "Grants permission to build"
        ClaimPermission.ContainerInspect -> "Grants permission to open inventories (Chest, Furnace, Anvil)"
        ClaimPermission.DisplayManipulate -> "Grants permission to manipulate display items (Item Frames, Armour Stands, Flower Pots)"
        ClaimPermission.VehicleDeploy -> "Grants permission to break and place vehicles (Boats, Minecarts)"
        ClaimPermission.SignEdit -> "Grants permission to edit signs"
        ClaimPermission.RedstoneInteract -> "Grants permission to use redstone interactions (Buttons, Levers, Pressure Plates)"
        ClaimPermission.DoorOpen -> "Grants permission to open and close doors"
        ClaimPermission.VillagerTrade -> "Grants permission to trade with villagers"
        ClaimPermission.Husbandry -> "Grants permission to interact with passive animals"
        ClaimPermission.Detonate -> "Grants permission to directly set off explosives"
        ClaimPermission.EventStart -> "Grants permission to start a raid event"
    }
}