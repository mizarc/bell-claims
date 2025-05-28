package dev.mizarc.bellclaims.utils

import dev.mizarc.bellclaims.application.utilities.LocalizationProvider
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import dev.mizarc.bellclaims.domain.values.Flag
import java.util.UUID

/**
 * Associates claim flags with a specific in-game item.
 *
 * @return ItemStack of the associated item for the given flag enum.
 */
fun Flag.getIcon(localizationProvider: LocalizationProvider, playerId: UUID): ItemStack {
    var item = when (this) {
        Flag.EXPLOSION -> ItemStack(Material.TNT)
        Flag.FIRE -> ItemStack(Material.FLINT_AND_STEEL)
        Flag.MOB -> ItemStack(Material.CREEPER_HEAD)
        Flag.PISTON -> ItemStack(Material.PISTON)
        Flag.FLUID -> ItemStack(Material.WATER_BUCKET)
        Flag.TREE -> ItemStack(Material.OAK_SAPLING)
        Flag.SCULK -> ItemStack(Material.SCULK_CATALYST)
        Flag.DISPENSER -> ItemStack(Material.DISPENSER)
        Flag.SPONGE -> ItemStack(Material.SPONGE)
        Flag.LIGHTNING -> ItemStack(Material.LIGHTNING_ROD)
        Flag.FALLING_BLOCK -> ItemStack(Material.ANVIL)
        Flag.PASSIVE_ENTITY_VEHICLE -> ItemStack(Material.OAK_BOAT)
    }

    // Get localized name and lore using the keys from the domain enum
    item = item.name(localizationProvider.get(playerId, this.nameKey))
    item = item.lore(localizationProvider.get(playerId, this.loreKey))
    return item
}