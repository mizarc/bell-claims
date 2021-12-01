package xyz.mizarc.solidclaims

import org.bukkit.Bukkit
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimPartition
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

    fun getAllClaims() : ArrayList<Claim>? {
        val claims: ArrayList<Claim> = arrayListOf()

        try {
            // Get all claims
            val sqlQuery = "SELECT * FROM claims;"
            val statement = connection.prepareStatement(sqlQuery)
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

                claims.add(Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    players
                ))
            }
            return claims

        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun getClaim(id: UUID): Claim? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE id=?;"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
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
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    players,
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    fun addClaim(worldId: UUID, ownerId: UUID) {
        val sqlQuery = "INSERT INTO claims (world, owner) VALUES (?, ?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, worldId.toString())
            statement.setString(2, ownerId.toString())
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

    fun getAllClaimPartitions(claims: ArrayList<Claim>) : ArrayList<ClaimPartition>? {
        val claimPartitions: ArrayList<ClaimPartition> = arrayListOf()
        for (claim in claims) {
            claimPartitions.addAll(getClaimPartitionsByClaim(claim))
        }

        return claimPartitions
    }

    fun getClaimPartitionsByClaim(claim: Claim) : ArrayList<ClaimPartition> {
        val sqlQuery = "SELECT * FROM claimPartitions WHERE claimId=?;"

        val claims : ArrayList<ClaimPartition> = arrayListOf()
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claim.id.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                claims.add(ClaimPartition(claim,
                    Pair(resultSet.getInt(2), resultSet.getInt(3)),
                    Pair(resultSet.getInt(4), resultSet.getInt(5))))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return claims
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

    fun getPlayerPermissions(playerId: UUID) : Player {
        val sqlQuery = "SELECT * FROM players WHERE playerId=?;"

        val player : Player = Player(playerId)
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                //player.permissions.add(ClaimPartition(resultSet.getString(4)))
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun addPlayerGlobalPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
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

    fun removePlayerGlobalPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
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

    fun addPlayerClaimPermission(playerId: UUID, claimId: UUID, permission: String) {
        val sqlQuery = "INSERT INTO players (playerId, claimId, permission) VALUES (?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
            statement.setString(3, permission)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    fun removePlayerClaimPermission(playerId: UUID, claimId: UUID, permission: String) {
        val sqlQuery = "DELETE FROM players WHERE playerId=? AND claimId=? AND permission=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
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
                "claimId TEXT, permission TEXT, FOREIGN KEY(claim) REFERENCES claims(id));"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}