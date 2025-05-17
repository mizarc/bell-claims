package dev.mizarc.bellclaims.domain.values

object LocalizationKeys {
    // -------------------------------------
    // General Messages
    // -------------------------------------
    const val GENERAL_ERROR = "general.error"
    const val GENERAL_NAME_ERROR = "general.name_error"

    // -------------------------------------
    // Action Feedback
    // -------------------------------------
    const val FEEDBACK_CLAIM_DENIED = "feedback.claim.denied"

    // -------------------------------------
    // Conditions
    // -------------------------------------

    // Accept Transfer Conditions
    const val ACCEPT_TRANSFER_CONDITION_INVALID_REQUEST = "accept_transfer_condition.invalid_request"
    const val ACCEPT_TRANSFER_CONDITION_INVALID_CLAIM = "accept_transfer_condition.invalid_claim"
    const val ACCEPT_TRANSFER_CONDITION_OWNER = "accept_transfer_condition.owner"

    // Creation Conditions
    const val CREATION_CONDITION_BLOCKS = "creation_condition.blocks"
    const val CREATION_CONDITION_CLAIMS = "creation_condition.claims"
    const val CREATION_CONDITION_EXISTING = "creation_condition.existing"
    const val CREATION_CONDITION_OVERLAP = "creation_condition.overlap"
    const val CREATION_CONDITION_UNNAMED = "creation_condition.unnamed"

    // Transfer Conditions
    const val SEND_TRANSFER_CONDITION_BLOCKS = "send_transfer_condition.blocks"
    const val SEND_TRANSFER_CONDITION_CLAIMS = "send_transfer_condition.claims"
    const val SEND_TRANSFER_CONDITION_EXIST = "send_transfer_condition.exist"
    const val SEND_TRANSFER_CONDITION_OWNER = "send_transfer_condition.owner"


    // -------------------------------------
    // Permissions
    // -------------------------------------
    const val PERMISSION_BUILD_NAME = "permission.build.name"
    const val PERMISSION_BUILD_LORE = "permission.build.lore"
    const val PERMISSION_CONTAINER_NAME = "permission.container.name"
    const val PERMISSION_CONTAINER_LORE = "permission.container.lore"
    const val PERMISSION_DETONATE_NAME = "permission.detonate.name"
    const val PERMISSION_DETONATE_LORE = "permission.detonate.lore"
    const val PERMISSION_DISPLAY_NAME = "permission.display.name"
    const val PERMISSION_DISPLAY_LORE = "permission.display.lore"
    const val PERMISSION_DOOR_NAME = "permission.door.name"
    const val PERMISSION_DOOR_LORE = "permission.door.lore"
    const val PERMISSION_EVENT_NAME = "permission.event.name"
    const val PERMISSION_EVENT_LORE = "permission.event.lore"
    const val PERMISSION_HARVEST_NAME = "permission.harvest.name"
    const val PERMISSION_HARVEST_LORE = "permission.harvest.lore"
    const val PERMISSION_HUSBANDRY_NAME = "permission.husbandry.name"
    const val PERMISSION_HUSBANDRY_LORE = "permission.husbandry.lore"
    const val PERMISSION_VEHICLE_NAME = "permission.vehicle.name"
    const val PERMISSION_VEHICLE_LORE = "permission.vehicle.lore"
    const val PERMISSION_REDSTONE_NAME = "permission.redstone.name"
    const val PERMISSION_REDSTONE_LORE = "permission.redstone.lore"
    const val PERMISSION_SLEEP_NAME = "permission.sleep.name"
    const val PERMISSION_SLEEP_LORE = "permission.sleep.lore"
    const val PERMISSION_SIGN_NAME = "permission.sign.name"
    const val PERMISSION_SIGN_LORE = "permission.sign.lore"
    const val PERMISSION_TRADE_NAME = "permission.trade.name"
    const val PERMISSION_TRADE_LORE = "permission.trade.lore"


