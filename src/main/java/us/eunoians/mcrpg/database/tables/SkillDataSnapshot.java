package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class serves as a POJO wraps player's skill data into a singular object.
 * <p>
 * This allows developers to request information and be able to access it in a concise way. This snapshot is only created whenever
 * a player's data is obtained from a DAO and can not be modified past that point.
 *
 * @author DiamondDagger590
 */
public class SkillDataSnapshot {

    private final UUID uuid;
    private final Skills skillType;
    private int currentExp;
    private int currentLevel;
    private final Map<GenericAbility, Boolean> abilityToggledMap;
    private final Map<UnlockedAbilities, Integer> abilityTiers;
    private final Map<GenericAbility, Integer> abilityCooldowns;
    private final Map<UnlockedAbilities, Boolean> pendingAbilities;

    /**
     * Constructs a new {@link SkillDataSnapshot} with the values for {@link #getCurrentExp()} and {@link #getCurrentLevel()}
     * both being 0.
     *
     * @param uuid      The {@link UUID} of the player this snapshot is for
     * @param skillType The {@link Skills} which represents the {@link us.eunoians.mcrpg.skills.Skill} this snapshot has data for
     */
    SkillDataSnapshot(@NotNull UUID uuid, @NotNull Skills skillType) {
        this.uuid = uuid;
        this.skillType = skillType;
        this.currentExp = 0;
        this.currentLevel = 0;
        this.abilityToggledMap = new HashMap<>();
        this.abilityTiers = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.pendingAbilities = new HashMap<>();
    }

    /**
     * @param uuid         The {@link UUID} of the player this snapshot is for
     * @param skillType    The {@link Skills} which represents the {@link us.eunoians.mcrpg.skills.Skill} this snapshot has data for
     * @param currentExp   The amount of exp the skill currently has
     * @param currentLevel The current level of the skill
     */
    SkillDataSnapshot(@NotNull UUID uuid, @NotNull Skills skillType, int currentExp, int currentLevel) {
        this.uuid = uuid;
        this.skillType = skillType;
        this.currentExp = currentExp;
        this.currentLevel = currentLevel;
        this.abilityToggledMap = new HashMap<>();
        this.abilityTiers = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        this.pendingAbilities = new HashMap<>();
    }

    /**
     * Gets the {@link UUID} of the player this snapshot has data for
     *
     * @return The {@link  UUID} of the player this snapshot has data for
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Gets the {@link Skills} which represents the {@link us.eunoians.mcrpg.skills.Skill} this
     * snapshot has data for
     *
     * @return The {@link Skills} which represents the {@link us.eunoians.mcrpg.skills.Skill} this
     * snapshot has data for
     */
    @NotNull
    public Skills getSkillType() {
        return skillType;
    }

    /**
     * Gets the amount of exp that the {@link Skill} represented by this snapshot has according to this snapshot
     *
     * @return The positive, zero inclusive amount of exp that the {@link Skill} represented by this snapshot has
     */
    public int getCurrentExp() {
        return currentExp;
    }

    /**
     * Sets the amount of exp that the {@link Skill} represented by this snapshot has inside this
     * snapshot.
     *
     * @param currentExp The amount of exp that the {@link Skill} represented by this snapshot has. Should be a positive, zero inclusive value
     */
    public void setCurrentExp(int currentExp) {
        this.currentExp = Math.max(0, currentExp);
    }

