package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.attributes.AbilityAttribute;
import us.eunoians.mcrpg.abilities.attributes.AbilityAttributeManager;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's specific data that doesn't really belong in another table
 *
 * @author DiamondDagger590
 */
public class SkillDAO {

    private static final String SKILL_DATA_TABLE_NAME = "mcrpg_skill_data";
    private static final String ABILITY_TOGGLED_OFF_TABLE_NAME = "mcrpg_toggled_off_abilities";
    private static final String ABILITY_ATTRIBUTE_TABLE_NAME = "mcrpg_ability_attributes";
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
    @NotNull
    public static CompletableFuture<Boolean> attemptCreateTable(@NotNull Connection connection, @NotNull DatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            boolean skillDataTableExists = databaseManager.getDatabase().tableExists(SKILL_DATA_TABLE_NAME);
            boolean abilityToggledOffTableExists = databaseManager.getDatabase().tableExists(ABILITY_TOGGLED_OFF_TABLE_NAME);
            boolean abilityAttributeTableExists = databaseManager.getDatabase().tableExists(ABILITY_ATTRIBUTE_TABLE_NAME);

            //Check to see if the table already exists
            if (skillDataTableExists && abilityToggledOffTableExists && abilityAttributeTableExists) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

            if (!skillDataTableExists) {

                /*****
                 ** Table Description:
                 ** Contains general information that is common to all skills
                 *
                 *
                 * player_uuid is the {@link java.util.UUID} of the player being stored
                 * skill_id is the id of the {@link us.eunoians.mcrpg.types.Skills} that is being stored
                 * current_level is the current level of the player's skill that is being stored
                 * current_exp is the current amount of exp the player's skill that is being stored has
                 **
                 ** Reasoning for structure:
                 ** The composite key is the `player_uuid` field and the `skill_id` field, as there should only be one unique combination of the two per player and skill
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + SKILL_DATA_TABLE_NAME + "`" +
                                                                               "(" +
                                                                               "`player_uuid` varchar(36) NOT NULL," +
                                                                               "`skill_id` varchar(32) NOT NULL," +
                                                                               "`current_level` int(11) NOT NULL DEFAULT 0," +
                                                                               "`current_exp` int(11) NOT NULL DEFAULT 0," +
                                                                               "PRIMARY KEY (`player_uuid`, `skill_id`)" +
                                                                               ");")) {
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
            }

            if (!abilityToggledOffTableExists) {

                /*****
                 ** Table Description:
                 ** Contains all abilities that are toggled off
                 *
                 *
                 * player_uuid is the {@link java.util.UUID} of the player being stored
                 * ability_id is the id of the {@link us.eunoians.mcrpg.types.GenericAbility} that is being stored. (Note abilities in here could either be {@link us.eunoians.mcrpg.types.DefaultAbilities} or {@link us.eunoians.mcrpg.types.UnlockedAbilities}
                 **
                 ** Reasoning for structure:
                 ** The composite key is the `player_uuid` field and the `ability_id` field, as there should only be one unique combination of the two per player and ability
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + ABILITY_TOGGLED_OFF_TABLE_NAME + "`" +
                                                                               "(" +
                                                                               "`player_uuid` varchar(36) NOT NULL," +
                                                                               "`ability_id` varchar(32) NOT NULL," +
                                                                               "PRIMARY KEY (`player_uuid`, `ability_id`)" +
                                                                               ");")) {
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
            }

            if (!abilityAttributeTableExists) {

                /*****
                 ** Table Description:
                 ** Contains various attributes about different abilities
                 *
                 *
                 * player_uuid is the {@link java.util.UUID} of the player being stored
                 * ability_id is the id of the {@link us.eunoians.mcrpg.types.GenericAbility} that is being stored. (Note abilities in here could either be {@link us.eunoians.mcrpg.types.DefaultAbilities} or {@link us.eunoians.mcrpg.types.UnlockedAbilities}.
                 *      - Not all abilities will have data in here
                 * key is the key of an ability attribute to obtain
                 * value is a string value that is stored against the key. The reason it's MAX in size is that we don't know what sort of data 3rd party plugins may want to store in the future when support is added
                 **
                 ** Reasoning for structure:
                 ** The composite key is the `player_uuid` field, `key` field and the `ability_id` field, as there should only be one unique combination of those three per each ability, since each ability will only have a single attribute once at most
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + ABILITY_ATTRIBUTE_TABLE_NAME + "`" +
                                                                               "(" +
                                                                               "`player_uuid` varchar(36) NOT NULL," +
                                                                               "`ability_id` varchar(32) NOT NULL," +
                                                                               "`key` varchar(32) NOT NULL," +
                                                                               "`value` varchar(MAX) NOT NULL," +
                                                                               "PRIMARY KEY (`player_uuid`, `ability_id`, `key`)" +
                                                                               ");")) {
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
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

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (TableVersionHistoryDAO.isAcceptingQueries()) {

                //Update skill data table
                TableVersionHistoryDAO.getLatestVersion(connection, SKILL_DATA_TABLE_NAME).thenAccept(lastStoredVersion -> {

                    if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                        return;
                    }

                    isAcceptingQueries = false;

                    //Adds table to our tracking
                    if (lastStoredVersion == 0) {

                        List<String> queries = new ArrayList<>();
                        for (Skills skillType : Skills.values()) {

                            String skillDatabaseName = skillType.getName().toLowerCase(Locale.ROOT);
                            String legacyTableName = "mcrpg_" + skillDatabaseName + "_data";

                            //Skill data query
                            String skillInfoQuery = "MERGE INTO " + SKILL_DATA_TABLE_NAME + " SELECT uuid as player_uuid, '" + skillDatabaseName + "' as skill_id, current_level, current_exp FROM " + legacyTableName + ";";
                            queries.add(skillInfoQuery);

                            //Ability toggled off queries
                            for (GenericAbility abilityType : skillType.getAllAbilities()) {
                                String abilityDatabaseName = abilityType.getDatabaseName();
                                String abilityToggledOffQuery = "MERGE INTO " + ABILITY_TOGGLED_OFF_TABLE_NAME + " SELECT uuid as player_uuid, '" + abilityDatabaseName + "' as ability_id FROM " + legacyTableName + " WHERE is_" + abilityDatabaseName + "_toggled = 0";

                                queries.add(abilityToggledOffQuery);
                            }

                            String abilityAttributeQuery = "MERGE INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " SELECT ";
                            boolean first = true; //Handle a column count mismatch issue for the union statements

                            //Ability attribute queries
                            for (UnlockedAbilities abilityType : skillType.getUnlockedAbilities()) {

                                String abilityDatabaseName = abilityType.getDatabaseName();

                                String tierColumnName = abilityDatabaseName + "_tier";
                                String pendingColumnName = "is_" + abilityDatabaseName + "_pending";

                                abilityAttributeQuery += ((first ? "" : "(SELECT ") + "uuid as player_uuid, '" + abilityDatabaseName + "' as ability_id, 'tier' as key, " + tierColumnName + " as value FROM " + legacyTableName + " WHERE " + tierColumnName + " > 0" + (first ? "" : ")") + " UNION ALL ");
                                abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityDatabaseName + "' as ability_id, 'pending_status' as key, " + pendingColumnName + " as value FROM " + legacyTableName + " WHERE " + pendingColumnName + " = 1) UNION ALL ");

                                if (abilityType.isCooldown()) {
                                    String cooldownColumnName = abilityDatabaseName + "_cooldown";
                                    abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityDatabaseName + "' as ability_id, 'cooldown' as key, " + cooldownColumnName + " as value FROM " + legacyTableName + " WHERE " + cooldownColumnName + " > 0) UNION ALL ");
                                }

                                if (first) {
                                    first = false;
                                }
                            }

                            abilityAttributeQuery = abilityAttributeQuery.trim();
                            abilityAttributeQuery = abilityAttributeQuery.substring(0, abilityAttributeQuery.length() - 9); //Trim the trailing UNION ALL
                            abilityAttributeQuery += ";";

                            queries.add(abilityAttributeQuery);
                        }

                        //Execute queries
                        for (String query : queries) {

                            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                                preparedStatement.executeUpdate();
                            }
                            catch (SQLException e) {
                                e.printStackTrace();
                                completableFuture.completeExceptionally(e);
                                return;
                            }
                        }

                        TableVersionHistoryDAO.setTableVersion(connection, SKILL_DATA_TABLE_NAME, 1);
                        lastStoredVersion = 1;
                    }

                    isAcceptingQueries = true;

                }).thenAccept(unused -> {

                    //Once skill data is finished, then we attampt to update the ability toggled off table
                    TableVersionHistoryDAO.getLatestVersion(connection, ABILITY_TOGGLED_OFF_TABLE_NAME).thenAccept(lastStoredVersion -> {

                        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                            return;
                        }

                        isAcceptingQueries = false;

                        //Adds table to our tracking
                        if (lastStoredVersion == 0) {
                            TableVersionHistoryDAO.setTableVersion(connection, ABILITY_TOGGLED_OFF_TABLE_NAME, 1);
                            lastStoredVersion = 1;
                        }

                        isAcceptingQueries = true;

                    });
                }).thenAccept(unused -> {

                    //After both of those are done, we then attempt to update the ability attribute table
                    TableVersionHistoryDAO.getLatestVersion(connection, ABILITY_ATTRIBUTE_TABLE_NAME).thenAccept(lastStoredVersion -> {

                        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                            completableFuture.complete(null);
                            return;
                        }

                        isAcceptingQueries = false;

                        //Adds table to our tracking
                        if (lastStoredVersion == 0) {
                            TableVersionHistoryDAO.setTableVersion(connection, ABILITY_ATTRIBUTE_TABLE_NAME, 1);
                            lastStoredVersion = 1;
                        }

                        isAcceptingQueries = true;

                        completableFuture.complete(null);
                    });
                });
            }

        });

        return completableFuture;
    }

    //TODO
    @NotNull
    public static CompletableFuture<Void> getPlayerSkillData(@NotNull Connection connection, @NotNull UUID uuid) {

        //TODO
        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + SKILL_DATA_TABLE_NAME + " WHERE player_uuid = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                    }

                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(null);

        });

        return completableFuture;
    }

    /**
     * Gets the player leveling information for a specific player's skill. This method calls {@link #getPlayerSkillLevelingData(Connection, UUID, SkillDataSnapshot)},
     * providing an empty {@link SkillDataSnapshot} with only the provided {@link UUID} and {@link Skills}.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillType  The {@link Skills} to get the skill leveling data for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the exp and level of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerSkillLevelingData(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Skills skillType) {
        return getPlayerSkillLevelingData(connection, uuid, new SkillDataSnapshot(uuid, skillType));
    }

    /**
     * Gets the player leveling information for a specific player's skill. This method accepts a {@link SkillDataSnapshot} which will be updated utilizing
     * {@link SkillDataSnapshot#setCurrentExp(int)} and {@link SkillDataSnapshot#setCurrentLevel(int)}.
     * <p>
     * The skill that will have data obtained for it will be obtained from {@link SkillDataSnapshot#getSkillType()}.
     *
     * @param connection        The {@link Connection} to use to run the query
     * @param uuid              The {@link UUID} of the player to get the data for
     * @param skillDataSnapshot The {@link SkillDataSnapshot} to update
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the exp and level of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerSkillLevelingData(@NotNull Connection connection, @NotNull UUID uuid, @NotNull SkillDataSnapshot skillDataSnapshot) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT current_level, current_exp FROM " + SKILL_DATA_TABLE_NAME + " WHERE player_uuid = ? AND skill_id = ?;")) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, skillDataSnapshot.getSkillType().getName().toLowerCase(Locale.ROOT));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        int currentExp = resultSet.getInt("current_exp");
                        int currentLevel = resultSet.getInt("current_level");

                        skillDataSnapshot.setCurrentExp(currentExp);
                        skillDataSnapshot.setCurrentLevel(currentLevel);
                    }

                    completableFuture.complete(skillDataSnapshot);
                }
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

        });

        return completableFuture;
    }

    /**
     * Gets the ability toggled statuses for a specific player's skill. This method calls {@link #getPlayerAbilityToggles(Connection, UUID, SkillDataSnapshot)},
     * providing an empty {@link SkillDataSnapshot} with only the provided {@link UUID} and {@link Skills}.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillType  The {@link Skills} to get the ability toggle data for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the ability toggle data of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerAbilityToggles(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Skills skillType) {
        return getPlayerAbilityToggles(connection, uuid, new SkillDataSnapshot(uuid, skillType));
    }

    /**
     * Gets the ability toggled statuses for a specific player's skill. This method accepts a {@link SkillDataSnapshot} which will be updated utilizing
     * {@link SkillDataSnapshot#addAbilityToggledData(GenericAbility, boolean)}.
     * <p>
     * The skill that will have data obtained for it will be obtained from {@link SkillDataSnapshot#getSkillType()}.
     *
     * @param connection        The {@link Connection} to use to run the query
     * @param uuid              The {@link UUID} of the player to get the data for
     * @param skillDataSnapshot The {@link SkillDataSnapshot} to update
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the toggled statuses for all abilities in the specified skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerAbilityToggles(@NotNull Connection connection, @NotNull UUID uuid, @NotNull SkillDataSnapshot skillDataSnapshot) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ability_id FROM " + ABILITY_TOGGLED_OFF_TABLE_NAME + " WHERE player_uuid = ?")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        String abilityID = resultSet.getString("ability_id");

                        UnlockedAbilities unlockedAbility = UnlockedAbilities.fromString(abilityID);
                        GenericAbility genericAbility = unlockedAbility != null ? unlockedAbility : DefaultAbilities.getFromID(abilityID);

                        if (genericAbility != null) {
                            skillDataSnapshot.addAbilityToggledData(genericAbility, false);
                        }
                    }

                    //Populate toggled on abilities
                    Skills skillType = skillDataSnapshot.getSkillType();
                    Map<GenericAbility, Boolean> savedToggledOffAbilities = skillDataSnapshot.getAbilityToggledMap();

                    for (GenericAbility genericAbility : skillType.getAllAbilities()) {
                        if (!savedToggledOffAbilities.containsKey(genericAbility)) {
                            skillDataSnapshot.addAbilityToggledData(genericAbility, true);
                        }
                    }

                    completableFuture.complete(skillDataSnapshot);
                }
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }

    /**
     * Gets all stored {@link AbilityAttribute}s that belong to abilities for a specific player's skill. This method creates a new {@link SkillDataSnapshot} object
     * to populate.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillType  The {@link Skills} to get the ability attributes for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the ability attributes of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAbilityAttributes(@NotNull Connection connection, @NotNull UUID uuid, Skills skillType) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();
        AbilityAttributeManager abilityAttributeManager = McRPG.getInstance().getAbilityAttributeManager();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT key, value FROM " + ABILITY_ATTRIBUTE_TABLE_NAME + " WHERE player_uuid = ? AND ability_id = ?;")) {

                SkillDataSnapshot skillDataSnapshot = new SkillDataSnapshot(uuid, skillType);
                preparedStatement.setString(1, uuid.toString());

                //Get data for all abilities
                for (GenericAbility genericAbility : skillType.getAllAbilities()) {

                    String databaseName = genericAbility.getDatabaseName();
                    preparedStatement.setString(2, databaseName);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {

                        //Iterate over all found values
                        while (resultSet.next()) {

                            String attributeName = resultSet.getString("key");
                            String attributeValue = resultSet.getString("value");

                            Optional<AbilityAttribute<?>> attributeOptional = abilityAttributeManager.getAttribute(attributeName);
                            AbilityAttribute<?> returnValue;

                            if (attributeOptional.isPresent()) {
                                AbilityAttribute<?> abilityAttribute = attributeOptional.get();
                                returnValue = abilityAttribute.create(attributeValue);
                                skillDataSnapshot.addAttribute(genericAbility, returnValue);
                            }

                        }

                        completableFuture.complete(skillDataSnapshot);
                    }
                }

            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerSkill(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {
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

}
