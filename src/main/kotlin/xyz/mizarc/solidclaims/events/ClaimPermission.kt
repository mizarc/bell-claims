package xyz.mizarc.solidclaims.events

import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.event.inventory.*
import org.bukkit.event.block.*
import org.bukkit.event.entity.*

typealias ClaimEventExecutor = Pair<Class<out Event>, (l: Listener, e: Event) -> Unit>

/**
 * Represents a string value that pertains to certain events, the action that occurs on that event,
 * and the priority of that action over other PermissionKeys that act upon the same event.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val alias: String, val events: Array<ClaimEventExecutor>) {
    /**
     * Every event. This has the least priority, and any explicit changes to other permissions will override the
     * actions of this one.
     */
    All(null, "all", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ClaimEventBehaviour::cancelEvent),
        Pair(InventoryOpenEvent::class.java,                ClaimEventBehaviour::cancelEvent),
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventBehaviour::cancelEvent))),

    /**
     * All block-related events. This includes breaking, placing, and interacting with any blocks.
     */
    AllBlocks(All, "allBlocks", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ClaimEventBehaviour::cancelEvent))),

    /**
     * When a block is broken/placed by a player.
     */
    Build(AllBlocks, "build", arrayOf(
        Pair(BlockBreakEvent::class.java,                   ClaimEventBehaviour::cancelEvent),
        Pair(BlockPlaceEvent::class.java,                   ClaimEventBehaviour::cancelEvent),
        Pair(BlockMultiPlaceEvent::class.java,              ClaimEventBehaviour::cancelEvent))),

    /**
     * When a block is interacted with by a player.
     */
    BlockInteract(AllBlocks, "blockInteract", arrayOf(
        Pair(BlockDamageEvent::class.java,                  ClaimEventBehaviour::cancelEvent),
        Pair(NotePlayEvent::class.java,                     ClaimEventBehaviour::cancelEvent),
        Pair(PlayerArmorStandManipulateEvent::class.java,   ClaimEventBehaviour::cancelEvent),
        Pair(PlayerTakeLecternBookEvent::class.java,        ClaimEventBehaviour::cancelEvent),
        Pair(PlayerUnleashEntityEvent::class.java,          ClaimEventBehaviour::cancelEvent),
        Pair(BlockFertilizeEvent::class.java,               ClaimEventBehaviour::cancelEvent))),
    // PlayerShearBlock, TargetHit, AnvilDamage

    /**
     * All inventory-related events. This is triggered every time a player opens anything with a stateful inventory.
     */
    AllInventories(All, "allInventories", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventBehaviour::cancelEvent))),

    /**
     * When a chest is opened by a player.
     */
    OpenChest(AllInventories, "openChest", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventBehaviour::cancelEvent))),

    /**
     * When a furnace is opened by a player.
     */
    OpenFurnace(AllInventories, "openFurnace", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ClaimEventBehaviour::cancelEvent))),

    /**
     * All entity interaction-related events. This includes hurting, trading, leashing, and any other kind of
     * interaction with any entity.
     */
    AllEntities(All, "allEntities", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventBehaviour::cancelEvent))),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(AllEntities, "villagerTrade", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ClaimEventBehaviour::cancelEvent))),

    /**
     * When an entity is hurt by a player.
     */
    EntityHurt(AllEntities, "entityHurt", arrayOf(
        Pair(EntityDamageByEntityEvent::class.java,         ClaimEventBehaviour::cancelEvent))),

    /**
     * When an entity is leashed by a player.
     */
    EntityLeash(AllEntities, "entityLeash", arrayOf(
        Pair(PlayerLeashEntityEvent::class.java,            ClaimEventBehaviour::cancelEvent))),

    /**
     * When an entity is sheared by a player.
     */
    EntityShear(AllEntities, "entityShear", arrayOf(
        Pair(PlayerShearEntityEvent::class.java,            ClaimEventBehaviour::cancelEvent)));


    companion object {
        /**
         * Get all ClaimPermissions that encompass [event].
         */
        fun getPermissionsForEvent(event: Class<out Event>) : Array<ClaimPermission> {
            val perms: ArrayList<ClaimPermission> = ArrayList()
            for (v in values()) {
                for (e in v.events) {
                    if (e.first == event) {
                        perms.add(v)
                        continue
                    }
                }
            }
            return perms.toTypedArray()
        }
    }
}