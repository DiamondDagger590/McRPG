package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Built-in reward type that grants McRPG skill experience to a player.
 * <p>
 * Config format:
 * <pre>
 * type: mcrpg:experience
 * skill: MINING
 * amount: 500
 * </pre>
 */
public class ExperienceRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "experience");

    private final String skillName;
    private final long amount;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ExperienceRewardType() {
        this.skillName = "";
        this.amount = 0;
    }

    private ExperienceRewardType(@NotNull String skillName, long amount) {
        this.skillName = skillName;
        this.amount = amount;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public ExperienceRewardType parseConfig(@NotNull Section section) {
        return new ExperienceRewardType(
                section.getString("skill", ""),
                section.getLong("amount", 0L)
        );
    }

    @NotNull
    @Override
    public ExperienceRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String skill = config.getOrDefault("skill", "").toString();
        long amt = config.containsKey("amount") ? ((Number) config.get("amount")).longValue() : 0;
        return new ExperienceRewardType(skill, amt);
    }

    @Override
    public void grant(@NotNull Player player) {
        // TODO: Hook into McRPG skill XP system to grant experience
        // SkillRegistry -> find skill by name -> grant XP to player
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        return Map.of("skill", skillName, "amount", amount);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
