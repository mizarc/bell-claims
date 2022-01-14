package xyz.mizarc.solidclaims.claims

import xyz.mizarc.solidclaims.DatabaseStorage
import xyz.mizarc.solidclaims.Mapper
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class PlayerAccessMapper(private val storage: DatabaseStorage): Mapper<PlayerAccess> {
    init {
        createTable()
    }

    fun getByClaim(claimId: UUID): ArrayList<PlayerAccess> {
        val playerAccesses = ArrayList<PlayerAccess>()
        try {
            val results = storage.connection.getResults("SELECT * FROM claims WHERE claimId=?;", claimId)
            for (result in results) {
                playerAccesses.add(PlayerAccess(UUID.fromString(result.getString("claimId")),
                    UUID.fromString(result.getString("playerId")), ClaimPermission.valueOf("permission")))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return playerAccesses
    }

    fun getByPlayer(claimId: UUID, playerId: UUID): ArrayList<PlayerAccess> {
        val playerAccesses = ArrayList<PlayerAccess>()
        try {
            val results = storage.connection.getResults("SELECT * FROM claims " +
                    "WHERE playerId=?;", claimId, playerId)
            for (result in results) {
                playerAccesses.add(PlayerAccess(UUID.fromString(result.getString("claimId")),
                    UUID.fromString(result.getString("playerId")), ClaimPermission.valueOf("permission")))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
        return playerAccesses
    }

    override fun add(entity: PlayerAccess) {
        try {
            storage.connection.executeUpdate("INSERT INTO playerAccess VALUES (playerId, claimId, permissionId)" +
                    "VALUES (?,?,?)")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    override fun remove(entity: PlayerAccess) {
        try {
            storage.connection.executeUpdate("REMOVE FROM playerAccess WHERE playerId=? " +
                    "AND claimId=? AND permissionId=?")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createTable() {
        try {
            storage.connection.executeUpdate("CREATE TABLE IF NOT EXISTS playerAccess (playerId TEXT, " +
                    "claimId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));")
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}