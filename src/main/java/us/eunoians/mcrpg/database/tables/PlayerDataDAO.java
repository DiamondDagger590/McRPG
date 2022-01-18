package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's specific data that doesn't really belong in another table
 *
 * @author DiamondDagger590
 */
public class PlayerDataDAO {

    private static final String TABLE_NAME = "mcrpg_player_data";
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
                                                                           "`id` int(11) NOT NULL AUTO_INCREMENT," +
                                                                           "`uuid` varchar(32) NOT NULL," +
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
     * Gets a {@link PlayerDataSnapshot} containing all of the player's data for misc information such as redeemable exp. If
     * the provided {@link UUID} doesn't have any data, an empty {@link PlayerDataSnapshot} will be returned instead with null or defaulted 0 values.
     *
     * @param connection The {@link Connection} to use to get the player data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link PlayerDataSnapshot} that has all of the player's misc
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link PlayerDataSnapshot} will be returned instead with null or defaulted 0 values.
     */
    public static CompletableFuture<PlayerDataSnapshot> getPlayerData(Connection connection, UUID uuid) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
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

        return completableFuture;    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerData(Connection connection, McRPGPlayer mcRPGPlayer) {
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


    public static class PlayerDataSnapshot{

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

        public PlayerDataSnapshot(@NotNull UUID uuid){
            this.uuid = uuid;
        }

        @NotNull
        public UUID getUuid() {
            return uuid;
        }

        @Nullable
        public UUID getPartyUUID() {
            return partyUUID;
        }

        public void setPartyUUID(@Nullable UUID partyUUID) {
            this.partyUUID = partyUUID;
        }

        public int getPowerLevel() {
            return powerLevel;
        }

        public void setPowerLevel(int powerLevel) {
            this.powerLevel = powerLevel;
        }

        public int getAbilityPoints() {
            return abilityPoints;
        }

        public void setAbilityPoints(int abilityPoints) {
            this.abilityPoints = abilityPoints;
        }

        public long getReplaceAbilityCooldownTime() {
            return replaceAbilityCooldownTime;
        }

        public void setReplaceAbilityCooldownTime(long replaceAbilityCooldownTime) {
            this.replaceAbilityCooldownTime = replaceAbilityCooldownTime;
        }

        public int getRedeemableExp() {
            return redeemableExp;
        }

        public void setRedeemableExp(int redeemableExp) {
            this.redeemableExp = redeemableExp;
        }

        public int getRedeemableLevels() {
            return redeemableLevels;
        }

        public void setRedeemableLevels(int redeemableLevels) {
            this.redeemableLevels = redeemableLevels;
        }

        public int getBoostedExp() {
            return boostedExp;
        }

        public void setBoostedExp(int boostedExp) {
            this.boostedExp = boostedExp;
        }

        public double getDivineEscapeExpDebuff() {
            return divineEscapeExpDebuff;
        }

        public void setDivineEscapeExpDebuff(double divineEscapeExpDebuff) {
            this.divineEscapeExpDebuff = divineEscapeExpDebuff;
        }

        public double getDivineEscapeDamageDebuff() {
            return divineEscapeDamageDebuff;
        }

        public void setDivineEscapeDamageDebuff(double divineEscapeDamageDebuff) {
            this.divineEscapeDamageDebuff = divineEscapeDamageDebuff;
        }

        public long getDivineEscapeExpEndTime() {
            return divineEscapeExpEndTime;
        }

        public void setDivineEscapeExpEndTime(long divineEscapeExpEndTime) {
            this.divineEscapeExpEndTime = divineEscapeExpEndTime;
        }

        public long getDivineEscapeDamageEndTime() {
            return divineEscapeDamageEndTime;
        }

        public void setDivineEscapeDamageEndTime(long divineEscapeDamageEndTime) {
            this.divineEscapeDamageEndTime = divineEscapeDamageEndTime;
        }

    }
}
