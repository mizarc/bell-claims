package xyz.mizarc.solidclaims.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import org.bukkit.event.inventory.*
import org.bukkit.event.block.*
import org.bukkit.event.entity.*

typealias EventExecutor = Pair<Class<out Event>, (l: Listener, e: Event) -> Unit>

/**
 * Represents a string value that pertains to certain events, the action that occurs on that event,
 * and the priority of that action over other PermissionKeys that act upon the same event.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val alias: String, val events: Array<EventExecutor>) {
    /**
     * Every event. This has the least priority, and any explicit changes to other permissions will override the
     * actions of this one.
     */
    All(null, "all", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ::cancelEvent),
        Pair(InventoryOpenEvent::class.java,                ::cancelEvent),
        Pair(PlayerInteractEntityEvent::class.java,         ::cancelEvent))),

    /**
     * All block-related events. This includes breaking, placing, and interacting with any blocks.
     */
    AllBlocks(All, "allBlocks", arrayOf(
        Pair(PlayerInteractEvent::class.java,               ::cancelEvent))),

    /**
     * When a block is broken/placed by a player.
     */
    Build(AllBlocks, "build", arrayOf(
        Pair(BlockBreakEvent::class.java,                   ::cancelEvent),
        Pair(BlockPlaceEvent::class.java,                   ::cancelEvent),
        Pair(BlockMultiPlaceEvent::class.java,              ::cancelEvent))),

    /**
     * When a block is interacted with by a player.
     */
    BlockInteract(AllBlocks, "blockInteract", arrayOf(
        Pair(BlockDamageEvent::class.java,                  ::cancelEvent),
        Pair(NotePlayEvent::class.java,                     ::cancelEvent),
        Pair(PlayerArmorStandManipulateEvent::class.java,   ::cancelEvent),
        Pair(PlayerTakeLecternBookEvent::class.java,        ::cancelEvent),
        Pair(PlayerUnleashEntityEvent::class.java,          ::cancelEvent),
        Pair(BlockFertilizeEvent::class.java,               ::cancelEvent))),
    // PlayerShearBlock, TargetHit, AnvilDamage

    /**
     * All inventory-related events. This is triggered every time a player opens anything with a stateful inventory.
     */
    AllInventories(All, "allInventories", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ::cancelEvent))),

    /**
     * When a chest is opened by a player.
     */
    OpenChest(AllInventories, "openChest", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ::cancelEvent))),

    /**
     * When a furnace is opened by a player.
     */
    OpenFurnace(AllInventories, "openFurnace", arrayOf(
        Pair(InventoryOpenEvent::class.java,                ::cancelEvent))),

    /**
     * All entity interaction-related events. This includes hurting, trading, leashing, and any other kind of
     * interaction with any entity.
     */
    AllEntities(All, "allEntities", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ::cancelEvent))),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(AllEntities, "villagerTrade", arrayOf(
        Pair(PlayerInteractEntityEvent::class.java,         ::cancelEvent))),

    /**
     * When an entity is hurt by a player.
     */
    EntityHurt(AllEntities, "entityHurt", arrayOf(
        Pair(EntityDamageByEntityEvent::class.java,         ::cancelEvent))),

    /**
     * When an entity is leashed by a player.
     */
    EntityLeash(AllEntities, "entityLeash", arrayOf(
        Pair(PlayerLeashEntityEvent::class.java,            ::cancelEvent))),

    /**
     * When an entity is sheared by a player.
     */
    EntityShear(AllEntities, "entityShear", arrayOf(
        Pair(PlayerShearEntityEvent::class.java,            ::cancelEvent)));

    /**
     * A collection of event handlers that can be associated to any number of events defined in the ClaimPermissions
     * enum that will be called when the appropriate conditions for that event are met.
     */
    @Suppress("UNUSED_PARAMETER")
    companion object {
        /**
         * The generic function to cancel any cancellable event.
         */
        private fun cancelEvent(listener: Listener, event: Event) {
            if (ClaimEventHandler.handleEvents) {
                if (event is Cancellable) {
                    event.isCancelled = true
                }
            }
        }

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