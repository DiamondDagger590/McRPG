package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.OptionalAbilityAttribute;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.exception.database.AbilityDatabaseNameException;
import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * A DAO used to store data regarding a player's specific data that doesn't really belong in another table
 */
public class SkillDAO {

    private static final Logger LOGGER = McRPG.getInstance().getLogger();

    private static final String SKILL_DATA_TABLE_NAME = "mcrpg_skill_data";
    private static final String ABILITY_TOGGLED_OFF_TABLE_NAME = "mcrpg_toggled_off_abilities";
    private static final String ABILITY_ATTRIBUTE_TABLE_NAME = "mcrpg_ability_attributes";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;
    private static final Set<NamespacedKey> LEGACY_ABILITY_ATTRIBUTES = Set.of(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);

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
                } catch (SQLException e) {
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
                        "`value` varchar(4096) NOT NULL," +
                        "PRIMARY KEY (`player_uuid`, `ability_id`, `key`)" +
                        ");")) {
                    statement.executeUpdate();
                } catch (SQLException e) {
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

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Update skill data table
            TableVersionHistoryDAO.getLatestVersion(connection, SKILL_DATA_TABLE_NAME).thenAccept(lastStoredVersion -> {

                        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                            return;
                        }

                        isAcceptingQueries = false;

                        //Adds table to our tracking
                        if (lastStoredVersion == 0) {

                            List<String> queries = new ArrayList<>();

                            for (NamespacedKey skillKey : skillRegistry.getRegisteredSkillKeys()) {
                                Skill skill = skillRegistry.getRegisteredSkill(skillKey);

                                String skillDatabaseName = skillKey.getKey().toLowerCase(Locale.ROOT);
                                String legacyTableName = "mcrpg_" + skillDatabaseName + "_data";

                                //Ensure legacy tables exist
                                if (databaseManager.getDatabase() != null && !databaseManager.getDatabase().tableExists(legacyTableName)) {
                                    continue;
                                }

                                //Skill data query
                                String skillInfoQuery = "MERGE INTO " + SKILL_DATA_TABLE_NAME + " SELECT uuid as player_uuid, '" + skillDatabaseName + "' as skill_id, current_level, current_exp FROM " + legacyTableName + ";";
                                queries.add(skillInfoQuery);

                                String abilityAttributeQuery = "MERGE INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " SELECT ";
                                boolean first = true; //Handle a column count mismatch issue for the union statements

                                //Ability toggled off queries
                                for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skillKey)) {
                                    Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);

                                    String abilityDatabaseName;
                                    String abilityName;
                                    if (ability.getDatabaseName().isPresent()) {
                                        abilityDatabaseName = ability.getDatabaseName().get();
                                        abilityName = abilityDatabaseName;
                                    } else if (ability.getLegacyName().isPresent()) {
                                        abilityName = ability.getLegacyName().get();
                                        abilityDatabaseName = getLegacyDatabaseName(abilityName);
                                    } else {
                                        throw new AbilityDatabaseNameException(ability);
                                    }

                                    if (ability.getApplicableAttributes().contains(AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY)) {
                                        String abilityToggledOffQuery = "MERGE INTO " + ABILITY_TOGGLED_OFF_TABLE_NAME + " SELECT uuid as player_uuid, '" + abilityName + "' as ability_id FROM " + legacyTableName + " WHERE is_" + abilityDatabaseName + "_toggled = 0";
                                        queries.add(abilityToggledOffQuery);
                                    }

                                    if (ability.getApplicableAttributes().containsAll(LEGACY_ABILITY_ATTRIBUTES)) {
                                        String tierColumnName = abilityDatabaseName + "_tier";
                                        String pendingColumnName = "is_" + abilityDatabaseName + "_pending";

                                        abilityAttributeQuery += ((first ? "" : "(SELECT ") + "uuid as player_uuid, '" + abilityName + "' as ability_id, 'tier' as key, " + tierColumnName + " as value FROM " + legacyTableName + " WHERE " + tierColumnName + " > 0" + (first ? "" : ")") + " UNION ALL ");
                                        abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityName + "' as ability_id, 'pending_status' as key, " + pendingColumnName + " as value FROM " + legacyTableName + " WHERE " + pendingColumnName + " = 1) UNION ALL ");

                                        if (ability.getApplicableAttributes().contains(AbilityAttributeManager.ABILITY_COOLDOWN_ATTRIBUTE_KEY)) {
                                            String cooldownColumnName = abilityDatabaseName + "_cooldown";
                                            abilityAttributeQuery += ("(SELECT uuid as player_uuid, '" + abilityName + "' as ability_id, 'cooldown' as key, " + cooldownColumnName + " as value FROM " + legacyTableName + " WHERE " + cooldownColumnName + " > 0) UNION ALL ");
                                        }

                                        if (first) {
                                            first = false;
                                        }
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
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    completableFuture.completeExceptionally(e);
                                    return;
                                }
                            }

                            TableVersionHistoryDAO.setTableVersion(connection, SKILL_DATA_TABLE_NAME, 1);
                            lastStoredVersion = 1;
                        } else if (lastStoredVersion == 1) {
                            //TODO update how ability toggles are handled
                        }

                        isAcceptingQueries = true;

                    })
                    .thenAccept(unused -> {

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
                    })
                    .thenAccept(unused -> {
                        //Once skill data is finished, then we attempt to update the ability toggled off table
                        TableVersionHistoryDAO.getLatestVersion(connection, ABILITY_TOGGLED_OFF_TABLE_NAME).thenAccept(lastStoredVersion -> {

                            if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                                return;
                            }

                            isAcceptingQueries = false;

                            if (lastStoredVersion == 0 || lastStoredVersion == 1) {
                                // TODO remove after Verdux upgrades
                                if (databaseManager.getDatabase() != null && databaseManager.getDatabase().tableExists("ABILITY_TOGGLED_OFF_TABLE_NAME")) {
                                    try (PreparedStatement preparedStatement = connection.prepareStatement("MERGE INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " SELECT player_uuid, ability_id, " + AbilityAttributeManager.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY + " as key, false as value FROM " + ABILITY_TOGGLED_OFF_TABLE_NAME)) {
                                        preparedStatement.executeUpdate();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        completableFuture.completeExceptionally(e);
                                    }
                                }
                                TableVersionHistoryDAO.setTableVersion(connection, ABILITY_TOGGLED_OFF_TABLE_NAME, 2);
                                lastStoredVersion = 2;
                            }

                            isAcceptingQueries = true;

                        });
                    });

        });

        return completableFuture;
    }

    /**
     * Gets all information that can be provided through this DAO and returns one single {@link SkillDataSnapshot} containing all the information requested.
     * <p>
     * This should be where most developers get the skill or ability information, unless they desire specific information for which they can use the specific individual method for.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillKey   The {@link NamespacedKey} of the {@link us.eunoians.mcrpg.skill.Skill} to get all data for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, with all the data this DAO can provide. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAllPlayerSkillInformation(@NotNull Connection connection, @NotNull UUID uuid, @NotNull NamespacedKey skillKey) {

        McRPG mcRPG = McRPG.getInstance();
        SkillRegistry skillRegistry = mcRPG.getSkillRegistry();

        if (!skillRegistry.isSkillRegistered(skillKey)) {
            throw new SkillNotRegisteredException(skillKey);
        }

        McRPGDatabaseManager databaseManager = mcRPG.getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            getPlayerSkillLevelingData(connection, uuid, skillKey)
                    .thenCompose(updatedSnapshot -> getAbilityAttributes(connection, uuid, updatedSnapshot)
                            .thenAccept(completableFuture::complete))
                    .exceptionally(throwable -> {
                        completableFuture.completeExceptionally(throwable);
                        return null;
                    });
        });


        return completableFuture;
    }

    /**
     * Gets the player leveling information for a specific player's skill. This method calls {@link #getPlayerSkillLevelingData(Connection, UUID, SkillDataSnapshot)},
     * providing an empty {@link SkillDataSnapshot} with only the provided {@link UUID} and {@link NamespacedKey}.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillKey   The {@link NamespacedKey} to get the skill leveling data for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the exp and level of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerSkillLevelingData(@NotNull Connection connection, @NotNull UUID uuid, @NotNull NamespacedKey skillKey) {
        return getPlayerSkillLevelingData(connection, uuid, new SkillDataSnapshot(uuid, skillKey));
    }

    /**
     * Gets the player leveling information for a specific player's skill. This method accepts a {@link SkillDataSnapshot} which will be updated utilizing
     * {@link SkillDataSnapshot#setCurrentExp(int)} and {@link SkillDataSnapshot#setCurrentLevel(int)}.
     * <p>
     * The skill that will have data obtained for it will be obtained from {@link SkillDataSnapshot#getSkillKey()} ()}.
     *
     * @param connection        The {@link Connection} to use to run the query
     * @param uuid              The {@link UUID} of the player to get the data for
     * @param skillDataSnapshot The {@link SkillDataSnapshot} to update
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the exp and level of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerSkillLevelingData(@NotNull Connection connection, @NotNull UUID uuid, @NotNull SkillDataSnapshot skillDataSnapshot) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT current_level, current_exp FROM " + SKILL_DATA_TABLE_NAME + " WHERE player_uuid = ? AND skill_id = ?;")) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, skillDataSnapshot.getSkillKey().getKey().toLowerCase(Locale.ROOT));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {
                        int currentExp = resultSet.getInt("current_exp");
                        int currentLevel = resultSet.getInt("current_level");

                        skillDataSnapshot.setCurrentExp(currentExp);
                        skillDataSnapshot.setCurrentLevel(currentLevel);
                    }

                    completableFuture.complete(skillDataSnapshot);
                }
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

        });

        return completableFuture;
    }

    /**
     * Gets all stored {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute AbilityAttributes} that belong to abilities for a specific player's skill.
     * This method creates a new {@link SkillDataSnapshot} object to populate.
     *
     * @param connection The {@link Connection} to use to run the query
     * @param uuid       The {@link UUID} of the player to get the data for
     * @param skillKey   The {@link NamespacedKey} to get the ability attributes for
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the ability attributes of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAbilityAttributes(@NotNull Connection connection, @NotNull UUID uuid, @NotNull NamespacedKey skillKey) {
        return getAbilityAttributes(connection, uuid, new SkillDataSnapshot(uuid, skillKey));
    }

    /**
     * Gets all stored {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute}s that belong to abilities for a specific player's skill.
     *
     * @param connection        The {@link Connection} to use to run the query
     * @param uuid              The {@link UUID} of the player to get the data for
     * @param skillDataSnapshot The {@link SkillDataSnapshot} to populate with ability attributes
     * @return A {@link CompletableFuture} that contains the provided {@link SkillDataSnapshot}, updated with the ability attributes of the desired skill. If
     * there is an error, the {@link CompletableFuture} will instead complete with the {@link SQLException}.
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getAbilityAttributes(@NotNull Connection connection, @NotNull UUID uuid, @NotNull SkillDataSnapshot skillDataSnapshot) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();
        AbilityAttributeManager abilityAttributeManager = McRPG.getInstance().getAbilityAttributeManager();
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT key, value FROM " + ABILITY_ATTRIBUTE_TABLE_NAME + " WHERE player_uuid = ? AND ability_id = ?;")) {

                if (!abilityRegistry.doesSkillHaveAbilities(skillDataSnapshot.getSkillKey())) {
                    completableFuture.complete(skillDataSnapshot);
                    return;
                }

                NamespacedKey skillKey = skillDataSnapshot.getSkillKey();

                preparedStatement.setString(1, uuid.toString());

                //Get data for all abilities
                for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skillKey)) {
                    // Add default attributes in case new attributes have been added
                    skillDataSnapshot.addDefaultAttributes(abilityRegistry.getRegisteredAbility(abilityKey));

                    String databaseName = abilityKey.getKey();
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
                                skillDataSnapshot.addAttribute(abilityKey, returnValue);
                            }
                        }
                    }
                }

                completableFuture.complete(skillDataSnapshot);

            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

    /**
     * Saves all the player skill data for the provided {@link SkillHolder}.
     * <p>
     * This method calls {@link #savePlayerSkillData(Connection, SkillHolder)} and {@link #savePlayerAbilityAttributes(Connection, SkillHolder)}
     * and should serve as a generic save all method.
     * <p>
     * To save specific information about a player and their skills, developers should call the specific methods that save the information they desire.
     *
     * @param connection  The {@link Connection} to use to save the player skill data
     * @param skillHolder The {@link SkillHolder} whose skill data are being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> saveAllSkillHolderInformation(@NotNull Connection connection, @NotNull SkillHolder skillHolder) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            CompletableFuture.allOf(savePlayerSkillData(connection, skillHolder),
                            savePlayerAbilityAttributes(connection, skillHolder))
                    .thenAccept(completableFuture::complete)
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });

        });
        return completableFuture;
    }

    /**
     * Saves the various {@link AbilityAttribute}s related to all abilities for the provided {@link SkillHolder}.
     *
     * @param connection  The {@link Connection} to use to save the {@link AbilityAttribute} information
     * @param skillHolder The {@link SkillHolder} whose {@link AbilityAttribute}s are being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerAbilityAttributes(@NotNull Connection connection, @NotNull SkillHolder skillHolder) {

        McRPG mcRPG = McRPG.getInstance();
        McRPGDatabaseManager databaseManager = mcRPG.getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        UUID playerUUID = skillHolder.getUUID();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try {
                connection.setAutoCommit(false);

                try (PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + ABILITY_ATTRIBUTE_TABLE_NAME + " (player_uuid, ability_id, key, value) VALUES(?, ?, ?, ?);")) {
                    preparedStatement.setString(1, playerUUID.toString());

                    AbilityRegistry abilityRegistry = mcRPG.getAbilityRegistry();
                    AbilityAttributeManager abilityAttributeManager = mcRPG.getAbilityAttributeManager();
                    PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + ABILITY_ATTRIBUTE_TABLE_NAME + " WHERE player_uuid = ? AND ability_id = ? AND key = ?;");
                    deleteStatement.setString(1, playerUUID.toString());

                    // Go through all registered abilities
                    for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
                        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(abilityKey);
                        // If the ability is stored inside the skill holder
                        if (abilityDataOptional.isPresent()) {
                            AbilityData abilityData = abilityDataOptional.get();
                            // Go through all attribute keys for this ability
                            for (NamespacedKey abilityAttributeKey : abilityData.getAllAttributeKeys()) {
                                Optional<AbilityAttribute<?>> abilityAttributeOptional = abilityData.getAbilityAttribute(abilityAttributeKey);
                                // If the attribute is registered
                                if (abilityAttributeOptional.isPresent()) {
                                    AbilityAttribute<?> abilityAttribute = abilityAttributeOptional.get();
                                    // If the ability attribute is an optional type, then that means we need to check if the data needs to be saved or deleted
                                    if (abilityAttribute instanceof OptionalAbilityAttribute<?> optionalAbilityAttribute && !optionalAbilityAttribute.shouldContentBeSaved()) {
                                        deleteStatement.setString(2, abilityData.getAbilityKey().value());
                                        deleteStatement.setString(3, abilityAttribute.getDatabaseKeyName());
                                        deleteStatement.execute();
                                    } else {
                                        preparedStatement.setString(2, abilityData.getAbilityKey().value());
                                        preparedStatement.setString(3, abilityAttribute.getDatabaseKeyName());
                                        preparedStatement.setString(4, abilityAttribute.getContent().toString());
                                        preparedStatement.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
                connection.commit();
                connection.setAutoCommit(true);
                completableFuture.complete(null);
            } catch (SQLException e) {
                //If there is an error, attempt to rollback and set auto commit to true
                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    completableFuture.completeExceptionally(ex);
                }

                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

    /**
     * Saves {@link Skill} specific information such as exp for the provided {@link SkillHolder} for all skills.
     *
     * @param connection  The {@link Connection} to use to save the skill information
     * @param skillHolder The {@link SkillHolder} whose skill information is being saved
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerSkillData(@NotNull Connection connection, @NotNull SkillHolder skillHolder) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement skillDataStatement = connection.prepareStatement("REPLACE INTO " + SKILL_DATA_TABLE_NAME + " (player_uuid, skill_id, current_level, current_exp) VALUES (?, ?, ?, ?);")) {
                skillDataStatement.setString(1, skillHolder.getUUID().toString());
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + SKILL_DATA_TABLE_NAME + " WHERE player_uuid = ? AND skill_id = ?");
                deleteStatement.setString(1, skillHolder.getUUID().toString());
                SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();

                for (NamespacedKey skillKey : skillRegistry.getRegisteredSkillKeys()) {
                    Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skillKey);
                    if (skillHolderDataOptional.isPresent()) {
                        SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                        // If there isnt anything to store in the db... clean it
                        if (skillHolderData.getCurrentExperience() == 0 && skillHolderData.getCurrentLevel() == 0) {
                            deleteStatement.setString(2, skillKey.value());
                            deleteStatement.executeUpdate();
                        } else {
                            skillDataStatement.setString(2, skillKey.value());
                            skillDataStatement.setInt(3, skillHolderData.getCurrentLevel());
                            skillDataStatement.setInt(4, skillHolderData.getCurrentExperience());
                            skillDataStatement.executeUpdate();
                        }
                    }
                }
                completableFuture.complete(null);
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

    /**
     * Saves skill specific information such as exp for the provided {@link SkillHolder} for the {@link Skill} associated with the provided {@link NamespacedKey}.
     *
     * @param connection  The {@link Connection} to use to save the skill information
     * @param skillHolder The {@link SkillHolder} whose skill information is being saved
     * @param skillKey    The {@link NamespacedKey} for the skill to save
     * @return A {@link CompletableFuture} that completes whenever the save has finished or completes with an {@link SQLException} if there
     * is an error with saving
     */
    @NotNull
    public static CompletableFuture<Void> savePlayerSkillData(@NotNull Connection connection, @NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement skillDataStatement = connection.prepareStatement("REPLACE INTO " + SKILL_DATA_TABLE_NAME + " (player_uuid, skill_id, current_level, current_exp) VALUES (?, ?, ?, ?);")) {
                skillDataStatement.setString(1, skillHolder.getUUID().toString());
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + SKILL_DATA_TABLE_NAME + " WHERE player_uuid = ? AND skill_id = ?");
                deleteStatement.setString(1, skillHolder.getUUID().toString());
                SkillRegistry skillRegistry = McRPG.getInstance().getSkillRegistry();
                Optional<SkillHolder.SkillHolderData> skillHolderDataOptional = skillHolder.getSkillHolderData(skillKey);
                if (skillHolderDataOptional.isPresent()) {
                    SkillHolder.SkillHolderData skillHolderData = skillHolderDataOptional.get();
                    // If there isnt anything to store in the db... clean it
                    if (skillHolderData.getCurrentExperience() == 0 && skillHolderData.getCurrentLevel() == 0) {
                        deleteStatement.setString(2, skillKey.value());
                        deleteStatement.executeUpdate();
                    } else {
                        skillDataStatement.setString(2, skillKey.value());
                        skillDataStatement.setInt(3, skillHolderData.getCurrentLevel());
                        skillDataStatement.setInt(4, skillHolderData.getCurrentExperience());
                        skillDataStatement.executeUpdate();
                    }
                }
                completableFuture.complete(null);
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }

