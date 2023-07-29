package dev.mizarc.bellclaims.listeners

import org.bukkit.event.Event

/**
 * Represents the expected behaviour of certain events in claims and the hierarchy of one permission to any others.
 */
enum class ClaimPermission(val parent: ClaimPermission?, val events: Array<PermissionExecutor>) {
    /**
     * When a block is broken/placed by a player.
     */
    Build(null, arrayOf(
        PermissionBehaviour.blockBreak,
        PermissionBehaviour.blockPlace,
        PermissionBehaviour.entityPlace,
        PermissionBehaviour.specialEntityDamage,
        PermissionBehaviour.fluidPlace,
        PermissionBehaviour.itemFramePlace,
        PermissionBehaviour.hangingEntityBreak,
        PermissionBehaviour.fertilize)),

    /**
     * When a door is opened by a player.
     */
    DoorOpen(null, arrayOf(
        PermissionBehaviour.openDoor)),

    /**
     * When a device used to activate redstone is interacted with by a player.
     */
    RedstoneUse(null, arrayOf(
        PermissionBehaviour.redstoneInteract)),

    /**
     * When a block is interacted with by a player.
     */
    DisplayTake(null, arrayOf(
        PermissionBehaviour.armorStandManipulate,
        PermissionBehaviour.takeLecternBook,
        PermissionBehaviour.miscDisplayInteractions,
        PermissionBehaviour.miscEntityDisplayInteractions)),

    /**
     * When a block is interacted with by a player.
     */
    Inventory(null, arrayOf(
        PermissionBehaviour.openInventory)),

    /**
     * When a villager or travelling merchant is traded with by a player.
     */
    VillagerTrade(null, arrayOf(PermissionBehaviour.villagerTrade)),

    /**
     * When an entity is hurt by a player.
     */
    EntityHurt(null, arrayOf(PermissionBehaviour.playerDamageEntity)),

    /**
     * When an entity is leashed by a player.
     */
    EntityLeash(null, arrayOf(PermissionBehaviour.leashEntity, PermissionBehaviour.fishingRod)),

    /**
     * When an entity is sheared by a player.
     */
    EntityShear(null, arrayOf(PermissionBehaviour.shearEntity));

    companion object {
        /**
         * Get all ClaimPermissions that encompass [event].
         */
        fun getPermissionsForEvent(event: Class<out Event>) : Array<ClaimPermission> {
            val perms: ArrayList<ClaimPermission> = ArrayList()
            for (permission in values()) {
                for (permissionEvent in permission.events) {
                    if (permissionEvent.eventClass == event) {
                        perms.add(permission)
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