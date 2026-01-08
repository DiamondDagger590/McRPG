package us.eunoians.mcrpg.database.table;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

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
    private final NamespacedKey skillKey;
    private int totalExperience;
    private final Map<NamespacedKey, Map<NamespacedKey, AbilityAttribute<?>>> abilityAttributes;

    /**
     * Constructs a new {@link SkillDataSnapshot} with the value for {@link #getTotalExperience()} being 0.
     *
     * @param uuid     The {@link UUID} of the player this snapshot is for
     * @param skillKey The {@link NamespacedKey} which represents the {@link us.eunoians.mcrpg.skill.Skill} this snapshot has data for
     */
    public SkillDataSnapshot(@NotNull UUID uuid, @NotNull NamespacedKey skillKey) {
        this.uuid = uuid;
        this.skillKey = skillKey;
        this.totalExperience = 0;
        this.abilityAttributes = new HashMap<>();
    }

    /**
     * @param uuid            The {@link UUID} of the player this snapshot is for
     * @param skillKey        The {@link NamespacedKey} which represents the {@link us.eunoians.mcrpg.skill.Skill} this snapshot has data for
     * @param totalExperience The total experience ever earned for this skill
     */
    public SkillDataSnapshot(@NotNull UUID uuid, @NotNull NamespacedKey skillKey, int totalExperience) {
        this.uuid = uuid;
        this.skillKey = skillKey;
        this.totalExperience = totalExperience;
        this.abilityAttributes = new HashMap<>();
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
     * Gets the {@link NamespacedKey} which represents the {@link us.eunoians.mcrpg.skill.Skill} this
     * snapshot has data for
     *
     * @return The {@link NamespacedKey} which represents the {@link us.eunoians.mcrpg.skill.Skill} this
     * snapshot has data for
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    /**
     * Gets the total experience ever earned for the {@link us.eunoians.mcrpg.skill.Skill} represented by this snapshot.
     *
     * @return The positive, zero inclusive total experience for the skill.
     */
    public int getTotalExperience() {
        return totalExperience;
    }

    /**
     * Sets the total experience for the {@link us.eunoians.mcrpg.skill.Skill} represented by this snapshot.
     *
     * @param totalExperience The total experience. Should be a positive, zero inclusive value.
     */
    public void setTotalExperience(int totalExperience) {
        this.totalExperience = Math.max(0, totalExperience);
    }

    /**
     * Adds the provided {@link AbilityAttribute} to this skill snapshot and associates it with the provided {@link NamespacedKey}
     *
     * @param abilityKey       The {@link NamespacedKey} to add the attribute for
     * @param abilityAttribute The {@link AbilityAttribute} to associate with the provided generic ability
     */
    public void addAttribute(@NotNull NamespacedKey abilityKey, @NotNull AbilityAttribute<?> abilityAttribute) {
        Map<NamespacedKey, AbilityAttribute<?>> abilityAttributeList = getAbilityAttributes(abilityKey);
        abilityAttributeList.put(abilityAttribute.getNamespacedKey(), abilityAttribute);
        abilityAttributes.put(abilityKey, abilityAttributeList);
    }

    public void addDefaultAttributes(@NotNull Ability ability) {
        Map<NamespacedKey, AbilityAttribute<?>> abilityAttributeList = getAbilityAttributes(ability.getAbilityKey());
        AbilityAttributeRegistry abilityAttributeRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY_ATTRIBUTE);
        for (NamespacedKey attributeKey : ability.getApplicableAttributes()) {
            abilityAttributeRegistry.getAttribute(attributeKey).ifPresent(abilityAttribute -> abilityAttributeList.put(attributeKey, abilityAttribute));
        }
        abilityAttributes.put(ability.getAbilityKey(), abilityAttributeList);
    }

    /**
     * Gets a map of {@link NamespacedKey}s mapped to {@link AbilityAttribute}s that are associated with the provided
     * {@link NamespacedKey}.
     *
     * @param abilityKey The {@link NamespacedKey} to get the attributes for
     * @return A {@link Map} of {@link NamespacedKey}s mapped to their {@link AbilityAttribute} for easy lookup of
     * what attributes an ability has.
     */
    @NotNull
    public Map<NamespacedKey, AbilityAttribute<?>> getAbilityAttributes(@NotNull NamespacedKey abilityKey) {
        return abilityAttributes.getOrDefault(abilityKey, new HashMap<>());
    }

    //TODO Put this here and maybe a #snapshot() method into Skill?
    public static SkillDataSnapshot fromSkill(@NotNull McRPGPlayer mcRPGPlayer, @NotNull NamespacedKey skillKey) {
        return null;
    }
}
