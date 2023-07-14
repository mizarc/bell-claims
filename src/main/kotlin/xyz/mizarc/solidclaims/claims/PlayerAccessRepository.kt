package xyz.mizarc.solidclaims.claims

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import xyz.mizarc.solidclaims.storage.DatabaseStorage
import xyz.mizarc.solidclaims.listeners.ClaimPermission
import java.sql.SQLException
import java.util.*

class PlayerAccessRepository(private val storage: DatabaseStorage) {
    private val playerAccess: MutableMap<UUID, MutableMap<UUID, MutableSet<ClaimPermission>>> = mutableMapOf()

    init {
        createTable()
        preload()
    }

    fun doesPlayerHaveAccess(claim: Claim, player: OfflinePlayer): Boolean {
        return playerAccess[claim.id]?.contains(player.uniqueId) ?: false
    }

    fun doesPlayerHavePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission): Boolean {
        return playerAccess[claim.id]?.get(player.uniqueId)?.contains(permission) ?: false
    }

    fun getByClaim(claim: Claim): MutableMap<UUID, MutableSet<ClaimPermission>> {
        Bukkit.getLogger().info("${claim.id}")
        for (e in playerAccess) {
            Bukkit.getLogger().info("${e.key}")
        }
        return playerAccess[claim.id] ?: mutableMapOf()
    }

    fun getByPlayerInClaim(claim: Claim, player: OfflinePlayer): MutableSet<ClaimPermission> {
        return playerAccess[claim.id]?.get(player.uniqueId) ?: mutableSetOf()
    }

    fun add(claim: Claim, player: OfflinePlayer, permission: ClaimPermission) {
        playerAccess.getOrPut(claim.id) { mutableMapOf() }.getOrPut(player.uniqueId) { mutableSetOf() }.add(permission)
        try {
            storage.connection.executeUpdate("INSERT INTO playerAccess (claimId, playerId, permission) " +
                    "VALUES (?,?,?)", claim.id, player.uniqueId, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removePermission(claim: Claim, player: OfflinePlayer, permission: ClaimPermission) {
        val claimPermissions = playerAccess[claim.id] ?: return
        val playerPermissions = claimPermissions[player.uniqueId] ?: return
        playerPermissions.remove(permission)

        try {
            storage.connection.executeUpdate("REMOVE FROM playerAccess WHERE claimId=? AND playerId=? " +
                    "AND permission=?", claim.id, player.uniqueId, permission.name)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removePlayer(claim: Claim, player: OfflinePlayer) {
        val claimPermissions = playerAccess[claim.id] ?: return
        claimPermissions.remove(player.uniqueId)

        try {
            storage.connection.executeUpdate("REMOVE FROM playerAccess WHERE claimId=? AND playerId=?",
                claim.id, player.uniqueId)
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS playerAccess (claimId TEXT, " +
                    "playerId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Fetches all player access permissions from database and saves it to memory.
     */
    private fun preload() {
        val results = storage.connection.getResults("SELECT * FROM playerAccess")
        for (result in results) {
            val playerId = UUID.fromString(result.getString("playerId"))
            val claimId = UUID.fromString(result.getString("claimId"))
            val permission = ClaimPermission.valueOf(result.getString("permission"))
            val claimPlayers = playerAccess
                .getOrPut(claimId) { mutableMapOf(playerId to mutableSetOf()) }
            claimPlayers.getOrPut(playerId) { mutableSetOf() }.add(permission)
        }
    }
}