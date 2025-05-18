package dev.mizarc.bellclaims.utils

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.values.ClaimPermission

/**
 * Associates claim permissions with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given permission enum.
 */
fun ClaimPermission.getIcon(localizationProvider: LocalizationProvider): ItemStack {
    var item = when (this) {
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
        ClaimPermission.DETONATE -> ItemStack(Material.TNT)
        ClaimPermission.EVENT -> ItemStack(Material.OMINOUS_BOTTLE)
        ClaimPermission.SLEEP -> ItemStack(Material.RED_BED)
    }

    // Get localized name and lore using the keys from the domain enum
    item = item.name(localizationProvider.get(this.nameKey))
    item = item.lore(localizationProvider.get(this.loreKey))
    return item
}