//    /**
//     * Gets the player leaderboard rankings for the provided {@link Skills} skill type.
//     *
//     * @param connection The {@link Connection} to use to get the player leaderboard rankings
//     * @param skillType  The {@link Skills} skill type to use to get the player leaderboard rankings for
//     * @return A {@link CompletableFuture} completed with a {@link LeaderboardData} which stores all of the player rankings for the given {@link Skills} skill type or will
//     * be completed exceptionally with an {@link SQLException} if an error occurs.
//     */
//    @NotNull
//    public static CompletableFuture<LeaderboardData> getPlayerLeaderboardRankings(@NotNull Connection connection, @NotNull Skills skillType) {
//
//        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
//        CompletableFuture<LeaderboardData> completableFuture = new CompletableFuture<>();
//
//        databaseManager.getDatabaseExecutorService().submit(() -> {
//
//            List<PlayerLeaderboardData> playerLeaderboardData = new ArrayList<>();
//            Map<UUID, Integer> playerRankings = new HashMap<>();
//
//            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT player_uuid, current_level FROM " + SKILL_DATA_TABLE_NAME + " WHERE skill_id = ? ORDER BY current_level DESC;")) {
//
//                preparedStatement.setString(1, skillType.getName().toLowerCase());
//
//                try (ResultSet resultSet = preparedStatement.executeQuery()) {
//
//                    while (resultSet.next()) {
//
//                        UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
//                        int level = resultSet.getInt("current_level");
//
//                        playerLeaderboardData.add(new PlayerLeaderboardData(uuid, level));
//                        playerRankings.put(uuid, playerLeaderboardData.size() + 1);
//                    }
//                }
//
//            }
//            catch (SQLException e) {
//                completableFuture.completeExceptionally(e);
//                return;
//            }
//
//            completableFuture.complete(new LeaderboardData(playerLeaderboardData, playerRankings));
//        });
//
//        return completableFuture;
//    }