    // -------------------------------------
    // Flags
    // -------------------------------------
    const val FLAG_DISPENSER_NAME = "flag.dispenser.name"
    const val FLAG_DISPENSER_LORE = "flag.dispenser.lore"
    const val FLAG_EXPLOSION_NAME = "flag.explosion.name"
    const val FLAG_EXPLOSION_LORE = "flag.explosion.lore"
    const val FLAG_FALLING_BLOCK_NAME = "flag.falling_block.name"
    const val FLAG_FALLING_BLOCK_LORE = "flag.falling_block.lore"
    const val FLAG_FIRE_NAME = "flag.fire.name"
    const val FLAG_FIRE_LORE = "flag.fire.lore"
    const val FLAG_FLUID_NAME = "flag.fluid.name"
    const val FLAG_FLUID_LORE = "flag.fluid.lore"
    const val FLAG_LIGHTNING_NAME = "flag.lightning.name"
    const val FLAG_LIGHTNING_LORE = "flag.lightning.lore"
    const val FLAG_MOB_NAME = "flag.mob.name"
    const val FLAG_MOB_LORE = "flag.mob.lore"
    const val FLAG_PASSIVE_ENTITY_VEHICLE_NAME = "flag.passive_entity_vehicle.name"
    const val FLAG_PASSIVE_ENTITY_VEHICLE_LORE = "flag.passive_entity_vehicle.lore"
    const val FLAG_PISTON_NAME = "flag.piston.name"
    const val FLAG_PISTON_LORE = "flag.piston.lore"
    const val FLAG_TREE_NAME = "flag.tree.name"
    const val FLAG_TREE_LORE = "flag.tree.lore"
    const val FLAG_SCULK_NAME = "flag.sculk.name"
    const val FLAG_SCULK_LORE = "flag.sculk.lore"
    const val FLAG_SPONGE_NAME = "flag.sponge.name"
    const val FLAG_SPONGE_LORE = "flag.sponge.lore"


    // -------------------------------------
    // Menu Elements
    // -------------------------------------

    // Common Menu Items
    const val MENU_COMMON_ITEM_BACK_NAME = "menu.common.item.back.name"
    const val MENU_COMMON_ITEM_CLOSE_NAME = "menu.common.item.close.name"
    const val MENU_COMMON_ITEM_CONFIRM_NAME = "menu.common.item.confirm.name"
    const val MENU_COMMON_ITEM_DESELECT_ALL_NAME = "menu.common.item.deselect_all.name"
    const val MENU_COMMON_ITEM_ERROR_NAME = "menu.common.item.error.name"
    const val MENU_COMMON_ITEM_ERROR_LORE = "menu.common.item.error.lore"
    const val MENU_COMMON_ITEM_NEXT_NAME = "menu.common.item.next.name"
    const val MENU_COMMON_ITEM_PAGE_NAME = "menu.common.item.page.name"
    const val MENU_COMMON_ITEM_PREV_NAME = "menu.common.item.prev.name"
    const val MENU_COMMON_ITEM_SELECT_ALL_NAME = "menu.common.item.select_all.name"

    // All Players Menu
    const val MENU_ALL_PLAYERS_TITLE = "menu.all_players.title"
    const val MENU_ALL_PLAYERS_ITEM_SEARCH_NAME = "menu.all_players.item.search.name"
    const val MENU_ALL_PLAYERS_ITEM_SEARCH_LORE = "menu.all_players.item.search.lore"

    // Claim Wide Permissions Menu
    const val MENU_CLAIM_WIDE_PERMISSIONS_TITLE = "menu.claim_wide_permissions.title"
    const val MENU_CLAIM_WIDE_PERMISSIONS_ITEM_INFO_NAME = "menu.claim_wide_permissions.item.info.name"

    // Confirmation Menu
    const val MENU_CONFIRMATION_ITEM_NO_NAME = "menu.confirmation.item.no.name"
    const val MENU_CONFIRMATION_ITEM_NO_LORE = "menu.confirmation.item.no.lore"
    const val MENU_CONFIRMATION_ITEM_YES_NAME = "menu.confirmation.item.yes.name"
    const val MENU_CONFIRMATION_ITEM_YES_LORE = "menu.confirmation.item.yes.lore"

    // Creation Menu
    const val MENU_CREATION_TITLE = "menu.creation.title"
    const val MENU_CREATION_ITEM_CANNOT_CREATE_NAME = "menu.creation.item.cannot_create.name"
    const val MENU_CREATION_ITEM_CREATE_NAME = "menu.creation.item.create.name"
    const val MENU_CREATION_ITEM_CREATE_LORE_PROTECTED = "menu.creation.item.create.lore.protected"
    const val MENU_CREATION_ITEM_CREATE_LORE_REMAINING = "menu.creation.item.create.lore.remaining"

    // Flags Menu
    const val MENU_FLAGS_TITLE = "menu.flags.title"

