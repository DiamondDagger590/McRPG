package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.exception.setting.SettingNotRegisteredException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This DAO is in charge of dealing with the saving and loading of {@link PlayerSetting}s.
 */
public class PlayerSettingDAO {

    private static final String TABLE_NAME = "mcrpg_player_settings";
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
            return false;
        }

        // Create an index for UUIDs
        try (PreparedStatement statement = connection.prepareStatement("CREATE INDEX idx_uuid ON " + TABLE_NAME + " (uuid)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    /**
     * Gets all of the {@link PlayerSetting}s for a given player.
     * <p>
     * If the player doesn't have a setting saved, it will instead provide the default option for the given setting.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @return A {@link CompletableFuture} containing a {@link Set} of all {@link PlayerSetting}s for a player.
     */
    @NotNull
    public static Set<PlayerSetting> getPlayerSettings(@NotNull Connection connection, @NotNull UUID playerUUID) {
        PlayerSettingRegistry playerSettingRegistry = McRPG.getInstance().registryAccess().registry(RegistryKey.PLAYER_SETTING);
        Set<PlayerSetting> playerSettings = new HashSet<>();
        Logger logger = McRPG.getInstance().getLogger();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT setting_value FROM " + TABLE_NAME + " WHERE uuid = ? AND setting_key = ?")) {
            preparedStatement.setString(1, playerUUID.toString());
            // Go through all settings
            logger.info("Loading settings");
            for (NamespacedKey settingKey : playerSettingRegistry.getSettingKeys()) {
                logger.info("Setting "  + settingKey.toString());
                var settingOptional = playerSettingRegistry.getSetting(settingKey);
                if (settingOptional.isEmpty()) {
                    logger.info("setting is empty");
                    continue;
                }
                PlayerSetting defaultSetting = settingOptional.get();
                logger.info("got default setting: " + defaultSetting);
                // Fetch the setting
                preparedStatement.setString(2, settingKey.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    logger.info("got result from db");
                    String settingValue = resultSet.getString("setting_value");
                    Optional<? extends PlayerSetting> playerSetting = defaultSetting.fromString(settingValue);
                    // If the player doesn't have the setting saved, then we should just grab the default one
                    if (playerSetting.isPresent()) {
                        logger.info("grabbed setting is present, using it");
                        playerSettings.add(playerSetting.get());
                    } else {
                        logger.info("grabbed setting is absent, using default");
                        playerSettings.add(defaultSetting);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return playerSettings;
        }
        logger.info("Loaded " + playerSettings.size() + " settings");
        return playerSettings;
    }

    /**
     * Gets the {@link PlayerSetting} belonging to the provided {@link NamespacedKey}.
     * <p>
     * If the player doesn't have a setting saved, it will instead provide the default option for the given setting.
     *
     * @param connection The {@link Connection} to use
     * @param playerUUID The {@link UUID} of the player.
     * @param settingKey The {@link NamespacedKey} of the {@link PlayerSetting}.
     * @return The requested {@link PlayerSetting}.
     * @throws SettingNotRegisteredException If {@link PlayerSettingRegistry#isSettingRegistered(NamespacedKey)} returns {@code false}.
     */
    @NotNull
    public static PlayerSetting getPlayerSetting(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull NamespacedKey settingKey) {
        PlayerSettingRegistry playerSettingRegistry = McRPG.getInstance().registryAccess().registry(RegistryKey.PLAYER_SETTING);
        var settingOptional = playerSettingRegistry.getSetting(settingKey);
        if (settingOptional.isEmpty()) {
            throw new SettingNotRegisteredException(settingKey);
        }
        PlayerSetting defaultSetting = settingOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT setting_value FROM " + TABLE_NAME + " WHERE uuid = ? AND setting_key = ?")) {
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setString(2, settingKey.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String settingValue = resultSet.getString("setting_value");
                    Optional<? extends PlayerSetting> playerSetting = defaultSetting.fromString(settingValue);
                    // If the player doesn't have the setting saved, then we should just grab the default one
                    if (playerSetting.isPresent()) {
                        return playerSetting.get();
                    } else {
                        return defaultSetting;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultSetting;
    }

    /**
     * Saves all of the {@link PlayerSetting}s for a given player.
     *
     * @param connection     The {@link Connection} to use.
     * @param playerUUID     The {@link UUID} of the player.
     * @param playerSettings The {@link Set} of {@link PlayerSetting}s to save.
     * @return A {@link List} of all {@link PreparedStatement}s to run in order to save.
     */
    public static List<PreparedStatement> savePlayerSettings(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull Set<? extends PlayerSetting> playerSettings) {
        return playerSettings.stream().map(playerSetting -> savePlayerSetting(connection, playerUUID, playerSetting)).toList();
    }

    /**
     * Saves a {@link PlayerSetting} for a player.
     *
     * @param connection    The {@link Connection} to use.
     * @param playerUUID    The {@link UUID} of the player.
     * @param playerSetting The {@link PlayerSetting} to save.
     * @return The {@link PreparedStatement} to run in order to save.
     */
    public static PreparedStatement savePlayerSetting(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull PlayerSetting playerSetting) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, setting_key, setting_value) VALUES (?, ?, ?)");
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setString(2, playerSetting.getSettingKey().toString());
            preparedStatement.setString(3, playerSetting.name());
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
