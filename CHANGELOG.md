# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog, and this project adheres to Semantic Versioning.

## [Latest]
Update to support MC version 1.21

### Added
- Protection against players breeding animals. Requires the husbandry permission.
- Protection against players right-clicking dragon eggs, causing it to teleport. Requires the build permission.
- Protection against fluid being picked up from the ground. Requires the build permission.
- Protection against fluid being picked up from a cauldron. Requires the display manipulate permission.
- Protection against signs being dyed or glowed. Requires the sign edit permission.
- Protection against pumpkins being sheared. Requires the build permission.
- Protection against the usage of composters. Requires the display manipulate permission.

### Changed
- Mob hurt/leash/shear claim permissions merged into the "Husbandry" permission.

### Fixed
- Inability to break blocks in north or south direction of bell regardless of bell attachment.

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