    // Icon Menu items
    const val MENU_ICON_TITLE = "menu.icon.title"
    const val MENU_ICON_ITEM_INFO_NAME = "menu.icon.item.info.name"
    const val MENU_ICON_ITEM_INFO_LORE = "menu.icon.item.info.lore"

    // Management Menu
    const val MENU_MANAGEMENT_TITLE = "menu.management.title"
    const val MENU_MANAGEMENT_ITEM_FLAGS_NAME = "menu.management.item.flags.name"
    const val MENU_MANAGEMENT_ITEM_ICON_NAME = "menu.management.item.icon.name"
    const val MENU_MANAGEMENT_ITEM_ICON_LORE = "menu.management.item.icon.lore"
    const val MENU_MANAGEMENT_ITEM_MOVE_NAME = "menu.management.item.move.name"
    const val MENU_MANAGEMENT_ITEM_MOVE_LORE = "menu.management.item.move.lore"
    const val MENU_MANAGEMENT_ITEM_PERMISSIONS_NAME = "menu.management.item.permissions.name"
    const val MENU_MANAGEMENT_ITEM_RENAME_NAME = "menu.management.item.rename.name"
    const val MENU_MANAGEMENT_ITEM_RENAME_LORE = "menu.management.item.rename.lore"
    const val MENU_MANAGEMENT_ITEM_TOOL_NAME = "menu.management.item.tool.name"
    const val MENU_MANAGEMENT_ITEM_TOOL_LORE = "menu.management.item.tool.lore"

    // Naming Menu
    const val MENU_NAMING_TITLE = "menu.naming.title"
    const val MENU_NAMING_ITEM_CANNOT_CREATE_NAME = "menu.naming.item.cannot_create.name"

    // Player Permissions Menu
    const val MENU_PLAYER_PERMISSIONS_TITLE = "menu.player_permissions.title"
    const val MENU_PLAYER_PERMISSIONS_ITEM_CANCEL_TRANSFER_NAME = "menu.player_permissions.item.cancel_transfer.name"
    const val MENU_PLAYER_PERMISSIONS_ITEM_CANCEL_TRANSFER_LORE = "menu.player_permissions.item.cancel_transfer.lore"
    const val MENU_PLAYER_PERMISSIONS_ITEM_CANNOT_TRANSFER_NAME = "menu.player_permissions.item.cannot_transfer.name"
    const val MENU_PLAYER_PERMISSIONS_ITEM_TRANSFER_NAME = "menu.player_permissions.item.transfer.name"
    const val MENU_PLAYER_PERMISSIONS_ITEM_TRANSFER_LORE = "menu.player_permissions.item.transfer.lore"

    // Player Search Menu
    const val MENU_PLAYER_SEARCH_TITLE = "menu.player_search.title"
    const val MENU_PLAYER_SEARCH_ITEM_PLAYER_NAME = "menu.player_search.item.player.name"
    const val MENU_PLAYER_SEARCH_ITEM_PLAYER_UNKNOWN_NAME = "menu.player_search.item.player_unknown.name"
    const val MENU_PLAYER_SEARCH_ITEM_PLAYER_UNKNOWN_LORE = "menu.player_search.item.player_unknown.lore"

    // Renaming Menu
    const val MENU_RENAMING_TITLE = "menu.renaming.title"
    const val MENU_RENAMING_ITEM_EXISTING_NAME = "menu.renaming.item.existing.name"
    const val MENU_RENAMING_ITEM_UNKNOWN_NAME = "menu.renaming.item.unknown.name"

    // Transfer Menu
    const val MENU_TRANSFER_TITLE = "menu.transfer.title"

    // Trusted Players Menu
    const val MENU_TRUSTED_PLAYERS_TITLE = "menu.trusted_players.title"
    const val MENU_TRUSTED_PLAYERS_ITEM_ALL_PLAYERS_NAME = "menu.trusted_players.item.all_players.name"
    const val MENU_TRUSTED_PLAYERS_ITEM_ALL_PLAYERS_LORE = "menu.trusted_players.item.all_players.lore"
    const val MENU_TRUSTED_PLAYERS_ITEM_DEFAULT_PERMISSIONS_NAME = "menu.trusted_players.item.default_permissions.name"
    const val MENU_TRUSTED_PLAYERS_ITEM_DEFAULT_PERMISSIONS_LORE = "menu.trusted_players.item.default_permissions.lore"
    const val MENU_TRUSTED_PLAYERS_ITEM_HAS_PERMISSION_LORE = "menu.trusted_players.item.has_permission.lore"
}