//    /**
//     * Gets the player leaderboard power level rankings.
//     *
//     * @param connection The {@link Connection} to use to get the player leaderboard rankings
//     * @return A {@link CompletableFuture} completed with a {@link LeaderboardData} which stores all of the player power level rankings or will
//     * be completed exceptionally with an {@link SQLException} if an error occurs.
//     */
//    @NotNull
//    public static CompletableFuture<LeaderboardData> getPlayerPowerLeaderboardRankings(@NotNull Connection connection) {
//
//        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
//        CompletableFuture<LeaderboardData> completableFuture = new CompletableFuture<>();
//
//        databaseManager.getDatabaseExecutorService().submit(() -> {
//
//            List<PlayerLeaderboardData> playerLeaderboardData = new ArrayList<>();
//            Map<UUID, Integer> playerRankings = new HashMap<>();
//
//            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT player_uuid, SUM(current_level) AS total_level_sum FROM " + SKILL_DATA_TABLE_NAME + " GROUP BY player_uuid ORDER BY total_level_sum DESC;")) {
//
//                try (ResultSet resultSet = preparedStatement.executeQuery()) {
//
//                    while (resultSet.next()) {
//
//                        UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
//                        int powerLevel = resultSet.getInt("total_level_sum");
//
//                        playerLeaderboardData.add(new PlayerLeaderboardData(uuid, powerLevel));
//                        playerRankings.put(uuid, playerLeaderboardData.size() + 1);
//                    }
//                }
//
//            }
//            catch (SQLException e) {
//                completableFuture.completeExceptionally(e);
//                return;
//            }
//
//            completableFuture.complete(new LeaderboardData(playerLeaderboardData, playerRankings));
//
//        });
//
//        return completableFuture;
//    }

    private static String getLegacyDatabaseName(String legacyAbilityName) {

        char[] chars = legacyAbilityName.toCharArray();
        StringBuilder string = new StringBuilder();
        boolean first = true;

        for (char letter : chars) {

            //On the first letter, we don't need to add a `_`, so we skip
            if (!first) {
                //Check here for uppercase except on the first letter
                if (Character.isUpperCase(letter)) {
                    string.append("_");
                }
            } else {
                first = false;
            }

            if (letter == ' ') { //Why have I done this to myself
                continue;
            }

            string.append(letter == '+' ? "_plus" : letter);//Hardcode to handle Bleed+
        }
        return string.toString().toLowerCase(Locale.ROOT); //Lowercase it all
    }
}
