package xyz.mizarc.solidclaims.listeners

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.plugin.java.JavaPlugin
import xyz.mizarc.solidclaims.ClaimService
import xyz.mizarc.solidclaims.PartitionService
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.getClaimTool
import xyz.mizarc.solidclaims.partitions.Partition
import xyz.mizarc.solidclaims.partitions.Position2D
import xyz.mizarc.solidclaims.partitions.Position3D
import xyz.mizarc.solidclaims.players.PlayerStateRepository


private const val upperRange = 5
private const val lowerRange = 20

class ClaimVisualiser(private val plugin: JavaPlugin,
                      private val claimService: ClaimService,
                      private val partitionService: PartitionService,
                      private val playerStateRepo: PlayerStateRepository) : Listener {

    enum class Direction {
        North,
        South,
        East,
        West
    }

    companion object {
        private val transparentMaterials = arrayOf(
            Material.AIR,
            Material.OAK_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.BUDDING_AMETHYST,
            Material.COBWEB,
            Material.GRASS,
            Material.FERN,
            Material.DEAD_BUSH,
            Material.SEAGRASS,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE,
            Material.SPORE_BLOSSOM,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS,
            Material.CRIMSON_ROOTS,
            Material.WARPED_ROOTS,
            Material.NETHER_SPROUTS,
            Material.WEEPING_VINES,
            Material.TWISTING_VINES,
            Material.SUGAR_CANE,
            Material.KELP,
            Material.MOSS_CARPET,
            Material.HANGING_ROOTS,
            Material.BIG_DRIPLEAF,
            Material.SMALL_DRIPLEAF,
            Material.TORCH,
            Material.END_ROD,
            Material.CHORUS_PLANT,
            Material.CHORUS_FLOWER,
            Material.CHEST,
            Material.LADDER,
            Material.OAK_FENCE,
            Material.SPRUCE_FENCE,
            Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE,
            Material.ACACIA_FENCE,
            Material.DARK_OAK_FENCE,
            Material.CRIMSON_FENCE,
            Material.WARPED_FENCE,
            Material.SOUL_TORCH,
            Material.IRON_BARS,
            Material.CHAIN,
            Material.GLASS_PANE,
            Material.VINE,
            Material.GLOW_LICHEN,
            Material.LILY_PAD,
            Material.ENCHANTING_TABLE,
            Material.END_PORTAL,
            Material.END_PORTAL_FRAME,
            Material.DRAGON_EGG,
            Material.ENDER_CHEST,
            Material.BEACON,
            Material.COBBLESTONE_WALL,
            Material.MOSSY_COBBLESTONE_WALL,
            Material.BRICK_WALL,
            Material.PRISMARINE_WALL,
            Material.RED_SANDSTONE_WALL,
            Material.MOSSY_STONE_BRICK_WALL,
            Material.GRANITE_WALL,
            Material.STONE_BRICK_WALL,
            Material.NETHER_BRICK_WALL,
            Material.ANDESITE_WALL,
            Material.RED_NETHER_BRICK_WALL,
            Material.SANDSTONE_WALL,
            Material.END_STONE_BRICK_WALL,
            Material.DIORITE_WALL,
            Material.BLACKSTONE_WALL,
            Material.POLISHED_BLACKSTONE_WALL,
            Material.COBBLED_DEEPSLATE_WALL,
            Material.POLISHED_DEEPSLATE_WALL,
            Material.DEEPSLATE_BRICK_WALL,
            Material.DEEPSLATE_TILE_WALL,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.BARRIER,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.TALL_GRASS,
            Material.LARGE_FERN,
            Material.WHITE_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE,
            Material.GRAY_STAINED_GLASS_PANE,
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.BROWN_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE,
            Material.BLACK_STAINED_GLASS_PANE,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.TURTLE_EGG,
            Material.TUBE_CORAL,
            Material.BRAIN_CORAL,
            Material.BUBBLE_CORAL,
            Material.FIRE_CORAL,
            Material.HORN_CORAL,
            Material.DEAD_BRAIN_CORAL,
            Material.DEAD_BUBBLE_CORAL,
            Material.DEAD_FIRE_CORAL,
            Material.DEAD_HORN_CORAL,
            Material.DEAD_TUBE_CORAL,
            Material.TUBE_CORAL_FAN,
            Material.BRAIN_CORAL_FAN,
            Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN,
            Material.HORN_CORAL_FAN,
            Material.DEAD_TUBE_CORAL_FAN,
            Material.DEAD_BRAIN_CORAL_FAN,
            Material.DEAD_BUBBLE_CORAL_FAN,
            Material.DEAD_FIRE_CORAL_FAN,
            Material.DEAD_HORN_CORAL_FAN,
            Material.CONDUIT,
            Material.SCAFFOLDING,
            Material.REDSTONE,
            Material.REDSTONE_TORCH,
            Material.REPEATER,
            Material.COMPARATOR,
            Material.PISTON,
            Material.STICKY_PISTON,
            Material.HOPPER,
            Material.LECTERN,
            Material.LEVER,
            Material.LIGHTNING_ROD,
            Material.SCULK_SENSOR,
            Material.TRIPWIRE_HOOK,
            Material.TRAPPED_CHEST,
            Material.STONE_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON,
            Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,
            Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON,
            Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON,
            Material.CRIMSON_BUTTON,
            Material.WARPED_BUTTON,
            Material.STONE_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.IRON_DOOR,
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,
            Material.CRIMSON_DOOR,
            Material.WARPED_DOOR,
            Material.IRON_TRAPDOOR,
            Material.OAK_TRAPDOOR,
            Material.SPRUCE_TRAPDOOR,
            Material.BIRCH_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR,
            Material.ACACIA_TRAPDOOR,
            Material.DARK_OAK_TRAPDOOR,
            Material.CRIMSON_TRAPDOOR,
            Material.WARPED_TRAPDOOR,
            Material.OAK_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.ACACIA_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,
            Material.CRIMSON_FENCE_GATE,
            Material.WARPED_FENCE_GATE,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.RAIL,
            Material.ACTIVATOR_RAIL,
            Material.WHEAT,
            Material.OAK_SIGN,
            Material.SPRUCE_SIGN,
            Material.BIRCH_SIGN,
            Material.JUNGLE_SIGN,
            Material.ACACIA_SIGN,
            Material.DARK_OAK_SIGN,
            Material.CRIMSON_SIGN,
            Material.WARPED_SIGN,
            Material.CAKE,
            Material.WHITE_BED,
            Material.ORANGE_BED,
            Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.PINK_BED,
            Material.GRAY_BED,
            Material.LIGHT_GRAY_BED,
            Material.CYAN_BED,
            Material.PURPLE_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.GREEN_BED,
            Material.RED_BED,
            Material.BLACK_BED,
            Material.NETHER_WART,
            Material.BREWING_STAND,
            Material.ITEM_FRAME,
            Material.GLOW_ITEM_FRAME,
            Material.FLOWER_POT,
            Material.SKELETON_SKULL,
            Material.WITHER_SKELETON_SKULL,
            Material.PLAYER_HEAD,
            Material.ZOMBIE_HEAD,
            Material.CREEPER_HEAD,
            Material.DRAGON_HEAD,
            Material.WHITE_BANNER,
            Material.ORANGE_BANNER,
            Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.YELLOW_BANNER,
            Material.LIME_BANNER,
            Material.PINK_BANNER,
            Material.GRAY_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.CYAN_BANNER,
            Material.PURPLE_BANNER,
            Material.BLUE_BANNER,
            Material.BROWN_BANNER,
            Material.GREEN_BANNER,
            Material.RED_BANNER,
            Material.BLACK_BANNER,
            Material.END_CRYSTAL,
            Material.COMPOSTER,
            Material.BARRIER,
            Material.SMOKER,
            Material.BLAST_FURNACE,
            Material.CARTOGRAPHY_TABLE,
            Material.FLETCHING_TABLE,
            Material.GRINDSTONE,
            Material.SMITHING_TABLE,
            Material.STONECUTTER,
            Material.BELL,
            Material.LANTERN,
            Material.SOUL_LANTERN,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.CANDLE,
            Material.WHITE_CANDLE,
            Material.ORANGE_CANDLE,
            Material.MAGENTA_CANDLE,
            Material.LIGHT_BLUE_CANDLE,
            Material.YELLOW_CANDLE,
            Material.LIME_CANDLE,
            Material.PINK_CANDLE,
            Material.GRAY_CANDLE,
            Material.LIGHT_GRAY_CANDLE,
            Material.CYAN_CANDLE,
            Material.PURPLE_CANDLE,
            Material.BLUE_CANDLE,
            Material.BROWN_CANDLE,
            Material.GREEN_CANDLE,
            Material.RED_CANDLE,
            Material.BLACK_CANDLE,
            Material.SMALL_AMETHYST_BUD,
            Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD,
            Material.WATER,
            Material.LAVA,
            Material.TALL_SEAGRASS,
            Material.PISTON_HEAD,
            Material.MOVING_PISTON,
            Material.WALL_TORCH,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.REDSTONE_WIRE,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.REDSTONE_WALL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.NETHER_PORTAL,
            Material.ATTACHED_PUMPKIN_STEM,
            Material.ATTACHED_MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.END_PORTAL,
            Material.COCOA,
            Material.TRIPWIRE,
            Material.POTTED_OAK_SAPLING,
            Material.POTTED_SPRUCE_SAPLING,
            Material.POTTED_BIRCH_SAPLING,
            Material.POTTED_JUNGLE_SAPLING,
            Material.POTTED_ACACIA_SAPLING,
            Material.POTTED_DARK_OAK_SAPLING,
            Material.POTTED_FERN,
            Material.POTTED_DANDELION,
            Material.POTTED_POPPY,
            Material.POTTED_BLUE_ORCHID,
            Material.POTTED_ALLIUM,
            Material.POTTED_AZURE_BLUET,
            Material.POTTED_RED_TULIP,
            Material.POTTED_ORANGE_TULIP,
            Material.POTTED_WHITE_TULIP,
            Material.POTTED_PINK_TULIP,
            Material.POTTED_OXEYE_DAISY,
            Material.POTTED_LILY_OF_THE_VALLEY,
            Material.POTTED_WITHER_ROSE,
            Material.POTTED_RED_MUSHROOM,
            Material.POTTED_BROWN_MUSHROOM,
            Material.POTTED_DEAD_BUSH,
            Material.POTTED_CACTUS,
            Material.CARROTS,
            Material.POTATOES,
            Material.SKELETON_WALL_SKULL,
            Material.WITHER_SKELETON_WALL_SKULL,
            Material.ZOMBIE_WALL_HEAD,
            Material.PLAYER_WALL_HEAD,
            Material.CREEPER_WALL_HEAD,
            Material.DRAGON_WALL_HEAD,
            Material.WHITE_WALL_BANNER,
            Material.ORANGE_WALL_BANNER,
            Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER,
            Material.YELLOW_WALL_BANNER,
            Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER,
            Material.GRAY_WALL_BANNER,
            Material.LIGHT_GRAY_WALL_BANNER,
            Material.CYAN_WALL_BANNER,
            Material.PURPLE_WALL_BANNER,
            Material.BLUE_WALL_BANNER,
            Material.BROWN_WALL_BANNER,
            Material.GREEN_WALL_BANNER,
            Material.RED_WALL_BANNER,
            Material.BLACK_WALL_BANNER,
            Material.BEETROOTS,
            Material.KELP_PLANT,
            Material.DEAD_TUBE_CORAL_WALL_FAN,
            Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN,
            Material.DEAD_FIRE_CORAL_WALL_FAN,
            Material.DEAD_HORN_CORAL_WALL_FAN,
            Material.TUBE_CORAL_WALL_FAN,
            Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN,
            Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            Material.BAMBOO_SAPLING,
            Material.POTTED_BAMBOO,
            Material.VOID_AIR,
            Material.CAVE_AIR,
            Material.SWEET_BERRY_BUSH,
            Material.WEEPING_VINES_PLANT,
            Material.TWISTING_VINES_PLANT,
            Material.CRIMSON_WALL_SIGN,
            Material.WARPED_WALL_SIGN,
            Material.POTTED_CRIMSON_FUNGUS,
            Material.POTTED_WARPED_FUNGUS,
            Material.POTTED_CRIMSON_ROOTS,
            Material.POTTED_WARPED_ROOTS,
            Material.CANDLE_CAKE,
            Material.WHITE_CANDLE_CAKE,
            Material.ORANGE_CANDLE_CAKE,
            Material.MAGENTA_CANDLE_CAKE,
            Material.LIGHT_BLUE_CANDLE_CAKE,
            Material.YELLOW_CANDLE_CAKE,
            Material.LIME_CANDLE_CAKE,
            Material.PINK_CANDLE_CAKE,
            Material.GRAY_CANDLE_CAKE,
            Material.LIGHT_GRAY_CANDLE_CAKE,
            Material.CYAN_CANDLE_CAKE,
            Material.PURPLE_CANDLE_CAKE,
            Material.BLUE_CANDLE_CAKE,
            Material.BROWN_CANDLE_CAKE,
            Material.GREEN_CANDLE_CAKE,
            Material.RED_CANDLE_CAKE,
            Material.BLACK_CANDLE_CAKE,
            Material.POWDER_SNOW,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.BIG_DRIPLEAF_STEM,
            Material.POTTED_AZALEA_BUSH,
            Material.POTTED_FLOWERING_AZALEA_BUSH
        )
        private val carpetBlocks = arrayOf(
            Material.SNOW,
            Material.MOSS_CARPET,
            Material.WHITE_CARPET,
            Material.ORANGE_CARPET,
            Material.MAGENTA_CARPET,
            Material.YELLOW_CARPET,
            Material.LIME_CARPET,
            Material.PINK_CARPET,
            Material.GRAY_CARPET,
            Material.LIGHT_GRAY_CARPET,
            Material.CYAN_CARPET,
            Material.PURPLE_CARPET,
            Material.BLUE_CARPET,
            Material.BROWN_CARPET,
            Material.GREEN_CARPET,
            Material.RED_CARPET,
            Material.BLACK_CARPET
        )
    }

    /**
     * Unrender old partition for players who are currently visualising it
     */
    fun updateVisualisation(partition: Partition) {
        // Get the claim of the partition
        val claim = claimService.getById(partition.claimId) ?: return
        val claimWorld = claim.getWorld() ?: return

        // Get all players who are visualising and in range
        val players = getNearbyPlayers(claim, 1)

        // Get claim borders
        val borders: ArrayList<Position2D> = ArrayList()
        borders.addAll(partition.area.getEdgeBlockPositions())
        for (claimPartition in partitionService.getByClaim(claim)) {
            borders.addAll(claimPartition.area.getEdgeBlockPositions())
        }

        // Get all players
        for (player in players) {
            for (block in borders) {
                for (y in -64..320) {
                    val blockLocation = Location(claimWorld, block.x.toDouble(), y.toDouble(), block.z.toDouble())
                    val blockData = claimWorld.getBlockAt(blockLocation).blockData

                    player.sendBlockChange(blockLocation, blockData)
                }
            }
            val playerState = playerStateRepo.get(player) ?: continue
            if (playerState.claimToolMode == 0) {
                showVisualisation(player, true, false)
            }
            else {
                showVisualisation(player, true, true)
            }
        }
    }

    /**
     * Get all players within server render distance plus [radiusModifier] and return an array of them.
     */
    private fun getNearbyPlayers(loc: Location, radiusModifier: Int): Array<Player> {
        val players: ArrayList<Player> = ArrayList()
        val chunks = getSurroundingChunks(Position2D(loc).toChunk(), plugin.server.viewDistance+(radiusModifier shr 4))

        for (player in plugin.server.onlinePlayers) {
            if (player.location.world != loc.world) continue
            val playerState = playerStateRepo.get(player) ?: continue
            if (chunks.contains(Position2D(player.location).toChunk()) && playerState.isVisualisingClaims) {
                players.add(player)
            }
        }

        return players.toTypedArray()
    }

    private fun getNearbyPlayers(claim: Claim, radiusModifier: Int): Array<Player> {
        val players: ArrayList<Player> = ArrayList()

        // Get list of chunks that the claim occupies
        val startingChunks: MutableSet<Position2D> = mutableSetOf()
        val partitions = partitionService.getByClaim(claim)
        for (partition in partitions) {
            startingChunks.addAll(partition.getChunks())
        }

        // Get surrounding chunks
        val finalChunks: MutableSet<Position2D> = mutableSetOf()
        for (chunk in startingChunks) {
            finalChunks.addAll(getSurroundingChunks(chunk, 1))
        }

        // Get players in chunks
        val world = claim.getWorld()
        for (player in plugin.server.onlinePlayers) {
            if (player.location.world != world) continue
            val playerState = playerStateRepo.get(player) ?: continue
            if (finalChunks.contains(Position2D(player.location).toChunk()) && playerState.isVisualisingClaims) {
                players.add(player)
            }
        }
        return players.toTypedArray()
    }

    /**
     * Determine if [player] should be seeing the borders of nearby claims, and if so, send fake block data representing those bounds.
     */
    fun showVisualisation(player: Player, viewMode: Boolean = false, refresh: Boolean = false) {
        val playerState = playerStateRepo.get(player) ?: return
        playerState.isVisualisingClaims = !refresh

        if (refresh) {
            hideVisualisation(player)
        }

        // Change visualiser depending on view mode
        if (viewMode) {
            updatePartitionVisualisation(player)
        }
        else {
            updateClaimVisualisation(player)
        }

        updateOthersVisualisation(player)
        playerState.isVisualisingClaims = true
    }

    fun hideVisualisation(player: Player) {
        val playerState = playerStateRepo.get(player) ?: return
        playerState.isVisualisingClaims = false
        revertVisualisedBlocks(player)
    }

    fun updatePartitionVisualisation(player: Player) {
        val chunks = getSurroundingChunks(Position2D(player.location).toChunk(), plugin.server.viewDistance)
        val partitionsInChunks = ArrayList<Partition>()
        for (chunk in chunks) {
            partitionsInChunks.addAll(partitionService.getByChunk(player.world.uid, chunk))
        }
        if (partitionsInChunks.isEmpty()) return

        val mainBorders: ArrayList<Position2D> = ArrayList()
        val mainCorners: ArrayList<Position2D> = ArrayList()
        val borders: ArrayList<Position2D> = ArrayList()
        val corners: ArrayList<Position2D> = ArrayList()
        for (partition in partitionsInChunks) {
            val claim = claimService.getById(partition.claimId) ?: continue
            val mainPartition = partitionService.getPrimaryPartition(claim)
            if (claim.owner.uniqueId != player.uniqueId) {
                continue
            }

            if (mainPartition.id == partition.id) {
                mainCorners.addAll(partition.area.getCornerBlockPositions())
                mainBorders.addAll(partition.area.getEdgeBlockPositions())
                continue
            }
            corners.addAll(partition.area.getCornerBlockPositions())
            borders.addAll(partition.area.getEdgeBlockPositions())
        }
        borders.removeAll(corners.toSet())

        setVisualisedBlocks(player, mainBorders, Material.CYAN_GLAZED_TERRACOTTA, Material.CYAN_CARPET)
        setVisualisedBlocks(player, mainCorners, Material.BLUE_GLAZED_TERRACOTTA, Material.BLUE_CARPET)
        setVisualisedBlocks(player, borders, Material.LIGHT_GRAY_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
        setVisualisedBlocks(player, corners, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LIGHT_BLUE_CARPET)
    }

    fun updateClaimVisualisation(player: Player) {
        val chunks = getSurroundingChunks(Position2D(player.location).toChunk(), plugin.server.viewDistance)
        val partitionsInChunks = ArrayList<Partition>()
        for (chunk in chunks) {
            partitionsInChunks.addAll(partitionService.getByChunk(player.world.uid, chunk))
        }
        if (partitionsInChunks.isEmpty()) return

        // Get all claims that exist in selected chunks
        val foundClaims : MutableSet<Claim> = mutableSetOf()
        for (partition in partitionsInChunks) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (claim.owner.uniqueId != player.uniqueId) continue
            if (claim in foundClaims) continue
            foundClaims.add(claim)
        }

        // Get borders for each claim
        val finalBorders: ArrayList<Position2D> = arrayListOf()
        for (claim in foundClaims) {
            // Add edge blocks for each partition in claim
            val partitions = partitionService.getByClaim(claim)
            val borders: MutableList<Position2D> = mutableListOf()
            for (partition in partitions) {
                borders.addAll(partition.area.getEdgeBlockPositions())
            }

            // Get starting position by finding the position with the largest x coordinate.
            // Could be the largest or smallest any coordinate, this is personal choice.
            var startingPosition = borders[0]
            for (border in borders) {
                if (border.x > startingPosition.x) {
                    startingPosition = border
                }
            }

            // Get second position by getting block either in front or to the right in a clockwise direction
            var currentPosition = borders.firstOrNull { it.z == startingPosition.z + 1 && it.x == startingPosition.x }
                ?: borders.first { it.x == startingPosition.x - 1 && it.z == startingPosition.z }

            // Loop through edges by first checking left, then front, then right side. Traverse whichever is found first
            // until back to the starting position.
            val resultingBorder: ArrayList<Position2D> = arrayListOf()
            var previousPosition: Position2D = startingPosition
            do {
                val nextPosition: Position2D = when (getTravelDirection(previousPosition, currentPosition)) {
                    Direction.North -> {
                        borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                            ?: borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                            ?: borders.first { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                    }

                    Direction.East -> {
                        borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                            ?: borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                            ?: borders.first { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                    }

                    Direction.South -> {
                        borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                            ?: borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                            ?: borders.first { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                    }

                    else -> {
                        borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                            ?: borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                            ?: borders.first { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                    }
                }
                resultingBorder.add(nextPosition)
                previousPosition = currentPosition
                currentPosition = nextPosition
            } while (currentPosition != startingPosition)
            finalBorders.addAll(resultingBorder)
        }

        // Visualise created border
        setVisualisedBlocks(player, finalBorders, Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
    }

    private fun updateOthersVisualisation(player: Player) {
        val chunks = getSurroundingChunks(Position2D(player.location).toChunk(), plugin.server.viewDistance)
        val partitionsInChunks = ArrayList<Partition>()
        for (chunk in chunks) {
            partitionsInChunks.addAll(partitionService.getByChunk(player.world.uid, chunk))
        }
        if (partitionsInChunks.isEmpty()) return

        // Get all claims that exist in selected chunks
        val foundClaims : MutableSet<Claim> = mutableSetOf()
        for (partition in partitionsInChunks) {
            val claim = claimService.getById(partition.claimId) ?: continue
            if (claim.owner.uniqueId == player.uniqueId) continue
            if (claim in foundClaims) continue
            foundClaims.add(claim)
        }

        // Get borders for each claim
        val finalBorders: ArrayList<Position2D> = arrayListOf()
        for (claim in foundClaims) {
            // Add edge blocks for each partition in claim
            val partitions = partitionService.getByClaim(claim)
            val borders: MutableList<Position2D> = mutableListOf()
            for (partition in partitions) {
                borders.addAll(partition.area.getEdgeBlockPositions())
            }

            // Get starting position by finding the position with the largest x coordinate.
            // Could be the largest or smallest any coordinate, this is personal choice.
            var startingPosition = borders[0]
            for (border in borders) {
                if (border.x > startingPosition.x) {
                    startingPosition = border
                }
            }

            // Get second position by getting block either in front or to the right in a clockwise direction
            var currentPosition = borders.firstOrNull { it.z == startingPosition.z + 1 && it.x == startingPosition.x }
                ?: borders.first { it.x == startingPosition.x - 1 && it.z == startingPosition.z }

            // Loop through edges by first checking left, then front, then right side. Traverse whichever is found first
            // until back to the starting position.
            val resultingBorder: ArrayList<Position2D> = arrayListOf()
            var previousPosition: Position2D = startingPosition
            do {
                val nextPosition: Position2D = when (getTravelDirection(previousPosition, currentPosition)) {
                    Direction.North -> {
                        borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                            ?: borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                            ?: borders.first { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                    }

                    Direction.East -> {
                        borders.firstOrNull { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                            ?: borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                            ?: borders.first { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                    }

                    Direction.South -> {
                        borders.firstOrNull { it.x == currentPosition.x + 1 && it.z == currentPosition.z }
                            ?: borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                            ?: borders.first { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                    }

                    else -> {
                        borders.firstOrNull { it.z == currentPosition.z + 1 && it.x == currentPosition.x }
                            ?: borders.firstOrNull { it.x == currentPosition.x - 1 && it.z == currentPosition.z }
                            ?: borders.first { it.z == currentPosition.z - 1 && it.x == currentPosition.x }
                    }
                }
                resultingBorder.add(nextPosition)
                previousPosition = currentPosition
                currentPosition = nextPosition
            } while (currentPosition != startingPosition)
            finalBorders.addAll(resultingBorder)
        }

        // Visualise created border
        setVisualisedBlocks(player, finalBorders, Material.RED_GLAZED_TERRACOTTA, Material.LIGHT_GRAY_CARPET)
    }

    private fun getTravelDirection(first: Position2D, second: Position2D): Direction {
        return when {
            second.z > first.z -> Direction.South
            second.z < first.z -> Direction.North
            second.x > first.x -> Direction.East
            else -> Direction.West
        }
    }

    /**
     * Determine if a block is a floor/ceiling and therefore should be considered visible
     */
    private fun isBlockVisible(loc: Location): Boolean {
        val above = Location(loc.world, loc.x, loc.y+1, loc.z).block.blockData.material
        val below = Location(loc.world, loc.x, loc.y-1, loc.z).block.blockData.material
        return transparentMaterials.contains(above) || transparentMaterials.contains(below)
    }

    @EventHandler
    fun onHoldClaimTool(event: PlayerItemHeldEvent) {
       plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.player) }) // Run task after this tick
    }

    @EventHandler
    fun onPickupClaimTool(event: EntityPickupItemEvent) {
        if (event.entityType != EntityType.PLAYER) return
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.entity as Player) }) // Run task after this tick
    }

    @EventHandler
    fun onDropClaimTool(event: PlayerDropItemEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(event.player) }) // Run task after this tick
    }

    @EventHandler
    fun onClaimToolInventoryInteract(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        plugin.server.scheduler.runTask(plugin, Runnable { autoClaimToolVisualisation(player) }) // Run task after this tick
    }

    private fun autoClaimToolVisualisation(player: Player) {
        val mainItemMeta = player.inventory.itemInMainHand.itemMeta
        val offhandItemMeta = player.inventory.itemInOffHand.itemMeta
        val playerState = playerStateRepo.get(player) ?: return

        val holdingClaimTool = (mainItemMeta != null && mainItemMeta == getClaimTool().itemMeta) ||
                (offhandItemMeta != null && offhandItemMeta == getClaimTool().itemMeta)


        if (holdingClaimTool && playerState.isHoldingClaimTool) {
            return
        }

        hideVisualisation(player)
        if (holdingClaimTool) {
            if (playerState.claimToolMode == 0) {
                showVisualisation(player, false)
            }
            else {
                showVisualisation(player, true)
            }
        }

        playerState.isHoldingClaimTool = holdingClaimTool
    }

    /**
     * Get a square of chunks centering on [position2D] with a size of [radius]
     */
    @Suppress("SameParameterValue")
    private fun getSurroundingChunks(position2D: Position2D, radius: Int): Array<Position2D> {
        val sideLength = (radius * 2) + 1 // Make it always odd (eg. radius of 2 results in 5x5 square)
        val chunks = Array(sideLength * sideLength) { Position2D(0,0) }

        for (x in 0 until sideLength) {
            for (z in 0 until sideLength) {
                chunks[(x * sideLength) + z] = Position2D(position2D.x + x - radius, position2D.z + z - radius)
            }
        }

        return chunks
    }

    private fun setVisualisedBlocks(player: Player, position2DS: List<Position2D>, block: Material, flatBlock: Material) {
        val visualisedBlocks: MutableSet<Position3D> = mutableSetOf()
        val playerState = playerStateRepo.get(player) ?: return
        for (position in position2DS) {
            for (y in player.location.blockY + 1 .. player.location.blockY + 1 + upperRange) {
                var blockData = block.createBlockData() // Set the visualisation block
                val blockLocation = Location(player.location.world, position.x.toDouble(), y.toDouble(), position.z.toDouble()) // Get the location of the block being considered currently
                if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue // If the block is transparent, skip it
                if (!isBlockVisible(blockLocation)) continue // If the block isn't considered to be visible, skip it
                if (carpetBlocks.contains(blockLocation.block.blockData.material)) blockData = flatBlock.createBlockData()
                player.sendBlockChange(blockLocation, blockData) // Send the player block updates
                visualisedBlocks.add(Position3D(blockLocation))
                break
            }
            for (y in player.location.blockY downTo player.location.blockY - lowerRange) { // Get all blocks on claim borders within 25 blocks up and down from the player's current position
                var blockData = block.createBlockData() // Set the visualisation block
                val blockLocation = Location(player.location.world, position.x.toDouble(), y.toDouble(), position.z.toDouble()) // Get the location of the block being considered currently
                if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue // If the block is transparent, skip it
                if (!isBlockVisible(blockLocation)) continue // If the block isn't considered to be visible, skip it
                if (carpetBlocks.contains(blockLocation.block.blockData.material)) blockData = flatBlock.createBlockData()
                player.sendBlockChange(blockLocation, blockData) // Send the player block updates
                visualisedBlocks.add(Position3D(blockLocation))
                break
            }
        }
        playerState.visualisedBlockPositions.addAll(visualisedBlocks)
    }

    private fun revertVisualisedBlocks(player: Player) {
        val playerState = playerStateRepo.get(player) ?: return
        for (position in playerState.visualisedBlockPositions) {
            val blockData = player.world.getBlockAt(position.toLocation(player.world)).blockData
            player.sendBlockChange(position.toLocation(player.world), blockData)
        }
        playerState.visualisedBlockPositions.clear()
    }

    private fun setVisualisedBlock(player: Player, position2D: Position2D, block: Material, flatBlock: Material) {
        val visualisedBlocks: MutableSet<Position3D> = mutableSetOf()
        val playerState = playerStateRepo.get(player) ?: return
        for (y in player.location.blockY + 1 .. player.location.blockY + 1 + upperRange) { // Get all blocks on claim borders within 25 blocks up and down from the player's current position
            var blockData = block.createBlockData() // Set the visualisation block
            val blockLocation = Location(player.location.world, position2D.x.toDouble(), y.toDouble(), position2D.z.toDouble()) // Get the location of the block being considered currently
            if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue // If the block is transparent, skip it
            if (!isBlockVisible(blockLocation)) continue // If the block isn't considered to be visible, skip it
            if (carpetBlocks.contains(blockLocation.block.blockData.material)) blockData = flatBlock.createBlockData()
            player.sendBlockChange(blockLocation, blockData) // Send the player block updates
            visualisedBlocks.add(Position3D(blockLocation))
        }
        for (y in player.location.blockY downTo player.location.blockY - lowerRange) { // Get all blocks on claim borders within 25 blocks up and down from the player's current position
            var blockData = block.createBlockData() // Set the visualisation block
            val blockLocation = Location(player.location.world, position2D.x.toDouble(), y.toDouble(), position2D.z.toDouble()) // Get the location of the block being considered currently
            if (transparentMaterials.contains(blockLocation.block.blockData.material)) continue // If the block is transparent, skip it
            if (!isBlockVisible(blockLocation)) continue // If the block isn't considered to be visible, skip it
            if (carpetBlocks.contains(blockLocation.block.blockData.material)) blockData = flatBlock.createBlockData()
            player.sendBlockChange(blockLocation, blockData) // Send the player block updates
            visualisedBlocks.add(Position3D(blockLocation))
        }
        playerState.visualisedBlockPositions.addAll(visualisedBlocks)
    }
}