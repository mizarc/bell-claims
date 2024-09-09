# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [0.2.4]

### Added
- Protection against painting placement. Requires the build permission.

### Changed
- Minimum partition size is now 3x3 down from "5x5", which was actually 7x7 due to it not counting the border.

### Fixed
- Placing bucket fluids by clicking through entities bypassing claim protections.

## [0.2.3]
Update to support MC version 1.21.1

### Fixed
- Inventory interactions not working in latest 1.21.1 version of MC.

## [0.2.2]

### Fixed
- Inability to break redstone when player has the build permission but doesn't have the redstone permission.
- Glow item frames can be placed in protected claims and items can also be placed in said frames, despite item removal affecting both normal and glow item frames.
- Missed projectile protection for item frames.

## [0.2.1]

### Fixed
- Existing permission enums that no longer exist but are present in database causing plugin to fail to load.

Note for migrations from pre 0.2 versions: Players will need to add the new Husbandry permission in place of the old Mob permissions. There will be residue in the database as "MobHurt", "MobLeash", and "MobShear" permissions are no longer used. You are free to remove these.

## [0.2.0]
Update to support MC version 1.21

### Added
- Amount of blocks remaining now displays when a corner resize action is started and finalised.
- Warning message when partition creation would result in it not connected to the claim.
- Ability to break claim bells owned by other players using claim override.
- Protection against players breeding animals. Requires the new husbandry permission.
- Protection against TNT being ignited by flint and steel or burning projectiles. Requires the new detonate permission.
- Protection against exploding beds and respawn anchors when used outside of their intended dimension. Requires the new detonate permission.
- Protection against end crystals being blown up. Requires the new detonate permission.
- Protection against players right-clicking dragon eggs, causing it to teleport. Requires the build permission.
- Protection against fluid being picked up from the ground. Requires the build permission.
- Protection against fluid being picked up from a cauldron. Requires the manipulate permission.
- Protection against signs being dyed or glowed. Requires the sign edit permission.
- Protection against pumpkins being sheared. Requires the build permission.
- Protection against the usage of composters. Requires the display manipulate permission.
- Protection against harvesting honey (bottling) and honeycombs (shearing) from beehives. Requires the husbandry permission.
- Protection against zombies breaking down doors. Bypassed by the mob griefing flag.
- Protection against projectile based weaponry, applied to any permission that protects entities.
- Protection against eating cakes. Requires the manipulate permission.

### Changed
- Mob hurt/leash/shear claim permissions merged into the "Husbandry" permission.
- Permissions have their display names modified to be verb before noun. Some names simplified such as "Display Manipulate" to just "Manipulate".

### Fixed
- Inability to break blocks in north or south direction of bell regardless of bell attachment.
- Bees unable to produce honey since they were affected by the mob griefing filter.
- Falling blocks not falling in claims (And potentially other non-monster entities that should be able to change state)
- GUIs consuming items shift clicked into them.
- Amount remaining on display incorrect when claim resize larger than the amount of blocks players have left.

## [0.1.2]
Update to support MC Version 1.20.6

### Changed
- Vault dependency is now optional. Without Vault and a compatible chat metadata provider, player limits are handled solely through the config.

## [0.1.1]

### Fixed
- GUIs consuming items placed into them.
- Deselecting all permissions/flags only deselecting one before throwing error.
- Visualiser not updating when changing edit tool mode.

## [0.1.0]
- Initial Pre-Release for Minecraft 1.20. Watch out for bugs!
