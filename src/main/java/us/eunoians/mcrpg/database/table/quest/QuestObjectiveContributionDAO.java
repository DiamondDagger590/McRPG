package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_objective_contributions} table, tracking per-player
 * contribution amounts for each objective instance. This enables fair attribution
 * in party/group quests where multiple players contribute to the same objective.
 */
public class QuestObjectiveContributionDAO {

    static final String TABLE_NAME = "mcrpg_quest_objective_contributions";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create the contributions table if it does not already exist.
     *
     * @param connection the database connection
     * @param database   the database instance
     * @return {@code true} if a new table was created, {@code false} if it already existed
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`objective_uuid` varchar(36) NOT NULL," +
                "`player_uuid` varchar(36) NOT NULL," +
                "`amount` BIGINT NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`objective_uuid`, `player_uuid`)," +
                "FOREIGN KEY (`objective_uuid`) REFERENCES `" + QuestObjectiveInstanceDAO.TABLE_NAME + "`(`objective_uuid`) ON DELETE CASCADE" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Applies any pending schema migrations for this table.
     *
     * @param connection the database connection
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
    }

    /**
     * Saves all player contributions for a single objective, using upsert semantics.
     * Each entry in the objective's contribution tracker becomes one row.
     *
     * @param connection the database connection
     * @param objective  the objective whose contributions should be saved
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveContributions(@NotNull Connection connection,
                                                            @NotNull QuestObjectiveInstance objective) {
        List<PreparedStatement> statements = new ArrayList<>();
        String objectiveUUID = objective.getQuestObjectiveUUID().toString();
        for (Map.Entry<UUID, Long> entry : objective.getPlayerContributions().entrySet()) {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO " + TABLE_NAME +
                                " (objective_uuid, player_uuid, amount) VALUES (?, ?, ?)" +
                                " ON CONFLICT(objective_uuid, player_uuid) DO UPDATE SET" +
                                " amount = excluded.amount");
                ps.setString(1, objectiveUUID);
                ps.setString(2, entry.getKey().toString());
                ps.setLong(3, entry.getValue());
                statements.add(ps);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return statements;
    }

    /**
     * Loads all player contributions for a single objective.
     *
     * @param connection    the database connection
     * @param objectiveUUID the objective UUID to load contributions for
     * @return a map of player UUID to contribution amount
     */
    @NotNull
    public static Map<UUID, Long> loadContributions(@NotNull Connection connection, @NotNull UUID objectiveUUID) {
        Map<UUID, Long> contributions = new HashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT player_uuid, amount FROM " + TABLE_NAME + " WHERE objective_uuid = ?")) {
            ps.setString(1, objectiveUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    long amount = rs.getLong("amount");
                    contributions.put(playerUUID, amount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contributions;
    }

    /**
     * Deletes all contribution rows for a given objective.
     *
     * @param connection    the database connection
     * @param objectiveUUID the objective UUID whose contributions should be deleted
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> deleteContributions(@NotNull Connection connection,
                                                              @NotNull UUID objectiveUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE objective_uuid = ?");
            ps.setString(1, objectiveUUID.toString());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }
}
