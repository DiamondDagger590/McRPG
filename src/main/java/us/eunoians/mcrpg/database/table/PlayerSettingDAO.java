package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.exception.setting.SettingNotRegisteredException;
import us.eunoians.mcrpg.setting.PlayerSetting;
import us.eunoians.mcrpg.setting.PlayerSettingRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This DAO is in charge of dealing with the saving and loading of {@link PlayerSetting}s.
 */
public class PlayerSettingDAO {

    private static final String TABLE_NAME = "mcrpg_player_settings";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link McRPGDatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    @NotNull
    public static CompletableFuture<Boolean> attemptCreateTable(@NotNull Connection connection, @NotNull McRPGDatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Check to see if the table already exists
            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
            }

            /*****
             ** Table Description:
             ** Contains player data that doesn't have another table to be located
             *
             *
             * uuid is the {@link java.util.UUID} of the player being stored
             * ability_points is the amount of ability points the player has left to spend
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                    "(" +
                    "`uuid` varchar(36) NOT NULL," +
                    "`setting_key` varchar(128) NOT NULL," +
                    "`setting_value` varchar(128) NOT NULL, " +
                    "PRIMARY KEY (`uuid`, `setting_key`)" +
                    ");")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            // Create an index for UUIDs
            try (PreparedStatement statement = connection.prepareStatement("CREATE INDEX idx_uuid ON " + TABLE_NAME + " (uuid)")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(true);
        });

        return completableFuture;
    }


    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     * @return The {@link  CompletableFuture} that is running these changes.
     */
    @NotNull
    public static CompletableFuture<Void> updateTable(@NotNull Connection connection) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME).thenAccept(lastStoredVersion -> {

                if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                    completableFuture.complete(null);
                    return;
                }

                //Adds table to our tracking
                if (lastStoredVersion == 0) {
                    TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                    lastStoredVersion = 1;
                }

            });

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Gets all of the {@link PlayerSetting}s for a given player.
     * <p>
     * If the player doesn't have a setting saved, it will instead provide the default option for the given setting.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing a {@link Set} of all {@link PlayerSetting}s for a player.
     */
    public static CompletableFuture<Set<PlayerSetting>> getPlayerSettings(@NotNull Connection connection, @NotNull UUID playerUUID) {
        CompletableFuture<Set<PlayerSetting>> settingFuture = new CompletableFuture<>();
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        PlayerSettingRegistry playerSettingRegistry = McRPG.getInstance().getPlayerSettingRegistry();
        Set<PlayerSetting> playerSettings = new HashSet<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT setting_value FROM " + TABLE_NAME + " WHERE uuid = ? AND setting_key = ?")) {
                preparedStatement.setString(1, playerUUID.toString());
                // Go through all settings
                for (NamespacedKey settingKey : playerSettingRegistry.getSettingKeys()) {
                    var settingOptional = playerSettingRegistry.getSetting(settingKey);
                    if (settingOptional.isEmpty()) {
                        continue;
                    }
                    PlayerSetting defaultSetting = settingOptional.get();
                    // Fetch the setting
                    preparedStatement.setString(2, settingKey.toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String settingValue = resultSet.getString("setting_value");
                        Optional<? extends PlayerSetting> playerSetting = defaultSetting.fromString(settingValue);
                        // If the player doesn't have the setting saved, then we should just grab the default one
                        if (playerSetting.isPresent()) {
                            playerSettings.add(playerSetting.get());
                        } else {
                            playerSettings.add(defaultSetting);
                        }
                    }
                }
            } catch (SQLException e) {
                settingFuture.completeExceptionally(e);
            }

        });
        return settingFuture;
    }

    /**
     * Gets the {@link PlayerSetting} belonging to the provided {@link NamespacedKey}.
     * <p>
     * If the player doesn't have a setting saved, it will instead provide the default option for the given setting.
     *
     * @param connection The {@link Connection} to use
     * @param playerUUID The {@link UUID} of the player.
     * @param settingKey The {@link NamespacedKey} of the {@link PlayerSetting}.
     * @return A {@link CompletableFuture} that contains the resulting {@link PlayerSetting}.
     * @throws SettingNotRegisteredException If {@link PlayerSettingRegistry#isSettingRegistered(NamespacedKey)} returns {@code false}.
     */
    public static CompletableFuture<PlayerSetting> getPlayerSetting(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull NamespacedKey settingKey) {
        CompletableFuture<PlayerSetting> settingFuture = new CompletableFuture<>();
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        PlayerSettingRegistry playerSettingRegistry = McRPG.getInstance().getPlayerSettingRegistry();
        var settingOptional = playerSettingRegistry.getSetting(settingKey);
        if (settingOptional.isEmpty()) {
            throw new SettingNotRegisteredException(settingKey);
        }

        PlayerSetting defaultSetting = settingOptional.get();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT setting_value FROM " + TABLE_NAME + " WHERE uuid = ? AND setting_key = ?")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setString(2, settingKey.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String settingValue = resultSet.getString("setting_value");
                        Optional<? extends PlayerSetting> playerSetting = defaultSetting.fromString(settingValue);
                        // If the player doesn't have the setting saved, then we should just grab the default one
                        if (playerSetting.isPresent()) {
                            settingFuture.complete(playerSetting.get());
                        } else {
                            settingFuture.complete(defaultSetting);
                        }
                    }
                }
            } catch (SQLException e) {
                settingFuture.completeExceptionally(e);
            }
        });

        return settingFuture;
    }

    /**
     * Saves all of the {@link PlayerSetting}s for a given player.
     *
     * @param connection     The {@link Connection} to use.
     * @param playerUUID     The {@link UUID} of the player.
     * @param playerSettings The {@link Set} of {@link PlayerSetting}s to save.
     * @return A {@link CompletableFuture} that is finished whenever all settings have been saved.
     */
    public static CompletableFuture<Void> savePlayerSettings(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull Set<PlayerSetting> playerSettings) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, setting_key, setting_value) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, playerUUID.toString());

                for (PlayerSetting playerSetting : playerSettings) {
                    preparedStatement.setString(2, playerSetting.getSettingKey().toString());
                    preparedStatement.setString(3, playerSetting.name());
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Saves a {@link PlayerSetting} for a player.
     *
     * @param connection    The {@link Connection} to use.
     * @param playerUUID    The {@link UUID} of the player.
     * @param playerSetting The {@link PlayerSetting} to save.
     * @return A {@link CompletableFuture} that is completed whenever the setting is done being saved.
     */
    public static CompletableFuture<Void> savePlayerSetting(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull PlayerSetting playerSetting) {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, setting_key, setting_value) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setString(2, playerSetting.getSettingKey().toString());
                preparedStatement.setString(3, playerSetting.name());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(null);
        });

        return completableFuture;
    }
}
