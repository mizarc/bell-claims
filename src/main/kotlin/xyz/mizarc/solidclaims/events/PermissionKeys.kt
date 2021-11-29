package xyz.mizarc.solidclaims.events

import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.event.inventory.*
import org.bukkit.event.block.*
import org.bukkit.event.entity.*

/**
 * Represents a string value that pertains to certain events, the action that occurs on that event,
 * and the priority of that action over other PermissionKeys that act upon the same event.
 */
enum class PermissionKeys(val priority: Int, val alias: String, val events: Array<Pair<Class<out Event>, (l: Listener, e: Event) -> Unit>>) {
    /**
     * Every event. This has the least priority, and any explicit changes to other permissions will override the
     * actions of this one.
     */
    All(0, "all", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ClaimEventHandler::cancelEvent),
        Pair(InventoryOpenEvent::class.java,                ClaimEventHandler::cancelEvent),
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventHandler::cancelEvent))),

    /**
     * All block-related events. This includes breaking, placing, and interacting with any blocks.
     */
    AllBlocks(1, "allBlocks", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ClaimEventHandler::cancelEvent))),

    /**
     * When a block is broken by a player.
     */
    BlockBreak(2, "blockBreak", arrayOf(
        Pair(BlockBreakEvent::class.java,                   ClaimEventHandler::cancelEvent))),

    /**
     * When a block is placed by a player.
     */
    BlockPlace(2, "blockPlace", arrayOf(
        Pair(BlockPlaceEvent::class.java,                   ClaimEventHandler::cancelEvent),
        Pair(BlockMultiPlaceEvent::class.java,              ClaimEventHandler::cancelEvent))),

    /**
     * When a block is interacted with by a player.
     */
    BlockInteract(2, "blockInteract", arrayOf(
        Pair(BlockDamageEvent::class.java,                  ClaimEventHandler::cancelEvent),
        Pair(NotePlayEvent::class.java,                     ClaimEventHandler::cancelEvent),
        Pair(PlayerArmorStandManipulateEvent::class.java,   ClaimEventHandler::cancelEvent),
        Pair(PlayerTakeLecternBookEvent::class.java,        ClaimEventHandler::cancelEvent),
        Pair(PlayerUnleashEntityEvent::class.java,          ClaimEventHandler::cancelEvent),
        Pair(BlockFertilizeEvent::class.java,               ClaimEventHandler::cancelEvent))),
        // PlayerShearBlock, TargetHit, AnvilDamage

    /**
     * All inventory-related events. This is triggered every time a player opens anything with a stateful inventory.
     */
    AllInventories(1, "allInventories", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventHandler::cancelEvent))),

    /**
     * When a chest is opened by a player.
     */
    OpenChest(2, "openChest", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventHandler::cancelEvent))),

    /**
     * When a furnace is opened by a player.
     */
    OpenFurnace(2, "openFurnace", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventHandler::cancelEvent))),

    /**
     * All entity interaction-related events. This includes hurting, trading, leashing, and any other kind of
     * interaction with any entity.
     */
    AllEntities(1, "allEntities", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventHandler::cancelEvent))),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(2, "villagerTrade", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventHandler::cancelEvent))),

    /**
     * When an entity is hurt by a player.
     */
    EntityHurt(2, "entityHurt", arrayOf(
        Pair(EntityDamageByEntityEvent::class.java,         ClaimEventHandler::cancelEvent))),

    /**
     * When an entity is leashed by a player.
     */
    EntityLeash(2, "entityLeash", arrayOf(
        Pair(PlayerLeashEntityEvent::class.java,            ClaimEventHandler::cancelEvent))),

    /**
     * When an entity is sheared by a player.
     */
    EntityShear(2, "entityShear", arrayOf(
        Pair(PlayerShearEntityEvent::class.java,            ClaimEventHandler::cancelEvent)))
}