package dev.mizarc.bellclaims.utils

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

/**
 * Creates a player head texture from a given player.
 *
 * @param player The player to take the head skin from.
 * @return The ItemStack of the head.
 */
fun createHead(player: OfflinePlayer): ItemStack {
    val head = ItemStack(Material.PLAYER_HEAD)
    val skullMeta = head.itemMeta as SkullMeta
    skullMeta.owningPlayer = player
    head.setItemMeta(skullMeta)
    return head
}