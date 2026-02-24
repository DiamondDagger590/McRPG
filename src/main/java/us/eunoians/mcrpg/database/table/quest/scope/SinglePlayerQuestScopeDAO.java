package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
// FK references mcrpg_quest_instances table directly by name
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SinglePlayerQuestScopeDAO {

    private static final String TABLE_NAME = "mcrpg_single_player_quest_scope";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection The {@link Connection} to use to attempt the creation
     * @param database   The {@link Database} being used to attempt to create the table
     * @return {@code true} if a new table was made or {@code false} otherwise.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        //Check to see if the table already exists
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }

        /*****
         ** Table Description:
         ** Contains quest scopes that encompass a single player
         *
         *
         * quest_uuid is the UUID of the quest the scope belongs to
         * player_uuid is the UUID of the player encompassed by the scope
         **
         ** Reasoning for structure:
         ** PK is the quest_uuid as each quest can only have one scope
         *
         * The foreign key requires the player's uuid to be present in the loadout table as that's where the player's loadout info is stored
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`quest_uuid` varchar(36) NOT NULL," +
                "`player_uuid` varchar(36) NOT NULL," +
                "PRIMARY KEY (`quest_uuid`), " +
                // Ensure that the loadout is stored in the info table, also if it ever gets removed from that table, ensure it's deleted here
                // TODO change the table name to be the quest DAO
                "CONSTRAINT FK_single_player_scope FOREIGN KEY (`quest_uuid`) REFERENCES `mcrpg_quest_instances` (`quest_uuid`) ON DELETE CASCADE" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }

        //Adds table to our tracking
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    @NotNull
    public static UUID getPlayerInScope(@NotNull Connection connection, @NotNull UUID questUUID) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT player_uuid FROM " + TABLE_NAME + " where quest_uuid = ?")) {
            preparedStatement.setString(1, questUUID.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("player_uuid"));
                }

            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("No player found in scope for quest " + questUUID) ;
    }

    /**
     * Finds all active quest UUIDs where the given player is the scoped single player.
     * Active means the quest state is {@code NOT_STARTED} or {@code IN_PROGRESS}.
     *
     * @param connection the database connection
     * @param playerUUID the player to find quests for
     * @return a list of active quest UUIDs scoped to this player
     */
    @NotNull
    public static List<UUID> findActiveQuestsForPlayer(@NotNull Connection connection, @NotNull UUID playerUUID) {
        List<UUID> questUUIDs = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT sp.quest_uuid FROM " + TABLE_NAME + " sp " +
                        "JOIN mcrpg_quest_instances qi ON sp.quest_uuid = qi.quest_uuid " +
                        "WHERE sp.player_uuid = ? AND qi.state IN ('NOT_STARTED', 'IN_PROGRESS')")) {
            ps.setString(1, playerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questUUIDs.add(UUID.fromString(rs.getString("quest_uuid")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questUUIDs;
    }

    @NotNull
    public static List<PreparedStatement> saveScope(@NotNull Connection connection, @NotNull SinglePlayerQuestScope singlePlayerQuestScope) {
        if (!singlePlayerQuestScope.isScopeValid()) {
            throw new QuestScopeInvalidStateException(singlePlayerQuestScope, "Cannot save scope without a player UUID");
        }
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (quest_uuid, player_uuid) VALUES (?, ?)" +
                    " ON CONFLICT (quest_uuid) DO UPDATE SET player_uuid = excluded.player_uuid");
            preparedStatement.setString(1, singlePlayerQuestScope.getQuestUUID().toString());
            preparedStatement.setString(2, singlePlayerQuestScope.getPlayerInScope().get().toString());
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }
}
