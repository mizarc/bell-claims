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

    // Claim
    const val FEEDBACK_CLAIM_DENIED = "feedback.claim.denied"
    const val FEEDBACK_CLAIM_OWNER = "feedback.claim.owner"

    // Destruction
    const val FEEDBACK_DESTRUCTION_ATTACHED = "feedback.destruction.attached"
    const val FEEDBACK_DESTRUCTION_PERMISSION = "feedback.destruction.permission"
    const val FEEDBACK_DESTRUCTION_PENDING = "feedback.destruction.pending"
    const val FEEDBACK_DESTRUCTION_SUCCESS = "feedback.destruction.success"

    // Edit Tool
    const val FEEDBACK_EDIT_TOOL_IN_CLAIM = "feedback.edit_tool.in_claim"
    const val FEEDBACK_EDIT_TOOL_INSUFFICIENT = "feedback.edit_tool.insufficient"
    const val FEEDBACK_EDIT_TOOL_INVALID = "feedback.edit_tool.invalid"
    const val FEEDBACK_EDIT_TOOL_MINIMUM_SIZE = "feedback.edit_tool.minimum_size"
    const val FEEDBACK_EDIT_TOOL_NEW_PARTITION = "feedback.edit_tool.new_partition"
    const val FEEDBACK_EDIT_TOOL_NOT_CONNECTED = "feedback.edit_tool.not_connected"
    const val FEEDBACK_EDIT_TOOL_OVERLAP = "feedback.edit_tool.overlap"
    const val FEEDBACK_EDIT_TOOL_PERMISSION = "feedback_edit_tool.permission"
    const val FEEDBACK_EDIT_TOOL_START_EXTENSION = "feedback.edit_tool.start_extension"
    const val FEEDBACK_EDIT_TOOL_START_RESIZE = "feedback_edit_tool.start_resize"
    const val FEEDBACK_EDIT_TOOL_SUCCESSFUL_RESIZE = "feedback_edit_tool.successful_resize"
    const val FEEDBACK_EDIT_TOOL_TOO_CLOSE = "feedback.edit_tool.too_close"
    const val FEEDBACK_EDIT_TOOL_UNEQUIP_BUILD = "feedback.edit_tool.unequip_build"
    const val FEEDBACK_EDIT_TOOL_UNEQUIP_RESIZE = "feedback.edit_tool.unequip_resize"

    // Move Tool
    const val FEEDBACK_MOVE_TOOL_SUCCESS = "feedback.move_tool.success"
    const val FEEDBACK_MOVE_TOOL_OUTSIDE_BORDER = "feedback.move_tool.outside_border"
    const val FEEDBACK_MOVE_TOOL_NO_PERMISSION = "feedback.move_tool.no_permission"

    // Transfer
    const val FEEDBACK_TRANSFER_SUCCESS = "feedback.transfer.success"


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

    // Transfer Send Menu
    const val MENU_TRANSFER_SEND_TITLE = "menu.transfer_send.title"

    // Trusted Players Menu
    const val MENU_TRUSTED_PLAYERS_TITLE = "menu.trusted_players.title"
    const val MENU_TRUSTED_PLAYERS_ITEM_ALL_PLAYERS_NAME = "menu.trusted_players.item.all_players.name"
    const val MENU_TRUSTED_PLAYERS_ITEM_ALL_PLAYERS_LORE = "menu.trusted_players.item.all_players.lore"
    const val MENU_TRUSTED_PLAYERS_ITEM_DEFAULT_PERMISSIONS_NAME = "menu.trusted_players.item.default_permissions.name"
    const val MENU_TRUSTED_PLAYERS_ITEM_DEFAULT_PERMISSIONS_LORE = "menu.trusted_players.item.default_permissions.lore"
    const val MENU_TRUSTED_PLAYERS_ITEM_HAS_PERMISSION_LORE = "menu.trusted_players.item.has_permission.lore"


    // -------------------------------------
    // Commands
    // -------------------------------------

    // Common
    const val COMMAND_COMMON_INVALID_PAGE = "command.common.invalid_page"
    const val COMMAND_COMMON_UNKNOWN_CLAIM = "command.common.unknown_claim"

    // Info Box
    const val COMMAND_INFO_BOX_ROW = "command.info_box.row"
    const val COMMAND_INFO_BOX_PAGED = "command.info_box.paged"

    // Add Flag
    const val COMMAND_CLAIM_ADD_FLAG_SUCCESS = "command.claim.add_flag.success"
    const val COMMAND_CLAIM_ADD_FLAG_ALREADY_EXISTS = "command.claim.add_flag.already_exists"

    // Claim
    const val COMMAND_CLAIM_SUCCESS = "command.claim.success"
    const val COMMAND_CLAIM_ALREADY_HAVE_TOOL = "command.claim.already_have_tool"

    // Claim List
    const val COMMAND_CLAIM_LIST_NO_CLAIMS = "command.claim_list.no_claims"
    const val COMMAND_CLAIM_LIST_HEADER = "command.claim_list.header"
    const val COMMAND_CLAIM_LIST_ROW = "command.claim_list.row"

    // Claim Override
    const val COMMAND_CLAIM_OVERRIDE_ENABLED = "command.claim_override.enabled"
    const val COMMAND_CLAIM_OVERRIDE_DISABLED = "command.claim_override.disabled"

    // Claim Description
    const val COMMAND_CLAIM_DESCRIPTION_SUCCESS = "command.claim.description.success"
    const val COMMAND_CLAIM_DESCRIPTION_EXCEED_LIMIT = "command.claim.description.exceed_limit"
    const val COMMAND_CLAIM_DESCRIPTION_INVALID_CHARACTER = "command.claim.description.invalid_character"
    const val COMMAND_CLAIM_DESCRIPTION_BLACKLISTED_WORD = "command.claim.description.blacklisted_word"
    const val COMMAND_CLAIM_DESCRIPTION_BLANK = "command.claim.description.blank"

    // Claim Info
    const val COMMAND_CLAIM_INFO_SUMMARY = "command.claim.info.summary"
    const val COMMAND_CLAIM_INFO_ROW_OWNER = "command.claim.info.row.owner"
    const val COMMAND_CLAIM_INFO_ROW_CREATION_DATE = "command.claim.info.row.creation_date"
    const val COMMAND_CLAIM_INFO_ROW_PARTITION_COUNT = "command.claim.info.row.partition_count"
    const val COMMAND_CLAIM_INFO_ROW_BLOCK_COUNT = "command.claim.info.row.block_count"
    const val COMMAND_CLAIM_INFO_ROW_FLAGS = "command.claim.info.row.flags"
    const val COMMAND_CLAIM_INFO_ROW_DEFAULT_PERMISSIONS = "command.claim.info.row.default_permissions"
    const val COMMAND_CLAIM_INFO_ROW_TRUSTED_USERS = "command.claim.info.row.trusted_users"

    // Claim Partitions
    const val COMMAND_PARTITIONS_HEADER = "command.partitions.header"
    const val COMMAND_PARTITIONS_ROW = "command.partitions.row"

    // Claim Remove
    const val COMMAND_CLAIM_REMOVE_SUCCESS = "command.claim.remove.success"
    const val COMMAND_CLAIM_REMOVE_UNKNOWN_PARTITION = "command.claim.remove.unknown_partition"
    const val COMMAND_CLAIM_REMOVE_DISCONNECTED = "command.claim.remove.disconnected"
    const val COMMAND_CLAIM_REMOVE_EXPOSED_ANCHOR = "command.claim.remove.exposed_anchor"

    // Claim Remove Flag
    const val COMMAND_CLAIM_REMOVE_FLAG_SUCCESS = "command.claim.remove_flag.success"
    const val COMMAND_CLAIM_REMOVE_FLAG_DOES_NOT_EXIST = "command.claim.remove_flag.does_not_exist"

    // Claim Rename
    const val COMMAND_CLAIM_RENAME_SUCCESS = "command.claim.rename.success"
    const val COMMAND_CLAIM_RENAME_ALREADY_EXISTS = "command.claim.rename.already_exists"
    const val COMMAND_CLAIM_RENAME_EXCEED_LIMIT = "command.claim.rename.exceed_limit"
    const val COMMAND_CLAIM_RENAME_INVALID_CHARACTER = "command.claim.rename.invalid_character"
    const val COMMAND_CLAIM_RENAME_BLACKLISTED_WORD = "command.claim.rename.blacklisted_word"
    const val COMMAND_CLAIM_RENAME_BLANK = "command.claim.rename.blank"

    // Claim Trust All
    const val COMMAND_CLAIM_TRUST_ALL_SUCCESS = "command.claim.trust_all.success"
    const val COMMAND_CLAIM_TRUST_ALL_ALREADY_EXISTS = "command.claim.trust_all.already_exists"

    // Claim Trust
    const val COMMAND_CLAIM_TRUST_SUCCESS = "command.claim.trust.success"
    const val COMMAND_CLAIM_TRUST_ALREADY_EXISTS = "command.claim.trust.already_exists"

    // Claim Trust List
    const val COMMAND_CLAIM_TRUST_LIST_NO_PLAYERS = "command.claim.trust_list.no_players"
    const val COMMAND_CLAIM_TRUST_LIST_TRUSTED_PLAYERS = "command.claim.trust_list.trusted_players"

    // Claim Untrust All
    const val COMMAND_CLAIM_UNTRUST_ALL_SUCCESS = "command.claim.untrust_all.success"
    const val COMMAND_CLAIM_UNTRUST_ALL_DOES_NOT_EXIST = "command.claim.untrust_all.does_not_exist"

    // Claim Untrust
    const val COMMAND_CLAIM_UNTRUST_SUCCESS = "command.claim.untrust.success"
    const val COMMAND_CLAIM_UNTRUST_DOES_NOT_EXIST = "command.claim.untrust.does_not_exist"

    // -------------------------------------
    // Items
    // -------------------------------------

    // Claim Tool
    const val ITEM_CLAIM_TOOL_NAME = "item.claim_tool.name"
    const val ITEM_CLAIM_TOOL_LORE_MAIN_HAND = "item.claim_tool.lore.main_hand"
    const val ITEM_CLAIM_TOOL_LORE_OFF_HAND = "item.claim_tool.lore.off_hand"

    // Move Tool
    const val ITEM_MOVE_TOOL_NAME = "item.move_tool.name"
    const val ITEM_MOVE_TOOL_LORE = "item.move_tool.lore"
}