package dev.mizarc.bellclaims.infrastructure.services

import dev.mizarc.bellclaims.application.services.PlayerStateService
import dev.mizarc.bellclaims.application.enums.PlayerRegisterResult
import dev.mizarc.bellclaims.application.enums.PlayerUnregisterResult
import dev.mizarc.bellclaims.domain.players.PlayerState
import dev.mizarc.bellclaims.application.persistence.PlayerStateRepository
import io.mockk.*
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.util.*

class PlayerStateServiceImplTest {
    private lateinit var playerStateRepo: PlayerStateRepository
    private lateinit var playerStateService: PlayerStateService

    private lateinit var playerOne: OfflinePlayer
    private lateinit var playerTwo: OfflinePlayer
    private lateinit var playerThree: OfflinePlayer

    private lateinit var uuidOne: UUID
    private lateinit var uuidTwo: UUID
    private lateinit var uuidThree: UUID

    @BeforeEach
    fun setup() {
        playerStateRepo = mockk()
        playerStateService = PlayerStateServiceImpl(playerStateRepo)

        playerOne = mockk<OfflinePlayer>()
        uuidOne = UUID.fromString("22d7b7e7-7773-4f78-8e0b-817960fba37a")
        every { playerOne.uniqueId } returns uuidOne

        playerTwo = mockk<OfflinePlayer>()
        uuidTwo = UUID.fromString("edd513e2-7dd1-4f40-9fcd-4911c198b204")
        every { playerTwo.uniqueId } returns uuidTwo

        playerThree = mockk<OfflinePlayer>()
        uuidThree = UUID.fromString("6211420c-fa72-481a-a237-2e9f8eb8abe0")
        every { playerThree.uniqueId } returns uuidThree
    }

    @Test
    fun `getAllOnline - return PlayerState of all registered players`() {
        // Given
        val playerStates = setOf(PlayerState(playerOne), PlayerState(playerTwo), PlayerState(playerThree))
        every { playerStateRepo.getAll() } returns playerStates

        // When
        val result = playerStateService.getAllOnline()

        // Then
        assertEquals(result, playerStates)
    }

    @Test
    fun `getById - return PlayerState`() {
        // Given
        val playerState = PlayerState(playerOne)
        mockkStatic(Bukkit::class)
        every { playerStateRepo.get(playerOne.uniqueId) } returns playerState
        every { Bukkit.getOfflinePlayer(uuidOne) } returns playerOne

        // When
        val result = playerStateService.getById(playerOne.uniqueId)

        // Then
        assertEquals(playerState, result)
    }

    @Test
    fun `getByPlayer - return PlayerState`() {
        // Given
        val playerState = PlayerState(playerOne)
        every { playerStateRepo.get(playerOne.uniqueId) } returns playerState

        // When
        val result = playerStateService.getByPlayer(playerOne)

        // Then
        assertEquals(playerState, result)
    }

    @Test
    fun `registerPlayer - when player is already registered - return UNCHANGED`() {
        // Given
        val playerOneState = PlayerState(playerOne)
        val playerTwoState = PlayerState(playerTwo)
        every { playerOne.player } returns mockk()
        every { playerOne.player!!.uniqueId } returns uuidOne
        every { playerStateRepo.getAll() } returns setOf(playerOneState, playerTwoState)
        every { playerStateRepo.add(any()) } just runs

        // When
        val result = playerStateService.registerPlayer(playerOne.player!!)

        // Then
        assertEquals(PlayerRegisterResult.UNCHANGED, result)
        verify(exactly = 0) { playerStateRepo.add(any()) }
    }

    @Test
    fun `registerPlayer - when player isn't registered - return SUCCESS`() {
        // Given
        val playerTwoState = PlayerState(playerTwo)
        every { playerOne.player } returns mockk()
        every { playerOne.player!!.uniqueId } returns uuidOne
        every { playerStateRepo.getAll() } returns setOf(playerTwoState)
        every { playerStateRepo.add(any()) } just runs

        // When
        val result = playerStateService.registerPlayer(playerOne.player!!)

        // Then
        assertEquals(PlayerRegisterResult.SUCCESS, result)
        verify { playerStateRepo.add(any()) }
    }

    @Test
    fun `unregisterPlayer - when player isn't registered - return UNCHANGED`() {
        // Given
        every { playerOne.player } returns mockk()
        every { playerOne.player!!.uniqueId } returns uuidOne
        every { playerStateRepo.get(uuidTwo) } returns null
        every { playerStateRepo.add(any()) } just runs

        // When
        val result = playerStateService.unregisterPlayer(playerTwo)

        // Then
        assertEquals(PlayerUnregisterResult.UNCHANGED, result)
        verify(exactly = 0) { playerStateRepo.remove(any()) }
    }

    @Test
    fun `unregisterPlayer - when player is registered - return UNCHANGED`() {
        // Given
        val playerTwoState = PlayerState(playerTwo)
        every { playerOne.player } returns mockk()
        every { playerOne.player!!.uniqueId } returns uuidOne
        every { playerStateRepo.get(uuidTwo) } returns playerTwoState
        every { playerStateRepo.remove(any()) } just runs

        // When
        val result = playerStateService.unregisterPlayer(playerTwo)

        // Then
        assertEquals(PlayerUnregisterResult.SUCCESS, result)
        verify { playerStateRepo.remove(any()) }
    }
}