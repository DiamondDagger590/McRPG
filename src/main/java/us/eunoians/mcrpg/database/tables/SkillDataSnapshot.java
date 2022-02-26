package us.eunoians.mcrpg.database.tables;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;

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

    private final UUID uuid;
    private final Skills skillType;
    private int currentExp;
    private int currentLevel;
    private final Map<GenericAbility, Map<NamespacedKey, AbilityAttribute<?>>> abilityAttributes;
    private final Map<GenericAbility, Boolean> abilityToggledMap;

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
        this.abilityAttributes = new HashMap<>();
        this.abilityToggledMap = new HashMap<>();
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
        this.abilityAttributes = new HashMap<>();
        this.abilityToggledMap = new HashMap<>();
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
     * Adds the provided {@link AbilityAttribute} to this skill snapshot and associates it with the provided {@link GenericAbility}
     *
     * @param genericAbility   The {@link GenericAbility} to add the attribute for
     * @param abilityAttribute The {@link AbilityAttribute} to associate with the provided generic ability
     */
    public void addAttribute(GenericAbility genericAbility, AbilityAttribute<?> abilityAttribute) {
        Map<NamespacedKey, AbilityAttribute<?>> abilityAttributeList = getAbilityAttributes(genericAbility);
        abilityAttributeList.put(abilityAttribute.getNamespacedKey(), abilityAttribute);
        abilityAttributes.put(genericAbility, abilityAttributeList);
    }

    /**
     * Gets a map of {@link NamespacedKey}s mapped to {@link AbilityAttribute}s that are associated with the provided
     * {@link GenericAbility}.
     *
     * @param genericAbility The {@link GenericAbility} to get the attributes for
     * @return A {@link Map} of {@link NamespacedKey}s mapped to their {@link AbilityAttribute} for easy lookup of
     * what attributes an ability has.
     */
    @NotNull
    public Map<NamespacedKey, AbilityAttribute<?>> getAbilityAttributes(GenericAbility genericAbility) {
        return abilityAttributes.getOrDefault(genericAbility, new HashMap<>());
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
     * Adds the provided data to the snapshot's stored map of what abilities are toggled on and off
     *
     * @param ability The {@link GenericAbility} to store data for
     * @param toggled The toggled state of the provided ability
     */
    void addAbilityToggledData(@NotNull GenericAbility ability, boolean toggled) {
        abilityToggledMap.put(ability, toggled);
    }

    //TODO Put this here and maybe a #snapshot() method into Skill?
    public static SkillDataSnapshot fromSkill(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return null;
    }
}
