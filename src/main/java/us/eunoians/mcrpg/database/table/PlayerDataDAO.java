package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's specific data that doesn't really belong in another table
 */
public class PlayerDataDAO {

    private static final String TABLE_NAME = "mcrpg_player_data";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;

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

            isAcceptingQueries = false;

            /*****
             ** Table Description:
             ** Contains player data that doesn't have another table to be located
             *
             *
             * uuid is the {@link java.util.UUID} of the player being stored
             * party_uuid is the uuid string of the player's current party. The absence of a party means that the value "nu" will be present
             * power_level is the last calculated power level for that player. This is a sum of the current_level field from all skill tables for the player
             * ability_points is the amount of ability points the player has left to spend
             * replace_ability_cooldown_time is the amount of time that the player has before they can replace an ability into their loadout again
             * redeemable_exp is the amount of redeemable exp that the player has
             * redeemable_levels is the amount of redeemable levels that the player has
             * boosted_exp is the amount of exp from McMMO level conversion the player has
             * divine_escape_exp_debuff is the percentage by which the player will gain less exp while debuffed
             * divine_escape_damage_debuff is the percentage by which the player will deal less damage while debuffed
             * divine_escape_exp_end_time is the time in millis that the player's exp debuff will end
             * divine_escape_damage_end_time is the time in millis that the player's damage debuff will end
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                                                                           "(" +
                                                                           "`uuid` varchar(36) NOT NULL," +
                                                                           "`party_uuid` varchar(32) NOT NULL DEFAULT 'nu'," +
                                                                           "`power_level` int(11) NOT NULL DEFAULT 0," +
                                                                           "`ability_points` int(11) NOT NULL DEFAULT 1," +
                                                                           "`replace_ability_cooldown_time` int(11) NOT NULL DEFAULT 0," +
                                                                           "`redeemable_exp` int(11) NOT NULL DEFAULT 0," +
                                                                           "`redeemable_levels` int(11) NOT NULL DEFAULT 0," +
                                                                           "`boosted_exp` int(11) NOT NULL DEFAULT 0," +
                                                                           "`divine_escape_exp_debuff` double(11) NOT NULL DEFAULT 0," +
                                                                           "`divine_escape_damage_debuff` double(11) NOT NULL DEFAULT 0," +
                                                                           "`divine_escape_exp_end_time` int(11) NOT NULL DEFAULT 0," +
                                                                           "`divine_escape_damage_end_time` int(11) NOT NULL DEFAULT 0," +
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

                isAcceptingQueries = false;

                //Adds table to our tracking
                if (lastStoredVersion == 0) {
                    TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                    lastStoredVersion = 1;
                }

                isAcceptingQueries = true;
            });

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Gets a {@link PlayerDataSnapshot} containing all of the player's data for misc information such as redeemable exp. If
     * the provided {@link UUID} doesn't have any data, an empty {@link PlayerDataSnapshot} will be returned instead with null or defaulted 0 values.
     *
     * @param connection The {@link Connection} to use to get the player data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link PlayerDataSnapshot} that has all of the player's misc
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link PlayerDataSnapshot} will be returned instead with null or defaulted 0 values.
     */
    @NotNull
    public static CompletableFuture<PlayerDataSnapshot> getPlayerData(@NotNull Connection connection, @NotNull UUID uuid) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<PlayerDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            PlayerDataSnapshot playerDataSnapshot = new PlayerDataSnapshot(uuid);

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        String partyUUIDString = resultSet.getString("party_uuid");
                        UUID partyUUID = partyUUIDString.equalsIgnoreCase("nu") ? null : UUID.fromString(partyUUIDString);

