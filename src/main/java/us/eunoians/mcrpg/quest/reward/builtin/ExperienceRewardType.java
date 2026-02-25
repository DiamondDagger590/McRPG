package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

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
        if (skillName.isEmpty() || amount <= 0) {
            return;
        }

        NamespacedKey skillKey = resolveSkillKey(skillName);
        if (skillKey == null || !RegistryAccess.registryAccess().registry(McRPGRegistryKey.SKILL).registered(skillKey)) {
            Logger.getLogger(ExperienceRewardType.class.getName())
                    .warning("Cannot grant experience — unknown skill: " + skillName);
            return;
        }

        Optional<McRPGPlayer> mcRPGPlayer = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        mcRPGPlayer.ifPresent(p ->
                p.asSkillHolder().getSkillHolderData(skillKey)
                        .ifPresent(data -> data.addExperience((int) amount)));
    }

    @org.jetbrains.annotations.Nullable
    private static NamespacedKey resolveSkillKey(@NotNull String input) {
        if (input.contains(":")) {
            return NamespacedKey.fromString(input.toLowerCase());
        }
        return new NamespacedKey(McRPGMethods.getMcRPGNamespace(), input.toLowerCase());
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        return Map.of("skill", skillName, "amount", amount);
    }

    @NotNull
    @Override
    public ExperienceRewardType withAmountMultiplier(double multiplier) {
        long scaled = Math.max(1, (long) (amount * multiplier));
        return new ExperienceRewardType(skillName, scaled);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
