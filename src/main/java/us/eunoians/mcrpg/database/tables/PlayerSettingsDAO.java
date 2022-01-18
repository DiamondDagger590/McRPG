package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's settings
 *
 * @author DiamondDagger590
 */
public class PlayerSettingsDAO {

    private static final String TABLE_NAME = "mcrpg_player_settings";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link DatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    public static CompletableFuture<Boolean> attemptCreateTable(Connection connection, DatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Check to see if the table already exists
            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

            /*****
             ** Table Description:
             ** Contains player data for the archery skill
             *
             *
             * id is the id of the entry which auto increments but doesn't really serve a large purpose since it isn't
             * guaranteed to be the same for players across the board
             * uuid is the {@link java.util.UUID} of the player being stored
             * keep_hand is the setting for preventing items from going into the player's held item slot if that slot is empty
             * ignore_tips is the setting for if a player wants to ignore McRPG's automatic tips
             * auto_deny is the setting for if a player wants to auto deny all new abilities
             * require_empty_offhand is the setting for if a player's offhand should be required to be empty in order to "ready" an ability
             * display_type is the setting controlling what method players want information, such as exp gain, to use for display
             * health_type is the setting controlling how a mob's health should appear over its head whenever a player hits it
             * unarmed_ignore_slot is the setting allowing a player to designate a slot to not have items go into it so they can have a slot always empty for unarmed
             * auto_accept_party_teleports is the setting for if a player should always have any incoming party tp requests instantly accepted
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                                                                           "(" +
                                                                           "`id` int(11) NOT NULL AUTO_INCREMENT," +
                                                                           "`uuid` varchar(32) NOT NULL," +
                                                                           "`keep_hand` BIT NOT NULL DEFAULT 0," +
                                                                           "`ignore_tips` BIT NOT NULL DEFAULT 0," +
                                                                           "`auto_deny` BIT NOT NULL DEFAULT 0," +
                                                                           "`require_empty_offhand` BIT NOT NULL DEFAULT 0," +
                                                                           "`display_type` varchar(32) NOT NULL DEFAULT 'Scoreboard'," +
                                                                           "`health_type` varchar(32) NOT NULL DEFAULT 'Bar'," +
                                                                           "`unarmed_ignore_slot` int(11) NOT NULL DEFAULT -1," +
                                                                           "`auto_accept_party_teleports` BIT NOT NULL DEFAULT 0," +
                                                                           "PRIMARY KEY (`uuid`)" +
                                                                           ");")) {
                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            isAcceptingQueries = true;

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
    public static CompletableFuture<Void> updateTable(Connection connection) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (TableVersionHistoryDAO.isAcceptingQueries()) {

                TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME).thenAccept(lastStoredVersion -> {

                    if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                        completableFuture.complete(null);
                        return;
                    }

                    isAcceptingQueries = false;

                    //Adds table to our tracking
                    if (lastStoredVersion == 0) {
                        TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                        lastStoredVersion = 1;
                    }

                    isAcceptingQueries = true;
                });

            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Gets a {@link PlayerSettingsSnapshot} containing all of the player's settings. If
     * the provided {@link UUID} doesn't have any data, an empty {@link PlayerSettingsSnapshot} will be returned instead with all the default setting values.
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link PlayerSettingsSnapshot} that has all of the player's settings.
     * If the provided {@link UUID} doesn't have any data, an empty {@link PlayerSettingsSnapshot} will be returned instead with all the default setting values.
     */
    public static CompletableFuture<PlayerSettingsSnapshot> getPlayerSettings(Connection connection, UUID uuid) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<PlayerSettingsSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            PlayerSettingsSnapshot playerSettingsSnapshot = new PlayerSettingsSnapshot(uuid);

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        boolean keepHandEmpty = resultSet.getBoolean("keep_hand");
                        boolean ignoreTips = resultSet.getBoolean("ignore_tips");
                        boolean autoDenyNewAbilities = resultSet.getBoolean("auto_deny");
                        boolean requireEmptyOffhandToReady = resultSet.getBoolean("require_empty_offhand");
                        DisplayType displayType = DisplayType.fromString(resultSet.getString("display_type"));
                        MobHealthbarUtils.MobHealthbarType mobHealthbarType = MobHealthbarUtils.MobHealthbarType.fromString(resultSet.getString("health_type"));
                        int unarmedIgnoreSlot = resultSet.getInt("unarmed_ignore_slot");
                        boolean autoAcceptPartyTeleports = resultSet.getBoolean("auto_accept_party_teleports");

                        playerSettingsSnapshot.setKeepHandEmpty(keepHandEmpty);
                        playerSettingsSnapshot.setIgnoreTips(ignoreTips);
                        playerSettingsSnapshot.setAutoDeny(autoDenyNewAbilities);
                        playerSettingsSnapshot.setRequireOffHand(requireEmptyOffhandToReady);
                        playerSettingsSnapshot.setDisplayType(displayType);
                        playerSettingsSnapshot.setHealthbarType(mobHealthbarType);
                        playerSettingsSnapshot.setUnarmedIgnoreSlot(unarmedIgnoreSlot);
                        playerSettingsSnapshot.setAutoAcceptPartyTeleports(autoAcceptPartyTeleports);

                    }

                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(playerSettingsSnapshot);

        });

        return completableFuture;
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerSettings(Connection connection, McRPGPlayer mcRPGPlayer) {

    }

    /**
     * Checks to see if this table is accepting queries at the moment. A reason it could be false is either the table is
     * in creation or the table is being updated and for some reason a query is attempting to be run.
     *
     * @return {@code true} if this table is accepting queries
     */
    public static boolean isAcceptingQueries() {
        return isAcceptingQueries;
    }

    /**
     * A POJO containing all relevant player data obtained from this DAO
     */
    public static class PlayerSettingsSnapshot {

        private final UUID uuid;

        private boolean keepHandEmpty;
        private boolean ignoreTips;
        private boolean autoDeny;
        private boolean requireOffHand;
        private DisplayType displayType;
        private MobHealthbarUtils.MobHealthbarType healthbarType;
        private int unarmedIgnoreSlot;
        private boolean autoAcceptPartyTeleports;

        PlayerSettingsSnapshot(@NotNull UUID uuid) {
            this.uuid = uuid;
            this.displayType = DisplayType.SCOREBOARD;
            this.healthbarType = MobHealthbarUtils.MobHealthbarType.BAR;
            this.unarmedIgnoreSlot = -1;
        }

        /**
         * Gets the {@link UUID} of the player represented by this snapshot
         *
         * @return The {@link UUID} of the player represented by this snapshot
         */
        public UUID getUuid() {
            return uuid;
        }

        /**
         * Gets the snapshotted setting for keeping the player's held item slot empty
         *
         * @return {@code true} if the player's keep held item slot empty setting is enabled in this snapshot
         */
        public boolean isKeepHandEmpty() {
            return keepHandEmpty;
        }

        /**
         * Sets the snapshotted value for the player's keep held item slot empty setting
         *
         * @param keepHandEmpty The snapshotted value for the player's keep held item slot empty setting
         */
        void setKeepHandEmpty(boolean keepHandEmpty) {
            this.keepHandEmpty = keepHandEmpty;
        }

        /**
         * Gets the snapshotted setting for the player to ignore all McRPG tips
         *
         * @return {@code true} if the player's ignore tips setting is enabled in this snapshot
         */
        public boolean isIgnoreTips() {
            return ignoreTips;
        }

        /**
         * Sets the snapshotted value for the player's ignore tips setting
         *
         * @param ignoreTips The snapshotted value for the player's ignore tips setting
         */
        void setIgnoreTips(boolean ignoreTips) {
            this.ignoreTips = ignoreTips;
        }

        /**
         * Gets the snapshotted setting for auto denying any new abilities
         *
         * @return {@code true} if the player's auto deny new abilities setting is enabled in this snapshot
         */
        public boolean isAutoDeny() {
            return autoDeny;
        }

        /**
         * Sets the snapshotted value for the player's auto deny new abilities setting
         *
         * @param autoDeny The snapshotted value for the player's auto deny new abilities setting
         */
        void setAutoDeny(boolean autoDeny) {
            this.autoDeny = autoDeny;
        }

        /**
         * Gets the snapshotted setting for requiring an empty offhand to "ready" abilities
         *
         * @return {@code true} if the player's require an empty offhand to "ready" setting is enabled in this snapshot
         */
        public boolean isRequireOffHand() {
            return requireOffHand;
        }

        /**
         * Sets the snapshotted value for the player's require an empty offhand to "ready" setting
         *
         * @param requireOffHand The snapshotted value for the player's require an empty offhand to "ready" setting
         */
        void setRequireOffHand(boolean requireOffHand) {
            this.requireOffHand = requireOffHand;
        }

        /**
         * Gets the snapshotted setting for the player's {@link DisplayType} preference
         *
         * @return The {@link DisplayType} of the player's desired display setting for this snapshot
         */
        @NotNull
        public DisplayType getDisplayType() {
            return displayType;
        }

        /**
         * Sets the snapshotted value for the player's {@link DisplayType} preference
         *
         * @param displayType The snapshotted value for the player's {@link DisplayType} preference
         */
        void setDisplayType(@NotNull DisplayType displayType) {
            this.displayType = displayType;
        }

        /**
         * Gets the snapshotted setting for the player's {@link us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils.MobHealthbarType} preference
         *
         * @return The {@link us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils.MobHealthbarType} of the player's desired display setting for this snapshot
         */
        @NotNull
        public MobHealthbarUtils.MobHealthbarType getHealthbarType() {
            return healthbarType;
        }

        /**
         * Sets the snapshotted value for the player's {@link us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils.MobHealthbarType} preference
         *
         * @param healthbarType The snapshotted value for the player's {@link us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils.MobHealthbarType} preference
         */
        void setHealthbarType(MobHealthbarUtils.MobHealthbarType healthbarType) {
            this.healthbarType = healthbarType;
        }

        /**
         * Gets the snapshotted setting for keeping a slot in the player's hotbar empty for unarmed
         *
         * @return {@code -1} if the setting is disabled, or a value of {@code 0} - {@code 8} for the slot to keep empty
         */
        public int getUnarmedIgnoreSlot() {
            return unarmedIgnoreSlot;
        }

        /**
         * Sets the snapshotted value for the player's unarmed ignored hotbar slot setting
         *
         * @param unarmedIgnoreSlot The snapshotted value for the player's unarmed ignored hotbar slot setting
         */
        void setUnarmedIgnoreSlot(int unarmedIgnoreSlot) {
            this.unarmedIgnoreSlot = unarmedIgnoreSlot;
        }

        /**
         * Gets the snapshotted setting for auto accepting incoming party teleport requests
         *
         * @return {@code true} if the player's auto accept incoming party teleport requests setting is enabled in this snapshot
         */
        public boolean isAutoAcceptPartyTeleports() {
            return autoAcceptPartyTeleports;
        }

        /**
         * Sets the snapshotted value for the player's auto accepting incoming party teleport requests setting
         *
         * @param autoAcceptPartyTeleports The snapshotted value for the player's auto accepting incoming party teleport requests setting
         */
        void setAutoAcceptPartyTeleports(boolean autoAcceptPartyTeleports) {
            this.autoAcceptPartyTeleports = autoAcceptPartyTeleports;
        }

    }
}
