# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [0.3.5]

### Added
- Protection against water placed on the edge of a claim turning concrete powder into concrete and lava into obsidian. Bypassed by the fluid flow flag.

### Fixed
- Server crashing due to math error when certain claim partition configurations are attempting to be visualised.


## [0.3.4]

### Fixed
- No visible inner border visualisation when set to view mode on claims that aren't completely filled in.
- Using bell while holding an item causing the item to be used, resulting in major issues like trident duplication.
- Claim flags bypassed when an action is being done in one claim that would have an effect in another.

## [0.3.3]

### Added
- Protection against players breaking pots with projectiles. Requires the build permission.
- Protection against mobs breaking pots with projectiles. Bypassed by the mob griefing flag.
- Protection against putting items in pots. Requires the manipulate permission.

### Fixed
- Tools being placed in pots which can be used to duplicate these items.
- Visualiser delay triggering when the visualiser is not currently being shown.
- Residual comments spamming the console.

## [0.3.2]

### Added
- Configurable delay on visualiser hide time to stop lag abuse.
- Configurable allowed refresh period for visualiser refreshes to stop lag abuse.

### Fixed
- Inability to add a new partition when the partition to attach to is on a chunk border.

## [0.3.1]

### Fixed
- Bell duplication when breaking a bell claim with a move item in your inventory.
- Player state unable to be retrieved if the player somehow bypassed the login player state creation.
- Other potential null-based issues based on invalid item, partition, or claim retrievals.

## [0.3.0]

### Added
- Ability to transfer claims to another player via a transfer request system. Player that is receiving the request must accept by interacting with the bell.
- Individual flags to allow the claim owner to control the existing tree growth and sculk spread protections.
- Protection against players triggering a raid through a bad omen effect. Requires the new raid permission.
- Protection against fluid flow griefing, with players able to place fluids outside of the claim and having it flow into a claim. Bypassed by the new fluid flow flag.
- Protection against mobs damaging animals and breaking static entities such as item frames and paintings. Bypassed by the mob griefing flag.
- Protection against trampling using rideable mobs. Requires the build permission.
- Protection against dispensers placed on the outer edge of the claim being used to grief with fluids or entities such as boats and armor stands. Bypassed by the new dispense flag.
- Protection against natural lightning causing damage and setting claim blocks alight. Bypassed by the new lightning damage flag.
- Protection against lightning damage created by a trident enchanted with channeling. Requires the husbandry permission.
- Protection against falling blocks being launched into the claim. Bypassed by the block launch flag.
- Protection against splash and lingering potions affecting passive mobs. Requires the husbandry permission.
- Protection against dispensed potions affecting players and passive mobs. Bypassed by the dispenser flag.
- Protection against sleeping in beds and setting respawn points at respawn anchors. Requires the new sleep permission.
- Config value for setting the initial claim size.
- Config value for setting the minimum partition size.
- Language file support, with all known instances of display text moved to a language file resource.
- English language complete.
- Chinese language machine translated. (Temporary, for demonstration)
- German language machine translated. (Temporary, for demonstration)
- Russian language machine translated. (Temporary, for demonstration)

### Changed
- Piston protection now only applies if the piston affecting the claim blocks is outside of the claim.
- Config keys are no longer auto added and have a fixed structure, default values are applied when values are missing.

### Fixed
- Multi-blocks bypassing protections when the source is placed outside of the claim.
- Protection event priority changed to "LOWEST" in order to override other plugins being able to bypass protections.
- Shift + right clicking an item in the player's own inventory inside a menu moves it and becomes irretrievable.

## [0.2.5]

### Fixed
- Creation of 1 block wide partitions due to funky math error that only applied when the minimum limit was set to 3x3.

## [0.2.4]

### Added
- Protection against painting placement. Requires the build permission.

### Changed
- Minimum partition size is now 3x3 down from "5x5", which was actually 7x7 due to it not counting the border.

### Fixed
- Placing bucket fluids by clicking through entities bypassing claim protections.
- Incorrect text output colour for adding a partition to a claim.

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
