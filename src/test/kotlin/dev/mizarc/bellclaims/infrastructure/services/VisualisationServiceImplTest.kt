package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.PartitionService
import dev.mizarc.bellclaims.application.services.VisualisationService
import dev.mizarc.bellclaims.domain.claims.Claim
import dev.mizarc.bellclaims.domain.partitions.Area
import dev.mizarc.bellclaims.domain.partitions.Partition
import dev.mizarc.bellclaims.domain.partitions.Position2D
import dev.mizarc.bellclaims.domain.partitions.Position3D
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.Block
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class VisualisationServiceImplTest {
    private lateinit var partitionService: PartitionService
    private lateinit var visualisationService: VisualisationService

    private lateinit var player: OfflinePlayer
    private lateinit var claim: Claim
    private lateinit var partitionCollection: Array<Partition>

    @BeforeEach
    fun setup() {
        partitionService = mockk()
        visualisationService = VisualisationServiceImpl(partitionService)

        player = mockk<OfflinePlayer>()
        claim = Claim(UUID.randomUUID(), player, Position3D(15,85,10), "Test")
        partitionCollection = arrayOf(
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(8, 5), Position2D(19, 16))),
            Partition(UUID.randomUUID(), claim.id, Area(Position2D(16, 17), Position2D(25, 24))))
    }

    @Test
    fun `getOuterBorders - return Set of Position2D`() {
        // Given
        val blockPositions = arrayOf(
            Position2D(8, 5), // Start
            Position2D(9, 5),
            Position2D(10, 5),
            Position2D(11, 5),
            Position2D(12, 5),
            Position2D(13, 5),
            Position2D(14, 5),
            Position2D(15, 5),
            Position2D(16, 5),
            Position2D(17, 5),
            Position2D(18, 5),
            Position2D(19, 5), // Turn
            Position2D(19, 6),
            Position2D(19, 7),
            Position2D(19, 8),
            Position2D(19, 9),
            Position2D(19, 10),
            Position2D(19, 11),
            Position2D(19, 12),
            Position2D(19, 13),
            Position2D(19, 14),
            Position2D(19, 15),
            Position2D(19, 16),
            Position2D(19, 17), // Turn
            Position2D(20, 17),
            Position2D(21, 17),
            Position2D(22, 17),
            Position2D(23, 17),
            Position2D(24, 17),
            Position2D(25, 17),
            Position2D(25, 18),
            Position2D(25, 19),
            Position2D(25, 20),
            Position2D(25, 21),
            Position2D(25, 22),
            Position2D(25, 23),
            Position2D(25, 24), // Turn
            Position2D(24, 24),
            Position2D(23, 24),
            Position2D(22, 24),
            Position2D(21, 24),
            Position2D(20, 24),
            Position2D(19, 24),
            Position2D(18, 24),
            Position2D(17, 24),
            Position2D(16, 24), // Turn
            Position2D(16, 23),
            Position2D(16, 22),
            Position2D(16, 21),
            Position2D(16, 20),
            Position2D(16, 19),
            Position2D(16, 18),
            Position2D(16, 17),
            Position2D(16, 16), // Turn
            Position2D(15, 16),
            Position2D(14, 16),
            Position2D(13, 16),
            Position2D(12, 16),
            Position2D(11, 16),
            Position2D(10, 16),
            Position2D(9, 16),
            Position2D(8, 16), // Turn
            Position2D(8, 15),
            Position2D(8, 14),
            Position2D(8, 13),
            Position2D(8, 12),
            Position2D(8, 11),
            Position2D(8, 10),
            Position2D(8, 9),
            Position2D(8, 8),
            Position2D(8, 7),
            Position2D(8, 6)) // End
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()

        // When
        val result = visualisationService.getOuterBorders(claim)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }

    @Test
    fun `getPartitionedBorders - return Set of Position2D`() {
        // Given
        val blockPositions = mapOf(
            partitionCollection[0] to arrayOf(
            Position2D(8, 5), // Start Partition
            Position2D(9, 5),
            Position2D(10, 5),
            Position2D(11, 5),
            Position2D(12, 5),
            Position2D(13, 5),
            Position2D(14, 5),
            Position2D(15, 5),
            Position2D(16, 5),
            Position2D(17, 5),
            Position2D(18, 5),
            Position2D(19, 5), // Turn
            Position2D(19, 6),
            Position2D(19, 7),
            Position2D(19, 8),
            Position2D(19, 9),
            Position2D(19, 10),
            Position2D(19, 11),
            Position2D(19, 12),
            Position2D(19, 13),
            Position2D(19, 14),
            Position2D(19, 15),
            Position2D(19, 16), // Turn
            Position2D(18, 16),
            Position2D(17, 16),
            Position2D(16, 16),
            Position2D(15, 16),
            Position2D(14, 16),
            Position2D(13, 16),
            Position2D(12, 16),
            Position2D(11, 16),
            Position2D(10, 16),
            Position2D(9, 16),
            Position2D(8, 16), // Turn
            Position2D(8, 15),
            Position2D(8, 14),
            Position2D(8, 13),
            Position2D(8, 12),
            Position2D(8, 11),
            Position2D(8, 10),
            Position2D(8, 9),
            Position2D(8, 8),
            Position2D(8, 7),
            Position2D(8, 6)), // End Partition

            partitionCollection[1] to arrayOf(
            Position2D(16, 17), // Start Partition
            Position2D(17, 17),
            Position2D(18, 17),
            Position2D(19, 17),
            Position2D(20, 17),
            Position2D(21, 17),
            Position2D(22, 17),
            Position2D(23, 17),
            Position2D(24, 17),
            Position2D(25, 17), // Turn
            Position2D(25, 18),
            Position2D(25, 19),
            Position2D(25, 20),
            Position2D(25, 21),
            Position2D(25, 22),
            Position2D(25, 23),
            Position2D(25, 24), // Turn
            Position2D(24, 24),
            Position2D(23, 24),
            Position2D(22, 24),
            Position2D(21, 24),
            Position2D(20, 24),
            Position2D(19, 24),
            Position2D(18, 24),
            Position2D(17, 24),
            Position2D(16, 24), // Turn
            Position2D(16, 23),
            Position2D(16, 22),
            Position2D(16, 21),
            Position2D(16, 20),
            Position2D(16, 19),
            Position2D(16, 18))) // End Partition
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()

        // When
        val result = visualisationService.getPartitionedBorders(claim)


        // Then
        assertEquals(blockPositions.size, result.size)
        for (key in blockPositions.keys) {
            assertEquals(blockPositions[key]!!.size, result[key]!!.size)
            for (element in blockPositions[key]!!) {
                assertTrue(result[key]!!.contains(element))
            }
        }
    }

    @Test
    fun `getPartitionedCorners - return Set of Position2D`() {
        // Given
        val blockPositions = mapOf(
            partitionCollection[0] to arrayOf(
                Position2D(8, 5),
                Position2D(19, 5),
                Position2D(19, 16),
                Position2D(8, 16)),

            partitionCollection[1] to arrayOf(
                Position2D(16, 17),
                Position2D(25, 17),
                Position2D(25, 24),
                Position2D(16, 24)))
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()

        // When
        val result = visualisationService.getPartitionedCorners(claim)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (key in blockPositions.keys) {
            assertEquals(blockPositions[key]!!.size, result[key]!!.size)
            for (element in blockPositions[key]!!) {
                assertTrue(result[key]!!.contains(element))
            }
        }
    }

    @Test
    fun `getMainPartitionBorders - return Set of Position2D`() {
        // Given
        val blockPositions = arrayOf(
            Position2D(8, 5), // Start
            Position2D(9, 5),
            Position2D(10, 5),
            Position2D(11, 5),
            Position2D(12, 5),
            Position2D(13, 5),
            Position2D(14, 5),
            Position2D(15, 5),
            Position2D(16, 5),
            Position2D(17, 5),
            Position2D(18, 5),
            Position2D(19, 5), // Turn
            Position2D(19, 6),
            Position2D(19, 7),
            Position2D(19, 8),
            Position2D(19, 9),
            Position2D(19, 10),
            Position2D(19, 11),
            Position2D(19, 12),
            Position2D(19, 13),
            Position2D(19, 14),
            Position2D(19, 15),
            Position2D(19, 16), // Turn
            Position2D(18, 16),
            Position2D(17, 16),
            Position2D(16, 16),
            Position2D(15, 16),
            Position2D(14, 16),
            Position2D(13, 16),
            Position2D(12, 16),
            Position2D(11, 16),
            Position2D(10, 16),
            Position2D(9, 16),
            Position2D(8, 16), // Turn
            Position2D(8, 15),
            Position2D(8, 14),
            Position2D(8, 13),
            Position2D(8, 12),
            Position2D(8, 11),
            Position2D(8, 10),
            Position2D(8, 9),
            Position2D(8, 8),
            Position2D(8, 7),
            Position2D(8, 6)) // End
        every { partitionService.getPrimary(claim) } returns partitionCollection[0]

        // When
        val result = visualisationService.getMainPartitionBorders(claim)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }

    @Test
    fun `getMainPartitionCorners - return Set of Position2D`() {
        // Given
        val blockPositions = arrayOf(
            Position2D(8, 5),
            Position2D(19, 5),
            Position2D(19, 16),
            Position2D(8, 16))
        every { partitionService.getPrimary(claim) } returns partitionCollection[0]

        // When
        val result = visualisationService.getMainPartitionCorners(claim)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }

    @Test
    fun `get3DOuterBorders - return Set of Position3D`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 16.0, 77.0, 16.0)
        val blockPositions = arrayOf(
            Position3D(8, 67, 5), // Start
            Position3D(9, 67, 5),
            Position3D(10, 67, 5),
            Position3D(11, 67, 5),
            Position3D(12, 68, 5),
            Position3D(13, 68, 5),
            Position3D(14, 68, 5),
            Position3D(15, 68, 5),
            Position3D(16, 69, 5),
            Position3D(17, 69, 5),
            Position3D(18, 70, 5),
            Position3D(19, 70, 5), // Turn
            Position3D(19, 70, 6),
            Position3D(19, 70, 7),
            Position3D(19, 70, 8),
            Position3D(19, 70, 9),
            Position3D(19, 70, 10),
            Position3D(19, 71, 11),
            Position3D(19, 71, 12),
            Position3D(19, 71, 13),
            Position3D(19, 71, 14),
            Position3D(19, 71, 15),
            Position3D(19, 71, 16), // Turn
            Position3D(19, 71, 17),
            Position3D(20, 71, 17),
            Position3D(21, 71, 17),
            Position3D(22, 71, 17),
            Position3D(23, 71, 17),
            Position3D(24, 71, 17),
            Position3D(25, 71, 17),
            Position3D(25, 72, 18),
            Position3D(25, 73, 19),
            Position3D(25, 73, 20),
            Position3D(25, 74, 21),
            Position3D(25, 74, 22),
            Position3D(25, 73, 23),
            Position3D(25, 72, 24), // Turn
            Position3D(24, 73, 24),
            Position3D(23, 73, 24),
            Position3D(22, 74, 24),
            Position3D(21, 74, 24),
            Position3D(20, 73, 24),
            Position3D(19, 73, 24),
            Position3D(18, 72, 24),
            Position3D(17, 72, 24),
            Position3D(16, 71, 24), // Turn
            Position3D(16, 71, 23),
            Position3D(16, 71, 22),
            Position3D(16, 71, 21),
            Position3D(16, 71, 20),
            Position3D(16, 71, 19),
            Position3D(16, 71, 18),
            Position3D(16, 71, 17),
            Position3D(16, 71, 16), // Turn
            Position3D(15, 71, 16),
            Position3D(14, 71, 16),
            Position3D(13, 71, 16),
            Position3D(12, 70, 16),
            Position3D(11, 70, 16),
            Position3D(10, 70, 16),
            Position3D(9, 70, 16),
            Position3D(8, 69, 16), // Turn
            Position3D(8, 68, 15),
            Position3D(8, 67, 14),
            Position3D(8, 67, 13),
            Position3D(8, 67, 12),
            Position3D(8, 67, 11),
            Position3D(8, 67, 10),
            Position3D(8, 67, 9),
            Position3D(8, 67, 8),
            Position3D(8, 67, 7),
            Position3D(8, 67, 6)) // End
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()
        val airBlock = mockk<Block>()
        every { airBlock.blockData.material } returns Material.AIR
        every { world.getBlockAt(any()) } returns airBlock
        for (position in blockPositions) {
            val block = mockk<Block>()
            every { block.blockData.material } returns Material.DIRT
            every { world.getBlockAt(
                Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())) } returns block
        }

        // When
        val result = visualisationService.get3DOuterBorders(claim, location)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }

    @Test
    fun `get3DPartitionedBorders - return Set of Position3D`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 16.0, 77.0, 16.0)
        val blockPositions = mapOf(
            partitionCollection[0] to arrayOf(
                Position3D(8, 67, 5), // Start Partition
                Position3D(9, 67, 5),
                Position3D(10, 67, 5),
                Position3D(11, 67, 5),
                Position3D(12, 68, 5),
                Position3D(13, 68, 5),
                Position3D(14, 68, 5),
                Position3D(15, 68, 5),
                Position3D(16, 69, 5),
                Position3D(17, 69, 5),
                Position3D(18, 70, 5),
                Position3D(19, 70, 5), // Turn
                Position3D(19, 70, 6),
                Position3D(19, 70, 7),
                Position3D(19, 70, 8),
                Position3D(19, 70, 9),
                Position3D(19, 70, 10),
                Position3D(19, 71, 11),
                Position3D(19, 71, 12),
                Position3D(19, 71, 13),
                Position3D(19, 71, 14),
                Position3D(19, 71, 15),
                Position3D(19, 71, 16), // Turn
                Position3D(18, 71, 16),
                Position3D(17, 71, 16),
                Position3D(16, 71, 16),
                Position3D(15, 71, 16),
                Position3D(14, 71, 16),
                Position3D(13, 71, 16),
                Position3D(12, 70, 16),
                Position3D(11, 70, 16),
                Position3D(10, 70, 16),
                Position3D(9, 70, 16),
                Position3D(8, 69, 16), // Turn
                Position3D(8, 68, 15),
                Position3D(8, 67, 14),
                Position3D(8, 67, 13),
                Position3D(8, 67, 12),
                Position3D(8, 67, 11),
                Position3D(8, 67, 10),
                Position3D(8, 67, 9),
                Position3D(8, 67, 8),
                Position3D(8, 67, 7),
                Position3D(8, 67, 6)), // End Partition

            partitionCollection[1] to arrayOf(
                Position3D(16, 71, 17), // Start Partition
                Position3D(17, 71, 17),
                Position3D(18, 71, 17),
                Position3D(19, 71, 17),
                Position3D(20, 71, 17),
                Position3D(21, 71, 17),
                Position3D(22, 71, 17),
                Position3D(23, 71, 17),
                Position3D(24, 71, 17),
                Position3D(25, 71, 17), // Turn
                Position3D(25, 72, 18),
                Position3D(25, 73, 19),
                Position3D(25, 73, 20),
                Position3D(25, 74, 21),
                Position3D(25, 74, 22),
                Position3D(25, 73, 23),
                Position3D(25, 72, 24), // Turn
                Position3D(24, 73, 24),
                Position3D(23, 73, 24),
                Position3D(22, 74, 24),
                Position3D(21, 74, 24),
                Position3D(20, 73, 24),
                Position3D(19, 73, 24),
                Position3D(18, 72, 24),
                Position3D(17, 72, 24),
                Position3D(16, 71, 24), // Turn
                Position3D(16, 71, 23),
                Position3D(16, 71, 22),
                Position3D(16, 71, 21),
                Position3D(16, 71, 20),
                Position3D(16, 71, 19),
                Position3D(16, 71, 18))) // End Partition
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()
        val airBlock = mockk<Block>()
        every { airBlock.blockData.material } returns Material.AIR
        every { world.getBlockAt(any()) } returns airBlock
        for (partition in blockPositions) {
            for (position in partition.value) {
                val block = mockk<Block>()
                every { block.blockData.material } returns Material.DIRT
                every { world.getBlockAt(
                    Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())) } returns block
            }
        }

        // When
        val result = visualisationService.get3DPartitionedBorders(claim, location)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (key in blockPositions.keys) {
            assertEquals(blockPositions[key]!!.size, result[key]!!.size)
            for (element in blockPositions[key]!!) {
                assertTrue(result[key]!!.contains(element))
            }
        }
    }

    @Test
    fun `get3DPartitionedCorners - return Set of Position3D`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 16.0, 77.0, 16.0)
        val blockPositions = mapOf(
            partitionCollection[0] to arrayOf(
                Position3D(8, 67, 5),
                Position3D(19, 70, 5),
                Position3D(19, 71, 16),
                Position3D(8, 69, 16)),

            partitionCollection[1] to arrayOf(
                Position3D(16, 71, 17),
                Position3D(25, 71, 17),
                Position3D(25, 72, 24),
                Position3D(16, 71, 24)))
        every { partitionService.getByClaim(claim) } returns partitionCollection.toSet()
        val airBlock = mockk<Block>()
        every { airBlock.blockData.material } returns Material.AIR
        every { world.getBlockAt(any()) } returns airBlock
        for (partition in blockPositions) {
            for (position in partition.value) {
                val block = mockk<Block>()
                every { block.blockData.material } returns Material.DIRT
                every { world.getBlockAt(
                    Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())) } returns block
            }
        }

        // When
        val result = visualisationService.get3DPartitionedCorners(claim, location)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (key in blockPositions.keys) {
            assertEquals(blockPositions[key]!!.size, result[key]!!.size)
            for (element in blockPositions[key]!!) {
                assertTrue(result[key]!!.contains(element))
            }
        }
    }

    @Test
    fun `get3DMainPartitionBorders - return Set of Position3D`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 16.0, 77.0, 16.0)
        val blockPositions = arrayOf(
            Position3D(8, 67, 5), // Start Partition
            Position3D(9, 67, 5),
            Position3D(10, 67, 5),
            Position3D(11, 67, 5),
            Position3D(12, 68, 5),
            Position3D(13, 68, 5),
            Position3D(14, 68, 5),
            Position3D(15, 68, 5),
            Position3D(16, 69, 5),
            Position3D(17, 69, 5),
            Position3D(18, 70, 5),
            Position3D(19, 70, 5), // Turn
            Position3D(19, 70, 6),
            Position3D(19, 70, 7),
            Position3D(19, 70, 8),
            Position3D(19, 70, 9),
            Position3D(19, 70, 10),
            Position3D(19, 71, 11),
            Position3D(19, 71, 12),
            Position3D(19, 71, 13),
            Position3D(19, 71, 14),
            Position3D(19, 71, 15),
            Position3D(19, 71, 16), // Turn
            Position3D(18, 71, 16),
            Position3D(17, 71, 16),
            Position3D(16, 71, 16),
            Position3D(15, 71, 16),
            Position3D(14, 71, 16),
            Position3D(13, 71, 16),
            Position3D(12, 70, 16),
            Position3D(11, 70, 16),
            Position3D(10, 70, 16),
            Position3D(9, 70, 16),
            Position3D(8, 69, 16), // Turn
            Position3D(8, 68, 15),
            Position3D(8, 67, 14),
            Position3D(8, 67, 13),
            Position3D(8, 67, 12),
            Position3D(8, 67, 11),
            Position3D(8, 67, 10),
            Position3D(8, 67, 9),
            Position3D(8, 67, 8),
            Position3D(8, 67, 7),
            Position3D(8, 67, 6)) // End Partition
        every { partitionService.getPrimary(claim) } returns partitionCollection[0]
        val airBlock = mockk<Block>()
        every { airBlock.blockData.material } returns Material.AIR
        every { world.getBlockAt(any()) } returns airBlock
        for (position in blockPositions) {
            val block = mockk<Block>()
            every { block.blockData.material } returns Material.DIRT
            every { world.getBlockAt(
                Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())) } returns block
        }

        // When
        val result = visualisationService.get3DMainPartitionBorders(claim, location)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }

    @Test
    fun `get3DMainPartitionCorners - return Set of Position3D`() {
        // Given
        val world = mockk<World>()
        val location = Location(world, 16.0, 77.0, 16.0)
        val blockPositions = arrayOf(
                Position3D(8, 67, 5),
                Position3D(19, 70, 5),
                Position3D(19, 71, 16),
                Position3D(8, 69, 16))
        every { partitionService.getPrimary(claim) } returns partitionCollection[0]
        val airBlock = mockk<Block>()
        every { airBlock.blockData.material } returns Material.AIR
        every { world.getBlockAt(any()) } returns airBlock
        for (position in blockPositions) {
            val block = mockk<Block>()
            every { block.blockData.material } returns Material.DIRT
            every { world.getBlockAt(
                Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())) } returns block
        }

        // When
        val result = visualisationService.get3DMainPartitionCorners(claim, location)

        // Then
        assertEquals(blockPositions.size, result.size)
        for (element in blockPositions) {
            assertTrue(result.contains(element))
        }
    }
}