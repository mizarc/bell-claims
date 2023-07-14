package xyz.mizarc.solidclaims.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import xyz.mizarc.solidclaims.listeners.ClaimPermission

fun ClaimPermission.getIcon(): ItemStack {
    return when (this) {
        ClaimPermission.All -> ItemStack(Material.BEACON)
        ClaimPermission.AllBlocks -> ItemStack(Material.GRASS_BLOCK)
        ClaimPermission.Build -> ItemStack(Material.DIAMOND_PICKAXE)
        ClaimPermission.BlockInteract -> ItemStack(Material.LECTERN)
        ClaimPermission.AllInventories -> ItemStack(Material.SHULKER_BOX)
        ClaimPermission.OpenChest -> ItemStack(Material.CHEST)
        ClaimPermission.OpenFurnace -> ItemStack(Material.FURNACE)
        ClaimPermission.OpenAnvil -> ItemStack(Material.ANVIL)
        ClaimPermission.VillagerTrade -> ItemStack(Material.EMERALD)
        ClaimPermission.AllEntities -> ItemStack(Material.ZOMBIE_SPAWN_EGG)
        ClaimPermission.EntityHurt -> ItemStack(Material.IRON_SWORD)
        ClaimPermission.EntityLeash -> ItemStack(Material.LEAD)
        ClaimPermission.EntityShear -> ItemStack(Material.SHEARS)
    }
}

fun ClaimPermission.getName(): String {
    return when (this) {
        ClaimPermission.All -> "All"
        ClaimPermission.AllBlocks -> "All Blocks"
        ClaimPermission.Build -> "Build"
        ClaimPermission.BlockInteract -> "Block Interact"
        ClaimPermission.AllInventories -> "All Inventories"
        ClaimPermission.OpenChest -> "Open Chests"
        ClaimPermission.OpenFurnace -> "Open Furnace"
        ClaimPermission.OpenAnvil -> "Open Anvil"
        ClaimPermission.VillagerTrade -> "Villager Trade"
        ClaimPermission.AllEntities -> "All Entities"
        ClaimPermission.EntityHurt -> "Entity Hurt"
        ClaimPermission.EntityLeash -> "Entity Leash"
        ClaimPermission.EntityShear -> "Entity Shear"
    }
}

fun ClaimPermission.getDescription(): String {
    return when (this) {
        ClaimPermission.All -> "Grants all permissions"
        ClaimPermission.AllBlocks -> "Grants permission to do anything with blocks (Build, Interact)"
        ClaimPermission.Build -> "Grants permission to build"
        ClaimPermission.BlockInteract -> "Grants permission to interact with blocks"
        ClaimPermission.AllInventories -> "Grants permission to open any inventory (Chest, Furnace, Anvil, Villager)"
        ClaimPermission.OpenChest -> "Grants permission to open chests"
        ClaimPermission.OpenFurnace -> "Grants permission to open furnaces"
        ClaimPermission.OpenAnvil -> "Grants permission to open anvils"
        ClaimPermission.VillagerTrade -> "Grants permission to trade with villagers"
        ClaimPermission.AllEntities -> "Grants permission to interact with entities (Hurt, Leash, Shear)"
        ClaimPermission.EntityHurt -> "Grants permission to damage entities"
        ClaimPermission.EntityLeash -> "Grants permission to use leads on entities"
        ClaimPermission.EntityShear -> "Grants permission to use shears on entities"
    }
}