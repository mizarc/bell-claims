package xyz.mizarc.solidclaims

import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.Player
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

class DatabaseStorage(var plugin: SolidClaims) {
    private lateinit var connection: Connection

    fun openConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:sqlite:" + plugin.dataFolder.toString() + "/claims.db"
            )
            createClaimTable()
            createClaimPartitionTable()
            createPlayerTable()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun closeConnection() {
        try {
            connection.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun getClaim(id: Int): Claim? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE id=?;"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, id)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {

                // Get all players trusted in claim
                val sqlPlayerQuery = "SELECT * FROM players WHERE id=?;"
                val playerStatement = connection.prepareStatement(sqlPlayerQuery)
                playerStatement.setInt(1, resultSet.getInt(1))
                val playerResultSet = statement.executeQuery()
                val players: ArrayList<Player> = ArrayList()
                while (playerResultSet.next()) {
                    players.add(Player(UUID.fromString(playerResultSet.getString(1))))
                }

                return Claim(
                    resultSet.getInt(1),
                    UUID.fromString(resultSet.getString(2)),
                    UUID.fromString(resultSet.getString(3)),
                    players,
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun addClaim(world: UUID, owner: UUID) {
        val sqlQuery = "INSERT INTO claims (owner) VALUES (?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, owner.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removeClaim(id: UUID) {
        val sqlQuery = "DELETE FROM claims WHERE id=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun addClaimPartition(id: UUID, firstLocationX: Int, firstLocationZ: Int,
                          secondLocationX: Int, secondLocationZ: Int) {
        val sqlQuery = "INSERT INTO claimPartitions (claimId, firstLocationX, firstLocationZ, " +
                "secondLocationX, secondLocationZ) VALUES (?,?,?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            statement.setInt(2, firstLocationX)
            statement.setInt(3, firstLocationZ)
            statement.setInt(4, secondLocationX)
            statement.setInt(5, secondLocationZ)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removeClaimPartition(firstLocationX: Int, firstLocationZ: Int,
                          secondLocationX: Int, secondLocationZ: Int) {
        val sqlQuery = "DELETE FROM claimPartitions WHERE firstLocationX=? AND firstLocationZ=? AND " +
                "secondLocationX=? AND secondLocationZ=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, firstLocationX)
            statement.setInt(2, firstLocationZ)
            statement.setInt(3, secondLocationX)
            statement.setInt(4, secondLocationZ)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun addGlobalPlayerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
        val sqlQuery = "INSERT INTO players (playerId, claimOwnerId, permission) VALUES (?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimOwnerId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removeGlobalPlayerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
        val sqlQuery = "DELETE FROM players WHERE playerId=? AND claimOwnerId=? AND permission=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimOwnerId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }


    private fun createClaimTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claims (id TEXT PRIMARY KEY, " +
                "owner TEXT NOT NULL);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    private fun createClaimPartitionTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claimPartitions (claimId TEXT, firstLocationX INTEGER NOT NULL," +
                "firstLocationZ INTEGER NOT NULL, secondLocationX INTEGER NOT NULL, secondLocationZ INTEGER NOT NULL);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    private fun createPlayerTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS players (playerId TEXT, claimOwnerId TEXT, " +
                "claimId INTEGER, permission TEXT, FOREIGN KEY(claim) REFERENCES claims(id));"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}