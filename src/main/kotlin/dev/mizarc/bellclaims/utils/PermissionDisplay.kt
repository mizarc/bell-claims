package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.listeners.ClaimPermission

fun ClaimPermission.getIcon(): ItemStack {
    return when (this) {
        ClaimPermission.Build -> ItemStack(Material.DIAMOND_PICKAXE)
        ClaimPermission.DoorOpen -> ItemStack(Material.ACACIA_DOOR)
        ClaimPermission.DisplayTake -> ItemStack(Material.ARMOR_STAND)
        ClaimPermission.Inventory -> ItemStack(Material.CHEST)
        ClaimPermission.RedstoneUse -> ItemStack(Material.LEVER)
        ClaimPermission.VillagerTrade -> ItemStack(Material.EMERALD)
        ClaimPermission.EntityHurt -> ItemStack(Material.IRON_SWORD)
        ClaimPermission.EntityLeash -> ItemStack(Material.LEAD)
        ClaimPermission.EntityShear -> ItemStack(Material.SHEARS)
        ClaimPermission.SignEdit -> ItemStack(Material.OAK_SIGN)
    }
}

fun ClaimPermission.getDisplayName(): String {
    return when (this) {
        ClaimPermission.Build -> "Build"
        ClaimPermission.DoorOpen -> "Open Doors"
        ClaimPermission.DisplayTake -> "Display Manipulate"
        ClaimPermission.Inventory -> "Inventories"
        ClaimPermission.RedstoneUse -> "Redstone Interact"
        ClaimPermission.VillagerTrade -> "Villager Trade"
        ClaimPermission.EntityHurt -> "Mob Hurt"
        ClaimPermission.EntityLeash -> "Mob Leash"
        ClaimPermission.EntityShear -> "Mob Shear"
        ClaimPermission.SignEdit -> "Sign Edit"
    }
}

fun ClaimPermission.getDescription(): String {
    return when (this) {
        ClaimPermission.Build -> "Grants permission to build"
        ClaimPermission.DoorOpen -> "Grants permission to open and close doors"
        ClaimPermission.DisplayTake -> "Grants permission to manipulate display items (Item Frames, Armour Stands, Flower Pots)"
        ClaimPermission.Inventory -> "Grants permission to open inventories (Chest, Furnace, Anvil)"
        ClaimPermission.RedstoneUse -> "Grants permission to use redstone interactions (Buttons, Levers, Pressure Plates)"
        ClaimPermission.VillagerTrade -> "Grants permission to trade with villagers"
        ClaimPermission.EntityHurt -> "Grants permission to damage passive mobs"
        ClaimPermission.EntityLeash -> "Grants permission to use leads on passive mobs"
        ClaimPermission.EntityShear -> "Grants permission to use shears on passive mobs"
        ClaimPermission.SignEdit -> "Grants permission to edit signs"
    }
}