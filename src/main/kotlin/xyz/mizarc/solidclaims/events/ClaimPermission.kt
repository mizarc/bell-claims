package xyz.mizarc.solidclaims.events

import org.bukkit.event.Event

/**
 * Represents a string value that pertains to certain events, the action that occurs on that event,
 * and the priority of that action over other PermissionKeys that act upon the same event.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val events: Array<PermissionExecutor>) {
    /**
     * Every event. This has the least priority, and any explicit changes to other permissions will override the
     * actions of this one.
     */
    All(null, arrayOf(PermissionBehaviour.playerInteract, PermissionBehaviour.playerInteractEntity, PermissionBehaviour.playerDamageEntity)),

    /**
     * All block-related events. This includes breaking, placing, and interacting with any blocks.
     */
    AllBlocks(All, arrayOf(PermissionBehaviour.playerInteract)),

    /**
     * When a block is broken/placed by a player.
     */
    Build(AllBlocks, arrayOf(PermissionBehaviour.blockBreak)),

    /**
     * When a block is interacted with by a player.
     */
    BlockInteract(AllBlocks, arrayOf(PermissionBehaviour.blockDamage, PermissionBehaviour.armorStandManipulate, PermissionBehaviour.takeLecternBook, PermissionBehaviour.fertilize)),

    /**
     * All inventory-related events. This is triggered every time a player opens anything with a stateful inventory.
     */
    AllInventories(All, arrayOf(PermissionBehaviour.inventoryOpen)),

    /**
     * When a chest is opened by a player.
     */
    OpenChest(AllInventories, arrayOf(PermissionBehaviour.openChest)),

    /**
     * When a furnace is opened by a player.
     */
    OpenFurnace(AllInventories, arrayOf(PermissionBehaviour.openFurnace)),

    /**
     * All entity interaction-related events. This includes hurting, trading, leashing, and any other kind of
     * interaction with any entity.
     */
    AllEntities(All, arrayOf(PermissionBehaviour.playerInteractEntity)),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(AllEntities, arrayOf(PermissionBehaviour.villagerTrade)),

    /**
     * When an entity is hurt by a player.
     */
    EntityHurt(AllEntities, arrayOf(PermissionBehaviour.playerDamageEntity)),

    /**
     * When an entity is leashed by a player.
     */
    EntityLeash(AllEntities, arrayOf(PermissionBehaviour.leashEntity)),

    /**
     * When an entity is sheared by a player.
     */
    EntityShear(AllEntities, arrayOf(PermissionBehaviour.shearEntity));

    companion object {
        /**
         * Get all ClaimPermissions that encompass [event].
         */
        fun getPermissionsForEvent(event: Class<out Event>) : Array<ClaimPermission> {
            val perms: ArrayList<ClaimPermission> = ArrayList()
            for (v in values()) {
                for (e in v.events) {
                    if (e.eventClass == event) {
                        perms.add(v)
                        continue
                    }
                }
            }
            return perms.toTypedArray()
        }

        /**
         * Get the first PermissionExecutor that handles [event].
         */
        fun getPermissionExecutorForEvent(event: Class<out Event>): PermissionExecutor? {
            for (v in values()) {
                for (pe in v.events) {
                    if (pe.eventClass == event) {
                        return pe
                    }
                }
            }
            return null
        }
    }
}