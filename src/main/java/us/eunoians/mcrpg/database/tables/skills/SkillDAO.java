package us.eunoians.mcrpg.database.tables.skills;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
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
 * @deprecated Instead working on implementing {@link us.eunoians.mcrpg.database.tables.SkillDAO} as a general solution
 * @author DiamondDagger590
 */
@Deprecated
public abstract class SkillDAO {

    /**
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for the provided {@link Skills}. If
     * the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param tableName  The name of the table to run the query against
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @param skillType  The {@link Skills} that is having data fetched for
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's provided {@link Skills}
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getSkillData(@NotNull String tableName, @NotNull Connection connection, @NotNull UUID uuid, @NotNull Skills skillType) {

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

    public static CompletableFuture<Void> savePlayerData(String tableName, Connection connection, McRPGPlayer mcRPGPlayer, Skills skillType) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<SkillDataSnapshot> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try {

                UUID uuid = mcRPGPlayer.getUuid();
                Skill skill = mcRPGPlayer.getSkill(skillType);

                //Update player data
                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (uuid, current_exp, current_level) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE current_exp=VALUES(current_exp), current_level(current_level);")) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setInt(2, skill.getCurrentExp());
                    preparedStatement.setInt(3, skill.getCurrentLevel());

                    preparedStatement.executeUpdate();
                }

                //Update default ability
                DefaultAbilities defaultAbility = skillType.getDefaultAbility();
                BaseAbility baseAbility = skill.getDefaultAbility();
                String defaultAbilitySQLName = "is_" + defaultAbility.getDatabaseName() + "_toggled";

                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + tableName + " (uuid, " + defaultAbilitySQLName + ") VALUES (?, ?) ON DUPLICATE KEY UPDATE " + defaultAbilitySQLName + "=VALUES(" + defaultAbilitySQLName + ");")) {

                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setBoolean(2, baseAbility.isToggled());

                    preparedStatement.executeUpdate();
                }

                //Unlocked abilities
                for (BaseAbility ability : skill.getAbilities()) {

                    //Ignore default abilities
                    if (ability.getGenericAbility() instanceof DefaultAbilities) {
                        continue;
                    }

                }
            }
            catch (SQLException e) {

            }

        });
        return null;
    }
}
