# Solid Claims

Bell Claims is an intuitive and comprehensive land-claiming Spigot/PaperMC plugin for Minecraft multiplayer servers. It allows players to claim
an area for their bases and control who is allowed to interact with them in order to prevent griefing.

Here are some of the cool features you can expect:
- **Physically Grounded** - Claims are marked using Bells, yes the block. Manage your claim from here, or break it to destroy your whole claim. 
- **Intuitive GUI Menus** - Your entire claim can be managed using click menus. Never need to type a command ever again.
- **Flexible Border Creation** - Claim borders aren't limited to just squares. Expand your claim whichever way you want without expending claim area limits.
- **Efficient & Informative Visualisations** - Never have trouble finding the borders of your own claim, as well as the claims of others.

## Project status
Alpha. Safe to run on testing servers, but would not recommend for production due to frequent breaking changes.

## Installation
Download the latest release (.jar file) from the releases tab and place it in your server's plugins folder. 

## Getting Started
To establish a claim, place down a bell and shift right click it. This opens up a creation menu where you are able to name your claim. Once the claim is established, you will be presented with various options to do with claim management.

## Permissions
### Recommended User Permissions
- bellclaims.action.bell - Allows for the creation and management of claims.
- bellclaims.command.claimlist - Allows use of the `claimlist` command to display a list of the player's existing claims.
- bellclaims.command.claimmenu - Allows use of the `claimmenu` command to display a GUI list of the player's existing claims.
- bellclaims.command.claim - Allows use of the `claim` command which gives the player the claim tool.
- bellclaims.command.info - Allows use of the `claim info` command to view information about the claim the player is standing in.
- bellclaims.command.rename - Allows use of the `claim rename` command to rename the current claim.
- bellclaims.command.partitionlist - Allows use of the `claim partitionlist` command to list the partitions that make up the claim.
- bellclaims.command.addrule - Allows use of the `claim addrule` command for adding a claim rule to the current claim.
- bellclaims.command.removerule - Allows use of the `claim removerule` command to remove a rule from the current claim.
- bellclaims.command.trustlist - Allows use of the `claim trustlist` command to list the currently trusted players.
- bellclaims.command.trust - Allows use of the `claim trust` command to give a player a permission in the claim.
- bellclaims.command.untrust - Allows use of the `claim untrust` command to remove a permission from a player in the claim.
- bellclaims.command.unclaim - Allows use of the `unclaim` command to remove a partition from the current claim.

### Recommended Moderation Permissions
- bellclaims.command.claimoverride - Allows you to bypass claim protections.

## Building from Source
### Requirements
- Java JDK 17 or newer
- Git

### Compiling
```
git clone https://gitlab.com/Mizarc/bellclaims.git
cd BellClaims/
./gradlew shadowjar
```
Built .jar binary can be found in the `build/libs` folder.

## Support
If you encounter any bugs, crashes, or unexpected behaviour, please [open an issue](https://gitlab.com/Mizarc/BellClaims/-/issues) in this repository.

## License
Solid Claims is licensed under the permissive MIT license. Please view [LICENSE](https://gitlab.com/Mizarc/BellClaims/-/blob/main/LICENSE) for more info.