                        int powerLevel = resultSet.getInt("power_level");
                        int abilityPoints = resultSet.getInt("ability_points");
                        long replaceAbilityCooldownTime = resultSet.getLong("replace_ability_cooldown_time");
                        int redeemableExp = resultSet.getInt("redeemable_exp");
                        int redeemableLevels = resultSet.getInt("redeemable_levels");
                        int boostedExp = resultSet.getInt("boosted_exp");
                        double divineEscapeExpDebuff = resultSet.getDouble("divine_escape_exp_debuff");
                        double divineEscapeDamageDebuff = resultSet.getDouble("divine_escape_damage_debuff");
                        long divineEscapeExpEndTime = resultSet.getLong("divine_escape_exp_end_time");
                        long divineEscapeDamageEndTime = resultSet.getLong("divine_escape_damage_end_time");


                        playerDataSnapshot.setPartyUUID(partyUUID);
                        playerDataSnapshot.setPowerLevel(powerLevel);
                        playerDataSnapshot.setAbilityPoints(abilityPoints);
                        playerDataSnapshot.setReplaceAbilityCooldownTime(replaceAbilityCooldownTime);
                        playerDataSnapshot.setRedeemableExp(redeemableExp);
                        playerDataSnapshot.setRedeemableLevels(redeemableLevels);
                        playerDataSnapshot.setBoostedExp(boostedExp);
                        playerDataSnapshot.setDivineEscapeExpDebuff(divineEscapeExpDebuff);
                        playerDataSnapshot.setDivineEscapeDamageDebuff(divineEscapeDamageDebuff);
                        playerDataSnapshot.setDivineEscapeExpEndTime(divineEscapeExpEndTime);
                        playerDataSnapshot.setDivineEscapeDamageEndTime(divineEscapeDamageEndTime);
                    }

                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(playerDataSnapshot);

        });

        return completableFuture;
    }

    /**
     * Saves all the player data that is stored inside this table, such as redeemable exp, for the provided {@link McRPGPlayer}.
     *
     * @param connection  The {@link Connection} to use to save the player data
     * @param mcRPGPlayer The {@link McRPGPlayer} whose data is being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerData(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        DatabaseDriver databaseDriver = databaseManager.getDriver();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = databaseDriver == DatabaseDriver.H2 ? connection.prepareStatement("INSERT INTO " + TABLE_NAME +
                                                                                                                         " (uuid, power_level, ability_points, redeemable_exp, redeemable_levels, " +
                                                                                                                         "divine_escape_exp_debuff, divine_escape_damage_debuff, divine_escape_exp_end_time, divine_escape_damage_end_time, " +
                                                                                                                         "replace_ability_cooldown_time, boosted_exp, party_uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                                                                                                                         "power_level=VALUES(power_level), ability_points=VALUES(ability_points), " +
                                                                                                                         "redeemable_exp=VALUES(redeemable_exp), redeemable_levels=VALUES(redeemable_levels), divine_escape_exp_debuff=VALUES(divine_escape_exp_debuff), " +
                                                                                                                         "divine_escape_damage_debuff=VALUES(divine_escape_damage_debuff), divine_escape_exp_end_time=VALUES(divine_escape_exp_end_time), divine_escape_damage_end_time=VALUES(divine_escape_damage_end_time), " +
                                                                                                                         "replace_ability_cooldown_time=VALUES(replace_ability_cooldown_time), boosted_exp=VALUES(boosted_exp), party_uuid=VALUES(party_uuid);")
                                                                                           : connection.prepareStatement("REPLACE INTO " + TABLE_NAME +
                                                                                                                         " (uuid, power_level, ability_points, redeemable_exp, redeemable_levels, " +
                                                                                                                         "divine_escape_exp_debuff, divine_escape_damage_debuff, divine_escape_exp_end_time, divine_escape_damage_end_time, " +
                                                                                                                         "replace_ability_cooldown_time, boosted_exp, party_uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                preparedStatement.setString(1, mcRPGPlayer.getUUID().toString());
//                preparedStatement.setInt(2, mcRPGPlayer.getPowerLevel());
//                preparedStatement.setInt(3, mcRPGPlayer.getAbilityPoints());
//                preparedStatement.setInt(4, mcRPGPlayer.getRedeemableExp());
//                preparedStatement.setInt(5, mcRPGPlayer.getRedeemableLevels());
//                preparedStatement.setDouble(6, mcRPGPlayer.getDivineEscapeExpDebuff());
//                preparedStatement.setDouble(7, mcRPGPlayer.getDivineEscapeDamageDebuff());
//                preparedStatement.setLong(8, mcRPGPlayer.getDivineEscapeExpEnd());
//                preparedStatement.setLong(9, mcRPGPlayer.getDivineEscapeDamageEnd());
//                preparedStatement.setLong(10, mcRPGPlayer.getEndTimeForReplaceCooldown());
//                preparedStatement.setInt(11, mcRPGPlayer.getBoostedExp());
//                UUID partyID = mcRPGPlayer.getPartyID();
//                preparedStatement.setString(12, (partyID == null ? "nu" : partyID.toString()));

                preparedStatement.executeUpdate();
                completableFuture.complete(null);
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
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
    public static class PlayerDataSnapshot {

        private final UUID uuid;

        private UUID partyUUID;
        private int powerLevel;
        private int abilityPoints;
        private long replaceAbilityCooldownTime;
        private int redeemableExp;
        private int redeemableLevels;
        private int boostedExp;
        private double divineEscapeExpDebuff;
        private double divineEscapeDamageDebuff;
        private long divineEscapeExpEndTime;
        private long divineEscapeDamageEndTime;

        public PlayerDataSnapshot(@NotNull UUID uuid) {
            this.uuid = uuid;
        }

        /**
         * Gets the {@link UUID} of the player represented by this snapshot
         *
         * @return The {@link UUID} of the player represented by this snapshot
         */
        @NotNull
        public UUID getUuid() {
            return uuid;
        }

        /**
         * Gets the {@link UUID} of the player's {@link}
         *
         * @return The {@link UUID} of the player's {@link} or {@code null} if the
         * player isn't in a party
         */
        @Nullable
        public UUID getPartyUUID() {
            return partyUUID;
        }

        /**
         * Sets the {@link UUID} of the player's {@link} for this snapshot
         *
         * @param partyUUID The {@link UUID} of the player's {@link} for this snapshot
         *                  or {@code null} if the player isn't in a party
         */
        void setPartyUUID(@Nullable UUID partyUUID) {
            this.partyUUID = partyUUID;
        }

        /**
         * Gets the power level of the player, which is the sum of all of the player's skill levels
         *
         * @return The positive, zero inclusive power level of a player
         */
        public int getPowerLevel() {
            return powerLevel;
        }

        /**
         * Sets the power level of the player represented by this snapshot
         *
         * @param powerLevel The new positive, zero inclusive power level of the player represented by this snapshot
         */
        void setPowerLevel(int powerLevel) {
            this.powerLevel = Math.max(0, powerLevel);
        }

        /**
         * Gets the amount of ability points that the player has left to spend
         *
         * @return The positive, zero inclusive amout of ability points that the player has left to spend
         */
        public int getAbilityPoints() {
            return abilityPoints;
        }

        /**
         * Sets the amount of ability points that the player has to spend as represented by this snapshot
         *
         * @param abilityPoints The positive, zero inclusive amount of ability points for the player to be represented by this snapshot
         */
        void setAbilityPoints(int abilityPoints) {
            this.abilityPoints = Math.max(0, abilityPoints);
        }

        /**
         * Gets the time in millis that the player is able to replace an ability again for this snapshot
         *
         * @return The time in millis that the player is able to replace an ability again for this snapshot
         */
        public long getReplaceAbilityCooldownTime() {
            return replaceAbilityCooldownTime;
        }

        /**
         * Sets the time in millis that the player is able to replace an ability again according to this snapshot
         *
         * @param replaceAbilityCooldownTime The time in millis that the player is able to replace an ability again according to this snapshot
         */
        void setReplaceAbilityCooldownTime(long replaceAbilityCooldownTime) {
            this.replaceAbilityCooldownTime = replaceAbilityCooldownTime;
        }

        /**
         * Gets the amount of redeemable exp that the player has according to this snapshot
         *
         * @return The positive, zero inclusive amount of redeemable exp that the player has according to this snapshot
         */
        public int getRedeemableExp() {
            return redeemableExp;
        }

        /**
         * Sets the amount of redeemable exp that the player has according to this snapshot
         *
         * @param redeemableExp A positive, zero inclusive amount of redeemable exp that this snapshot should report the player having
         */
        void setRedeemableExp(int redeemableExp) {
            this.redeemableExp = Math.max(0, redeemableExp);
        }

        /**
         * Gets the amount of redeemable levels that the player has according to this snapshot
         *
         * @return The positive, zero inclusive amount of redeemable leves that the player has according to this snapshot
         */
        public int getRedeemableLevels() {
            return redeemableLevels;
        }

        /**
         * Sets the amount of redeemable levels that the player has according to this snapshot
         *
         * @param redeemableLevels A positive, zero inclusive amount of redeemable levels that this snapshot should report the player having
         */
        void setRedeemableLevels(int redeemableLevels) {
            this.redeemableLevels = Math.max(0, redeemableLevels);
        }

        /**
         * Gets the amount of boosted exp that the player has according to this snapshot
         *
         * @return The positive, zero inclusive amount of boosted exp that the player has according to this snapshot
         */
        public int getBoostedExp() {
            return boostedExp;
        }

        /**
         * Sets the amount of boosted exp that the player has according to this snapshot
         *
         * @param boostedExp A positive, zero inclusive amount of boosted exp that this snapshot should report the player having
         */
        void setBoostedExp(int boostedExp) {
            this.boostedExp = Math.max(0, boostedExp);
        }

        /**
         * Gets the percentage to debuff the player's exp gain by according to this snapshot
         *
         * @return The percentage to debuff the player's exp gain by according to this snapshot
         */
        public double getDivineEscapeExpDebuff() {
            return divineEscapeExpDebuff;
        }

        /**
         * Sets the percentage to debuff the player's exp gain by according to this snapshot
         *
         * @param divineEscapeExpDebuff The percentage to debuff the player's exp gain by according to this snapshot
         */
        void setDivineEscapeExpDebuff(double divineEscapeExpDebuff) {
            this.divineEscapeExpDebuff = divineEscapeExpDebuff;
        }

        /**
         * Gets the percentage to debuff the player's damage output by according to this snapshot
         *
         * @return The percentage to debuff the player's damage output by according to this snapshot
         */
        public double getDivineEscapeDamageDebuff() {
            return divineEscapeDamageDebuff;
        }

        /**
         * Sets the percentage to debuff the player's damage output by according to this snapshot
         *
         * @param divineEscapeDamageDebuff The percentage to debuff the player's damage output by according to this snapshot
         */
        void setDivineEscapeDamageDebuff(double divineEscapeDamageDebuff) {
            this.divineEscapeDamageDebuff = divineEscapeDamageDebuff;
        }

        /**
         * Gets the time in millis that the player's exp debuff ends for this snapshot
         *
         * @return The time in millis that the player's exp debuff ends for this snapshot
         */
        public long getDivineEscapeExpEndTime() {
            return divineEscapeExpEndTime;
        }

        /**
         * Gets the time in millis that the player's exp debuff ends for this snapshot
         *
         * @param divineEscapeExpEndTime The time in millis that the player's exp debuff ends for this snapshot
         */
        void setDivineEscapeExpEndTime(long divineEscapeExpEndTime) {
            this.divineEscapeExpEndTime = divineEscapeExpEndTime;
        }

        /**
         * Gets the time in millis that the player's damage debuff ends for this snapshot
         *
         * @return The time in millis that the player's damage debuff ends for this snapshot
         */
        public long getDivineEscapeDamageEndTime() {
            return divineEscapeDamageEndTime;
        }

        /**
         * Gets the time in millis that the player's damage debuff ends for this snapshot
         *
         * @param divineEscapeDamageEndTime The time in millis that the player's damage debuff ends for this snapshot
         */
        void setDivineEscapeDamageEndTime(long divineEscapeDamageEndTime) {
            this.divineEscapeDamageEndTime = divineEscapeDamageEndTime;
        }

    }
}
