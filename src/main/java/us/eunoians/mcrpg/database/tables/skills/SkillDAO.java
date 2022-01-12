package us.eunoians.mcrpg.database.tables.skills;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class containing some shared queries among all DAO's that
 * store skill information.
 *
 * @author DiamondDagger590
 */
public abstract class SkillDAO {

    /**
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for the provided {@link Skills}. If
     * the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param tableName  The name of the table to run the query against
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player who's data is being obtained
     * @param skillType  The {@link Skills} that is having data fetched for
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's provided {@link Skills}
     * data. If the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    public static CompletableFuture<SkillDataSnapshot> getSkillData(String tableName, Connection connection, UUID uuid, Skills skillType) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            SkillDataSnapshot skillDAOWrapper = new SkillDataSnapshot(uuid, skillType);

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + tableName + " WHERE uuid = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {


                    while (resultSet.next()) {

                        int currentExp = resultSet.getInt("current_exp");
                        int currentLevel = resultSet.getInt("current_level");

                        skillDAOWrapper.setCurrentExp(currentExp);
                        skillDAOWrapper.setCurrentLevel(currentLevel);

                        //Default Ability
                        DefaultAbilities defaultAbility = skillType.getDefaultAbility();
                        skillDAOWrapper.addAbilityToggledData(defaultAbility, resultSet.getBoolean("is_" + defaultAbility.getDatabaseName() + "_toggled"));

                        //Unlocked Abilities
                        for (UnlockedAbilities ability : skillType.getUnlockedAbilities()) {
                            skillDAOWrapper.addAbilityData(ability, resultSet);
                        }
                    }

                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(skillDAOWrapper);

        });

        return completableFuture;
    }
}