    /**
     * Gets the amount of levels that the {@link Skill} represented by this snapshot has according to this snapshot
     *
     * @return The positive, zero inclusive amount of levels that the {@link Skill} represented by this snapshot has
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Sets the amount of levels that the {@link Skill} represented by this snapshot has inside this
     * snapshot.
     *
     * @param currentLevel The amount of levels that the {@link Skill} represented by this snapshot has. Should be a positive, zero inclusive value
     */
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = Math.max(0, currentLevel);
    }

    /**
     * Returns an unmodifiable copy of the snapshot's map of what abilities are toggled on and off for the skill
     * represented by this snapshot
     *
     * @return An unmodifiable copy of the snapshot's map of what abilities are toggled on and off for the skill represented by
     * this snapshot. The key is the {@link GenericAbility} for all abilities under the snapshot's {@link Skills} found under {@link #getSkillType()} with
     * the value being {@code true} if the ability is toggled on and {@code false} otherwise
     */
    @NotNull
    public Map<GenericAbility, Boolean> getAbilityToggledMap() {
        return Collections.unmodifiableMap(abilityToggledMap);
    }

    /**
     * Returns an unmodifiable copy of the snapshot's map of all the tiers for the abilities of the skill represented by
     * this snapshot.
     *
     * @return An unmodifiable copy of the snapshot's map of all the tiers for the abilities of the skill represented by
     * this snapshot. The key is the {@link UnlockedAbilities} for all abilities under the snapshot's {@link Skills} found under {@link #getSkillType()} with
     * the value being an {@link Integer} which ranges from {@code 0} to a configurable positive value.
     */
    @NotNull
    public Map<UnlockedAbilities, Integer> getAbilityTiers() {
        return Collections.unmodifiableMap(abilityTiers);
    }

    /**
     * Returns an unmodifiable copy of the snapshot's map of all the cooldowns for the abilities of the skill represented by
     * this snapshot.
     * <p>
     * Note that all {@link GenericAbility}s that could possibly be stored here would be active abilities.
     *
     * @return An unmodifiable copy of the snapshot's map of all the cooldowns for the abilities of the skill represented by
     * this snapshot. The key is the {@link GenericAbility} for all active abilities under the snapshot's {@link Skills} found under {@link #getSkillType()} with
     * the value being an {@link Integer} which represents the amount of seconds remaining (I think, if this is wrong and you read this yell at me lmao) for the cooldown.
     */
    @NotNull
    public Map<GenericAbility, Integer> getAbilityCooldowns() {
        return Collections.unmodifiableMap(abilityCooldowns);
    }

    /**
     * Returns an unmodifiable copy of the snapshot's map of what abilities are pending for the skill
     * represented by this snapshot
     *
     * @return An unmodifiable copy of the snapshot's map of what abilities are pending for the skill represented by
     * this snapshot. The value will be {@code true} if the ability is pending and {@code false} otherwise
     */
    @NotNull
    public Map<UnlockedAbilities, Boolean> getPendingAbilities() {
        return Collections.unmodifiableMap(pendingAbilities);
    }

    /**
     * Adds the provided data to the snapshot's stored map of what abilities are toggled on and off
     *
     * @param ability The {@link GenericAbility} to store data for
     * @param toggled The toggled state of the provided ability
     */
    void addAbilityToggledData(@NotNull GenericAbility ability, boolean toggled) {
        abilityToggledMap.put(ability, toggled);
    }

    /**
     * Adds the provided data to the snapshot's stored map of the tiers for abilities
     *
     * @param ability The {@link UnlockedAbilities} to store data for
     * @param tier    The tier of the provided ability
     */
    void addAbilityTierData(@NotNull UnlockedAbilities ability, int tier) {
        abilityTiers.put(ability, tier);
    }

    /**
     * Adds the provided data to the snapshot's stored map of the cooldowns for active abilities
     *
     * @param ability  The {@link GenericAbility} to store data for
     * @param cooldown The current cooldown of the provided ability
     */
    void addAbilityCooldownData(@NotNull GenericAbility ability, int cooldown) {
        abilityCooldowns.put(ability, cooldown);
    }

    /**
     * Adds the provided {@link UnlockedAbilities} to the snapshot's stored map of what abilities are currently pending
     *
     * @param ability The {@link UnlockedAbilities} to store data for
     * @param pending The current pending status of the provided ability
     */
    void addAbilityPendingData(@NotNull UnlockedAbilities ability, boolean pending) {
        pendingAbilities.put(ability, pending);
    }

    /**
     * Adds all of the relevant ability data for the provided {@link UnlockedAbilities} type that is stored in the
     * provided {@link ResultSet} without needing to manually call each method.
     * <p>
     * All abilities passed in will look for the toggled state, tier, and pending status. Any ability
     * passed in with {@link GenericAbility#isCooldown()} return {@code true} also have the cooldown column checked.
     *
     * @param ability   The {@link UnlockedAbilities} ability type that data is being snapshotted for
     * @param resultSet The {@link ResultSet} containing all of the expected information for the ability
     * @throws SQLException Whenever the {@link ResultSet} doesn't have the expected columns
     */
    public void addAbilityData(@NotNull UnlockedAbilities ability, @NotNull ResultSet resultSet) throws SQLException {

        String abilityDatabaseName = ability.getDatabaseName();
        addAbilityToggledData(ability, resultSet.getBoolean("is_" + abilityDatabaseName + "_toggled"));
        addAbilityTierData(ability, resultSet.getInt(abilityDatabaseName + "_tier"));
        addAbilityPendingData(ability, resultSet.getBoolean("is_" + abilityDatabaseName + "_pending"));

        if (ability.isCooldown()) {
            addAbilityCooldownData(ability, resultSet.getInt(abilityDatabaseName + "_cooldown"));
        }
    }

    //TODO Put this here and maybe a #snapshot() method into Skill?
    public static SkillDataSnapshot fromSkill(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return null;
    }
}
