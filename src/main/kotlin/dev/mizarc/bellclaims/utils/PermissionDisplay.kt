package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.interaction.listeners.ClaimPermission

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
        ClaimPermission.MobHurt -> ItemStack(Material.IRON_SWORD)
        ClaimPermission.MobLeash -> ItemStack(Material.LEAD)
        ClaimPermission.MobShear -> ItemStack(Material.SHEARS)
    }
}

fun ClaimPermission.getDisplayName(): String {
    return when (this) {
        ClaimPermission.Build -> "Build"
        ClaimPermission.ContainerInspect -> "Container Inspect"
        ClaimPermission.DisplayManipulate -> "Display Manipulate"
        ClaimPermission.VehicleDeploy -> "Vehicle Deploy"
        ClaimPermission.SignEdit -> "Sign Edit"
        ClaimPermission.RedstoneInteract -> "Redstone Interact"
        ClaimPermission.DoorOpen -> "Door Open"
        ClaimPermission.VillagerTrade -> "Villager Trade"
        ClaimPermission.MobHurt -> "Mob Hurt"
        ClaimPermission.MobLeash -> "Mob Leash"
        ClaimPermission.MobShear -> "Mob Shear"
    }
}

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
        ClaimPermission.MobHurt -> "Grants permission to damage passive mobs"
        ClaimPermission.MobLeash -> "Grants permission to use leads on passive mobs"
        ClaimPermission.MobShear -> "Grants permission to use shears on passive mobs"
    }
}