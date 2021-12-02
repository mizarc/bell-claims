package xyz.mizarc.solidclaims

import org.bukkit.Bukkit
import xyz.mizarc.solidclaims.claims.Claim
import xyz.mizarc.solidclaims.claims.ClaimPartition
import xyz.mizarc.solidclaims.claims.ClaimPlayer
import xyz.mizarc.solidclaims.events.ClaimPermission
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

/**
 * The gateway to the persistent SQLite based database to hold claim data.
 * @property plugin A reference to the main plugin instance.
 */
class DatabaseStorage(var plugin: SolidClaims) {
    private lateinit var connection: Connection

    /**
     * Creates a connection to the claims database.
     */
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

    /**
     * Closes the connection to the claims database if opened.
     */
    fun closeConnection() {
        try {
            connection.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets a specific claim from the database if available.
     * @param id The unique identifier for the claim.
     * @return The claim object linked to the id. May return null.
     */
    fun getClaim(id: UUID): Claim? {
        try {
            // Get specified claim
            val sqlQuery = "SELECT * FROM claims WHERE id=?;"
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val claimPermissions = getClaimPermissions(id) ?: return null
                val claimPlayers: ArrayList<ClaimPlayer> =
                    getAllPlayersClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: return null

                return Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(3))),
                    claimPermissions,
                    claimPlayers,
                )
            }
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets a list of every claim in the database.
     * @return Array of claim objects. May return null.
     */
    fun getAllClaims() : ArrayList<Claim>? {
        val claims: ArrayList<Claim> = arrayListOf()

        try {
            // Get all claims
            val sqlQuery = "SELECT * FROM claims;"
            val statement = connection.prepareStatement(sqlQuery)
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                val claimPermissions: ArrayList<ClaimPermission> =
                    getClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: return null
                val claimPlayers: ArrayList<ClaimPlayer> =
                    getAllPlayersClaimPermissions(UUID.fromString(resultSet.getString(1))) ?: return null

                claims.add(Claim(
                    UUID.fromString(resultSet.getString(1)),
                    UUID.fromString(resultSet.getString(2)),
                    Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString(4))),
                    claimPermissions,
                    claimPlayers
                ))
            }
            return claims

        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a new claim instance to the database.
     * @param worldId The unique identifier for the world.
     * @param ownerId The unique identifier for the player.
     */
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

    /**
     * Removes a claim instance from the database.
     * @param id The unique identifier for the claim.
     */
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

    /**
     * Gets a list of all claim partitions associated with a list of claims.
     * @param claims The claims to read from.
     * @return An array of claim partition objects. May return null.
     */
    fun getAllClaimPartitions(claims: ArrayList<Claim>) : ArrayList<ClaimPartition>? {
        val claimPartitions: ArrayList<ClaimPartition> = arrayListOf()
        for (claim in claims) {
            val claimPartition = getClaimPartitionsByClaim(claim) ?: return null
            claimPartitions.addAll(claimPartition)
        }

        return claimPartitions
    }

    /**
     * Gets a list of claim partitions associated with a claim.
     * @param claim The claim to read from.
     * @return An array of claim partition objects. May return null.
     */
    fun getClaimPartitionsByClaim(claim: Claim) : ArrayList<ClaimPartition>? {
        val sqlQuery = "SELECT * FROM claimPartitions WHERE claimId=?;"

        try {
            val claims : ArrayList<ClaimPartition> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claim.id.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                claims.add(ClaimPartition(claim,
                    Pair(resultSet.getInt(2), resultSet.getInt(3)),
                    Pair(resultSet.getInt(4), resultSet.getInt(5))))
            }

            return claims
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a new claim partition to the database.
     * @param id The unique identifier for the claim to be associated with.
     * @param firstLocation The integer pair defining the first location.
     * @param secondLocation The integer pair defining the second location.
     */
    fun addClaimPartition(id: UUID, firstLocation: Pair<Int, Int>, secondLocation: Pair<Int, Int>) {
        val sqlQuery = "INSERT INTO claimPartitions (claimId, firstLocationX, firstLocationZ, " +
                "secondLocationX, secondLocationZ) VALUES (?,?,?,?,?);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, id.toString())
            statement.setInt(2, firstLocation.first)
            statement.setInt(3, firstLocation.second)
            statement.setInt(4, secondLocation.first)
            statement.setInt(5, secondLocation.second)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a claim partition based on the claim id and partition positions.
     * @param id The unique identifier for the claim to be associated with.
     * @param firstLocation The integer pair defining the first location.
     * @param secondLocation The integer pair defining the second location.
     */
    fun removeClaimPartition(firstLocation: Pair<Int, Int>, secondLocation: Pair<Int, Int>) {
        val sqlQuery = "DELETE FROM claimPartitions WHERE firstLocationX=? AND firstLocationZ=? AND " +
                "secondLocationX=? AND secondLocationZ=?;"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setInt(1, firstLocation.first)
            statement.setInt(2, firstLocation.second)
            statement.setInt(3, secondLocation.first)
            statement.setInt(4, secondLocation.second)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets the default permissions associated with a claim.
     * @param claimID The unique identifier of the claim.
     * @return An array of ClaimPermissions. May return null.
     */
    fun getClaimPermissions(claimID: UUID) : ArrayList<ClaimPermission>? {
        val sqlQuery = "SELECT * FROM claimPermissions WHERE claimId=?;"

        try {
            val claimPermissions: ArrayList<ClaimPermission> = ArrayList()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimID.toString())
            val permissionResultSet = statement.executeQuery()

            while (permissionResultSet.next()) {
                claimPermissions.add(ClaimPermission.valueOf(permissionResultSet.getString(1)))
            }

            return claimPermissions
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Adds a permission entry to a specific claim from the database.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission enum to assign.
     */
    fun addClaimPermission(claimId: UUID, permission: ClaimPermission) {
        val sqlQuery = "INSERT INTO claimPermissions (claimId, permission) VALUES (?,?);"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimId.toString())
            statement.setString(2, permission.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Removes a permission entry for a specific claim from the database.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission enum to remove.
     */
    fun removeClaimPermission(claimId: UUID, permission: ClaimPermission) {
        val sqlQuery = "DELETE FROM claimPermissions WHERE claimId=? AND permission=?;"

        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimId.toString())
            statement.setString(2, permission.toString())
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Gets all player permission associated with a particular claim owner.
     * @param ownerID The unique identifier for the claim owner.
     * @return An array of player claim permissions. May return null.
     */
    fun getAllPlayersOwnerPermissions(ownerID: UUID) : ArrayList<ClaimPlayer>? {
        val sqlQuery = "SELECT * FROM players WHERE claimOwnerId=?;"

        try {
            val claimPlayers: ArrayList<ClaimPlayer> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, ownerID.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                var foundExistingPlayer = false

                // Add to existing player entry if found
                for (claimPlayer in claimPlayers) {
                    if (UUID.fromString(resultSet.getString(1)) == claimPlayer.id) {
                        claimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                        foundExistingPlayer = true
                    }
                }

                // Skip next step if existing player has already been added
                if (foundExistingPlayer) {
                    continue
                }

                // Add new ClaimPlayer entry to list if it doesn't already exist
                val newClaimPlayer = ClaimPlayer(UUID.fromString(resultSet.getString(1)))
                newClaimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                claimPlayers.add(newClaimPlayer)
            }

            return claimPlayers
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all player permission associated with a particular claim.
     * @param claimID The unique identifier for the claim.
     * @return An array of player claim permissions. May return null.
     */
    fun getAllPlayersClaimPermissions(claimID: UUID) : ArrayList<ClaimPlayer>? {
        val sqlQuery = "SELECT * FROM players WHERE claimId=?;"

        try {
            val claimPlayers: ArrayList<ClaimPlayer> = arrayListOf()
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, claimID.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                var foundExistingPlayer = false

                // Add to existing player entry if found
                for (claimPlayer in claimPlayers) {
                    if (UUID.fromString(resultSet.getString(1)) == claimPlayer.id) {
                        claimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                        foundExistingPlayer = true
                    }
                }

                // Skip next step if existing player has already been added
                if (foundExistingPlayer) {
                    continue
                }

                // Add new ClaimPlayer entry to list if it doesn't already exist
                val newClaimPlayer = ClaimPlayer(UUID.fromString(resultSet.getString(1)))
                newClaimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
                claimPlayers.add(newClaimPlayer)
            }

            return claimPlayers
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all of a player's permissions for a specified claim owner.
     * @param playerId The unique identifier for the player.
     * @param ownerId The unique identifier for a claim owner.
     * @return A ClaimPlayer object. May return null.
     */
    fun getPlayerOwnerPermissions(playerId: UUID, ownerId: UUID) : ClaimPlayer? {
        val sqlQuery = "SELECT * FROM players WHERE playerId=? AND claimOwnerId=?;"

        try {
            val claimPlayer = ClaimPlayer(playerId)
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, ownerId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                claimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
            }

            return claimPlayer
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Gets all of a player's permissions for a specified claim.
     * @param playerId The unique identifier for the player.
     * @param claimId The unique identifier for the claim.
     * @return A ClaimPlayer object. May Return Null.
     */
    fun getPlayerClaimPermissions(playerId: UUID, claimId: UUID) : ClaimPlayer? {
        val sqlQuery = "SELECT * FROM players WHERE playerId=? AND claimId=?;"

        try {
            val claimPlayer = ClaimPlayer(playerId)
            val statement = connection.prepareStatement(sqlQuery)
            statement.setString(1, playerId.toString())
            statement.setString(2, claimId.toString())
            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                claimPlayer.claimPermissions.add(ClaimPermission.valueOf(resultSet.getString(4)))
            }

            return claimPlayer
        } catch (error: SQLException) {
            error.printStackTrace()
        }

        return null
    }

    /**
     * Assigns a player a permission for all of a claim owner's claims.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimOwnerId The unique identifier for the claim owner.
     * @param permission The permission key name.
     */
    fun addPlayerOwnerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
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

    /**
     * Removes a player's permissions from all of a claim owner's claims.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimOwnerId The unique identifier for the claim owner.
     * @param permission The permission key name.
     */
    fun removePlayerOwnerPermission(playerId: UUID, claimOwnerId: UUID, permission: String) {
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

    /**
     * Assigns a player a permission for a specific claim.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission key name.
     */
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

    /**
     * Removes a player's permissions from a specfic claim.
     * @param playerId The unique identifier for the player to give a permission to.
     * @param claimId The unique identifier for the claim.
     * @param permission The permission key name.
     */
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

    /**
     * Creates a new table to store claim data if it doesn't exist.
     */
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

    /**
     * Creates a new table to store claim partition data if it doesn't exist.
     */
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

    /**
     * Creates a new table to store claim default permission data if it doesn't exist.
     */
    private fun createClaimPermissionTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS claimPermissions (claimId TEXT NOT NULL, " +
                "permission TEXT NOT NULL, FOREIGN KEY (claimId) REFERENCES claims(id);"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }

    /**
     * Creates a new table to store player permission data if it doesn't exist.
     */
    private fun createPlayerTable() {
        val sqlQuery = "CREATE TABLE IF NOT EXISTS players (playerId TEXT, claimOwnerId TEXT, " +
                "claimId TEXT, permission TEXT, FOREIGN KEY(claimId) REFERENCES claims(id));"
        try {
            val statement = connection.prepareStatement(sqlQuery)
            statement.executeUpdate()
            statement.close()
        } catch (error: SQLException) {
            error.printStackTrace()
        }
    }
}