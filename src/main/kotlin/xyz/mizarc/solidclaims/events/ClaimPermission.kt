package xyz.mizarc.solidclaims.events

import org.bukkit.event.Event

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val events: Array<PermissionExecutor>) {
    /**
     * Every event. This has the least priority, and any explicit changes to other permissions will override the
     * actions of this one.
     */
    All(null, arrayOf(
        PermissionBehaviour.blockBreak,
        PermissionBehaviour.blockPlace,
        PermissionBehaviour.openChest,
        PermissionBehaviour.openFurnace,
        PermissionBehaviour.villagerTrade,
        PermissionBehaviour.playerDamageEntity,
        PermissionBehaviour.leashEntity,
        PermissionBehaviour.shearEntity,
        PermissionBehaviour.blockDamage,
        PermissionBehaviour.armorStandManipulate,
        PermissionBehaviour.takeLecternBook,
        PermissionBehaviour.fertilize)),

    /**
     * All block-related events. This includes breaking, placing, and interacting with any blocks.
     */
    AllBlocks(All, arrayOf(
        PermissionBehaviour.blockBreak,
        PermissionBehaviour.blockPlace,
        PermissionBehaviour.blockDamage,
        PermissionBehaviour.armorStandManipulate,
        PermissionBehaviour.takeLecternBook,
        PermissionBehaviour.fertilize)),

    /**
     * When a block is broken/placed by a player.
     */
    Build(AllBlocks, arrayOf(PermissionBehaviour.blockBreak, PermissionBehaviour.blockPlace)),

    /**
     * When a block is interacted with by a player.
     */
    BlockInteract(AllBlocks, arrayOf(
        PermissionBehaviour.blockDamage,
        PermissionBehaviour.armorStandManipulate,
        PermissionBehaviour.takeLecternBook,
        PermissionBehaviour.fertilize)),

    /**
     * All inventory-related events. This is triggered every time a player opens anything with a stateful inventory.
     */
    AllInventories(All, arrayOf(
        PermissionBehaviour.openChest,
        PermissionBehaviour.openFurnace,
        PermissionBehaviour.openAnvil,
        PermissionBehaviour.villagerTrade)),

    /**
     * When a chest is opened by a player.
     */
    OpenChest(AllInventories, arrayOf(PermissionBehaviour.openChest)),

    /**
     * When a furnace is opened by a player.
     */
    OpenFurnace(AllInventories, arrayOf(PermissionBehaviour.openFurnace)),

    /**
     * When an anvil is opened by a player.
     */
    OpenAnvil(AllInventories, arrayOf(PermissionBehaviour.openAnvil)),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(AllInventories, arrayOf(PermissionBehaviour.villagerTrade)),

    /**
     * All entity interaction-related events. This includes hurting, trading, leashing, and any other kind of
     * interaction with any entity.
     */
    AllEntities(All, arrayOf(
        PermissionBehaviour.playerDamageEntity,
        PermissionBehaviour.leashEntity,
        PermissionBehaviour.shearEntity)),

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

        /**
         * Get all children of this permission
         */
        fun getAllPermissionChildren(perm: ClaimPermission): Array<ClaimPermission> {
            val children = ArrayList<ClaimPermission>()
            for (v in values()) {
                if (v == perm) continue
                if (v.parent == perm || children.contains(v.parent)) children.add(v)
            }
            return children.toTypedArray()
        }
    }
}