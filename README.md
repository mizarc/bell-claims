![Banner](branding/banner.svg)

# Bell Claims

Bell Claims is an intuitive and comprehensive land-claiming Spigot/PaperMC plugin for Minecraft multiplayer servers. It allows players to claim
an area for their bases and control who is allowed to interact with them in order to prevent griefing.

Here are some of the cool features you can expect:
- **Physically Grounded** - Claims are marked using Bells, yes the block. Manage your claim from here, or break it to destroy your whole claim. 
- **Intuitive GUI Menus** - Your entire claim can be managed using click menus. Never need to type a command ever again.
- **Flexible Border Creation** - Claim borders aren't limited to just squares. Expand your claim whichever way you want without expending claim area limits.
- **Efficient & Informative Visualisations** - Never have trouble finding the borders of your own claim, as well as the claims of others.

## Project status
Beta. Safe to run on testing servers, but would not recommend for production due to frequent breaking changes.

## Installation
Download the latest release (.jar file) from the releases tab and place it in your server's plugins folder. 

For additional functionality such as per player/rank permissions and claim limits, you must install 
[Vault](https://www.spigotmc.org/resources/vault.34315/) as well as a compatible permission and chat
metadata provider. [LuckPerms](https://luckperms.net/) is a recommended plugin for handling both.

## Getting Started
To establish a claim, place down a bell and shift right click it. This opens up a creation menu where you are able to 
name your claim. Once the claim is established, you will be presented with various options to do with claim management.

## Permissions
### Recommended User Permissions
- bellclaims.action.bell - Allows for the creation and management of claims.
- bellclaims.command.claimlist - Allows use of the `claimlist` command to display a list of the player's existing claims.
- bellclaims.command.claimmenu - Allows use of the `claimmenu` command to display a GUI list of the player's existing claims.
- bellclaims.command.claim - Allows use of the `claim` command which gives the player the claim tool.
- bellclaims.command.claim.info - Allows use of the `claim info` command to view information about the claim the player is standing in.
- bellclaims.command.claim.rename - Allows use of the `claim rename` command to rename the current claim.
- bellclaims.command.claim.description - Allows use of the `claim description` command to change the description of the claim.
- bellclaims.command.claim.partitions - Allows use of the `claim partitions` command to list the partitions that make up the claim.
- bellclaims.command.claim.addflag - Allows use of the `claim addflag` command for adding a claim flag to the current claim.
- bellclaims.command.claim.removeflag - Allows use of the `claim removeflag` command to remove a flag from the current claim.
- bellclaims.command.claim.trustlist - Allows use of the `claim trustlist` command to list the currently trusted players.
- bellclaims.command.claim.trust - Allows use of the `claim trust` command to give a player a permission in the claim.
- bellclaims.command.claim.untrust - Allows use of the `claim untrust` command to remove a permission from a player in the claim.
- bellclaims.command.claim.trustall - Allows use of the `claim trustall` command to set a default permission in the claim.
- bellclaims.command.claim.untrustall - Allows use of the `claim untrustall` command to unset a default permission in the claim.
- bellclaims.command.claim.remove - Allows use of the `claim remove` command to remove a partition from the current claim.

### Recommended Moderation Permissions
- bellclaims.command.claimoverride - Allows use of the `claimoverride` command bypass claim protections.

## Per Player Claims Limits
Ensure that you have a Vault provider installed to set limits as described out in the installation section. Each Vault 
provider plugin has its own way of implementing this feature. As LuckPerms is the recommended provider, instructions 
will make use of it as such.

To set a metadata for a player, use command:

`/lp group <group_name> meta set <limit_name> <desired_number>`

For groups:

`/lp user <user_name> meta set <limit_name> <desired_number>`

Here are the different limits you can set:
- bellclaims.claim_limit - Defines how many claim bells the player can own.
- bellclaims.claim_block_limit - Defines how many blocks the player's claims can occupy in total.

## Building from Source
### Requirements
- Java JDK 21 or newer
- Git

### Compiling
```
git clone https://gitlab.com/Mizarc/bellclaims.git
cd BellClaims/
./gradlew shadowJar
```
Built .jar binary can be found in the `build/libs` folder.

## Support
If you encounter any bugs, crashes, or unexpected behaviour, please [open an issue](https://github.com/mizarc/bell-claims/issues) in this repository.

## License
Bell Claims is licensed under the permissive MIT license. Please view [LICENSE](LICENSE) for more info.
