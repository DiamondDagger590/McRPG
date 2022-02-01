package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.attributes.AbilityAttribute;
import us.eunoians.mcrpg.abilities.attributes.AbilityAttributeManager;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
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
                                String abilityToggledOffQuery = "MERGE INTO " + ABILITY_TOGGLED_OFF_TABLE_NAME + " SELECT uuid as player_uuid, '" + abilityType.getName() + "' as ability_id FROM " + legacyTableName + " WHERE is_" + abilityDatabaseName + "_toggled = 0";

                                queries.add(abilityToggledOffQuery);
                            }

                            String abilityAttributeQuery = "MERGE INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " SELECT ";
                            boolean first = true; //Handle a column count mismatch issue for the union statements

                            //Ability attribute queries
                            for (UnlockedAbilities abilityType : skillType.getUnlockedAbilities()) {

                                String abilityDatabaseName = abilityType.getDatabaseName();
                                String abilityValueName = abilityType.getName();

                                String tierColumnName = abilityDatabaseName + "_tier";
                                String pendingColumnName = "is_" + abilityDatabaseName + "_pending";

                                abilityAttributeQuery += ((first ? "" : "(SELECT ") + "uuid as player_uuid, '" + abilityValueName + "' as ability_id, 'tier' as key, " + tierColumnName + " as value FROM " + legacyTableName + " WHERE " + tierColumnName + " > 0" + (first ? "" : ")") + " UNION ALL ");
                                abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityValueName + "' as ability_id, 'pending_status' as key, " + pendingColumnName + " as value FROM " + legacyTableName + " WHERE " + pendingColumnName + " = 1) UNION ALL ");

                                if (abilityType.isCooldown()) {
                                    String cooldownColumnName = abilityDatabaseName + "_cooldown";
                                    abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityValueName + "' as ability_id, 'cooldown' as key, " + cooldownColumnName + " as value FROM " + legacyTableName + " WHERE " + cooldownColumnName + " > 0) UNION ALL ");
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

    /**
     * Gets all information that can be provided through this DAO and returns one single {@link SkillDataSnapshot} containing all the information requested.
     *
     * This should be where most developers get the skill or ability information, unless they desire specific information for which they can use the specific individual method for.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillType  The {@link Skills} to get all data for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, with all the data this DAO can provide. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAllPlayerSkillInformation(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Skills skillType) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            getPlayerSkillLevelingData(connection, uuid, skillType)
                    .thenCompose(skillDataSnapshot -> getPlayerAbilityToggles(connection, uuid, skillDataSnapshot))
                    .thenCompose(skillDataSnapshot -> getAbilityAttributes(connection, uuid, skillDataSnapshot))
                    .thenAccept(completableFuture::complete)
                    .exceptionally(throwable -> {
                        completableFuture.completeExceptionally(throwable);
                        return null;
                    });


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

                    String databaseName = genericAbility.getName();
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

    /**
     * Gets all stored {@link AbilityAttribute}s that belong to abilities for a specific player's skill.
     *
     * @param connection        The {@link Connection} to use to run the query
     * @param uuid              The {@link UUID} of the player to get the data for
     * @param skillDataSnapshot The {@link SkillDataSnapshot} to populate with ability attributes
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the ability attributes of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAbilityAttributes(@NotNull Connection connection, @NotNull UUID uuid, SkillDataSnapshot skillDataSnapshot) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();
        AbilityAttributeManager abilityAttributeManager = McRPG.getInstance().getAbilityAttributeManager();
        Skills skillType = skillDataSnapshot.getSkillType();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT key, value FROM " + ABILITY_ATTRIBUTE_TABLE_NAME + " WHERE player_uuid = ? AND ability_id = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                //Get data for all abilities
                for (GenericAbility genericAbility : skillType.getAllAbilities()) {

                    String databaseName = genericAbility.getName();
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

    /**
     * Saves all the player skill data for the provided {@link McRPGPlayer}.
     * <p>
     * This method calls {@link #savePlayerSkillData(Connection, McRPGPlayer)}, {@link #savePlayerAbilityToggles(Connection, McRPGPlayer)} and {@link #savePlayerAbilityAttributes(Connection, McRPGPlayer)}
     * and should serve as a generic save all method.
     * <p>
     * To save specific information about a player and their skills, developers should call the specific methods that save the information they desire.
     *
     * @param connection  The {@link Connection} to use to save the player skill data
     * @param mcRPGPlayer The {@link McRPGPlayer} whose skill data are being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> saveAllPlayerSkillInformation(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            savePlayerSkillData(connection, mcRPGPlayer)
                    .thenCompose(unused -> savePlayerAbilityToggles(connection, mcRPGPlayer))
                    .thenCompose(unused -> savePlayerAbilityAttributes(connection, mcRPGPlayer))
                    .thenAccept(completableFuture::complete)
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });

        });
        return completableFuture;
    }

    /**
     * Saves the various {@link AbilityAttribute}s related to all abilities for the provided {@link McRPGPlayer}.
     *
     * @param connection  The {@link Connection} to use to save the {@link AbilityAttribute} information
     * @param mcRPGPlayer The {@link McRPGPlayer} whose {@link AbilityAttribute}s are being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerAbilityAttributes(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        UUID playerUUID = mcRPGPlayer.getUuid();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try {

                connection.setAutoCommit(false);

                try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + ABILITY_ATTRIBUTE_TABLE_NAME + " WHERE player_uuid = ?;")) {

                    deleteStatement.setString(1, playerUUID.toString());
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " (player_uuid, ability_id, key, value) VALUES(?, ?, ?, ?);")) {

                    preparedStatement.setString(1, playerUUID.toString());

                    for (Skills skillType : Skills.values()) {

                        for (UnlockedAbilities abilityType : skillType.getUnlockedAbilities()) {
                            BaseAbility baseAbility = mcRPGPlayer.getBaseAbility(abilityType);
                            preparedStatement.setString(2, abilityType.getName());

                            //TODO this is a sloppy hard coded solution. The recode will have attributes inside of ability classes in which we can store/pull values
                            if (baseAbility.getCurrentTier() > 0) {
                                Optional<AbilityAttribute<?>> attributeOptional = McRPG.getInstance().getAbilityAttributeManager().getAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);

                                if (attributeOptional.isPresent()) {
                                    AbilityAttribute<?> abilityAttribute = attributeOptional.get();
                                    preparedStatement.setString(3, abilityAttribute.getDatabaseKeyName());
                                    preparedStatement.setString(4, Integer.toString(baseAbility.getCurrentTier()));

                                    preparedStatement.executeUpdate();
                                }
                            }

                            if (mcRPGPlayer.getCooldown(abilityType) > 0) {
                                Optional<AbilityAttribute<?>> attributeOptional = McRPG.getInstance().getAbilityAttributeManager().getAttribute(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY);

                                if (attributeOptional.isPresent()) {
                                    AbilityAttribute<?> abilityAttribute = attributeOptional.get();
                                    preparedStatement.setString(3, abilityAttribute.getDatabaseKeyName());
                                    preparedStatement.setString(4, Long.toString(mcRPGPlayer.getCooldown(abilityType)));

                                    preparedStatement.executeUpdate();
                                }
                            }

                            if (mcRPGPlayer.getPendingUnlockAbilities().contains(abilityType)) {
                                Optional<AbilityAttribute<?>> attributeOptional = McRPG.getInstance().getAbilityAttributeManager().getAttribute(AbilityAttributeManager.ABILITY_PENDING_ATTRIBUTE_KEY);

                                if (attributeOptional.isPresent()) {
                                    AbilityAttribute<?> abilityAttribute = attributeOptional.get();
                                    preparedStatement.setString(3, abilityAttribute.getDatabaseKeyName());
                                    preparedStatement.setString(4, Boolean.toString(true));

                                    preparedStatement.executeUpdate();
                                }
                            }
                        }
                    }
                }

                connection.commit();
                connection.setAutoCommit(true);

                completableFuture.complete(null);
            }
            catch (SQLException e) {

                //If there is an error, attempt to rollback and set auto commit to true
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    completableFuture.completeExceptionally(ex);
                }

                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }

    /**
     * Saves the toggled off state of all abilities for the provided {@link McRPGPlayer}. Any abilities that are toggled on will not be saved here,
     * as an absence from the database indicates a toggled on state. This design choice is simply because most players will rarely if ever toggle off abilities,
     * so this helps better manage the size of the database by keeping out needless information.
     *
     * @param connection  The {@link Connection} to use to save the ability toggles
     * @param mcRPGPlayer The {@link McRPGPlayer} whose ability toggles are being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerAbilityToggles(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try {

                connection.setAutoCommit(false);

                //Remove existing values
                try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + ABILITY_TOGGLED_OFF_TABLE_NAME + " WHERE player_uuid = ?;")) {
                    preparedStatement.setString(1, mcRPGPlayer.getUuid().toString());
                    preparedStatement.executeUpdate();
                }

                try (PreparedStatement abilityToggledOffStatement = connection.prepareStatement("INSERT INTO " + ABILITY_TOGGLED_OFF_TABLE_NAME + " (player_uuid, ability_id) VALUES(?, ?)")) {

                    abilityToggledOffStatement.setString(1, mcRPGPlayer.getUuid().toString());

                    for (Skills skillType : Skills.values()) {
                        for (GenericAbility genericAbility : skillType.getAllAbilities()) {
                            if (!mcRPGPlayer.getBaseAbility(genericAbility).isToggled()) {
                                abilityToggledOffStatement.setString(2, genericAbility.getName());
                            }
                        }
                    }
                }

                connection.commit();
                connection.setAutoCommit(true);

                completableFuture.complete(null);
            }
            catch (SQLException e) {

                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    completableFuture.completeExceptionally(ex);
                }

                completableFuture.completeExceptionally(e);
            }

        });

        return completableFuture;
    }

    /**
     * Saves skill specific information such as exp for the provided {@link McRPGPlayer}.
     *
     * @param connection  The {@link Connection} to use to save the skill information
     * @param mcRPGPlayer The {@link McRPGPlayer} whose skill information is being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerSkillData(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement skillDataStatement = connection.prepareStatement("INSERT INTO " + SKILL_DATA_TABLE_NAME + " (player_uuid, skill_id, current_level, current_exp) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                                                                                    "current_level=VALUES(current_level), current_exp=VALUES(current_exp);")) {
                for (Skills skillType : Skills.values()) {

                    Skill skill = mcRPGPlayer.getSkill(skillType);
                    int currentExp = skill.getCurrentExp();
                    int currentLevel = skill.getCurrentLevel();

                    skillDataStatement.setString(1, skillType.getName().toLowerCase(Locale.ROOT));
                    skillDataStatement.setInt(2, currentLevel);
                    skillDataStatement.setInt(3, currentExp);

                    skillDataStatement.executeUpdate();
                }

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

}
