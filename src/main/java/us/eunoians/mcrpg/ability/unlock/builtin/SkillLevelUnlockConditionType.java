package us.eunoians.mcrpg.ability.unlock.builtin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionParseException;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Unlock condition met when the holder's level in {@link #skillKey} is at or above
 * {@link #requiredLevel}. The behaviour-preserving migration target for the legacy
 * {@code getUnlockLevel()} call path.
 */
public final class SkillLevelUnlockConditionType implements UnlockConditionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_level");

    private final NamespacedKey skillKey;
    private final int requiredLevel;

    /** Registry base instance — unconfigured prototype. */
    public SkillLevelUnlockConditionType() {
        this(null, 0);
    }

    /** Public configured constructor used by Java-authored defaults (e.g. TierableAbility). */
    public SkillLevelUnlockConditionType(@Nullable NamespacedKey skillKey, int requiredLevel) {
        this.skillKey = skillKey;
        this.requiredLevel = requiredLevel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public UnlockConditionType parseConfig(@NotNull Section section) {
        NamespacedKey skill = McRPGMethods.parseNamespacedKey(section.getString("skill"));
        if (skill == null) {
            throw new UnlockConditionParseException("mcrpg:skill_level requires a 'skill' key");
        }
        if (!section.contains("level")) {
            throw new UnlockConditionParseException("mcrpg:skill_level requires a 'level' key");
        }
        return new SkillLevelUnlockConditionType(skill, section.getInt("level"));
    }

    @Override
    public boolean isMet(@NotNull AbilityHolder holder) {
        if (skillKey == null || !(holder instanceof SkillHolder skillHolder)) {
            return false;
        }
        return skillHolder.getSkillHolderData(skillKey)
                .map(data -> data.getCurrentLevel() >= requiredLevel)
                .orElse(false);
    }

    @Override
    public double getProgress(@NotNull AbilityHolder holder) {
        if (skillKey == null || requiredLevel <= 0 || !(holder instanceof SkillHolder skillHolder)) {
            return 0.0;
        }
        return skillHolder.getSkillHolderData(skillKey)
                .map(data -> Math.min(1.0, (double) data.getCurrentLevel() / requiredLevel))
                .orElse(0.0);
    }

    @NotNull
    @Override
    public Component getDisplayDescription(@NotNull McRPGPlayer player) {
        return renderTemplate(player, LocalizationKey.UNLOCK_CONDITION_SKILL_LEVEL_DESCRIPTION);
    }

    @NotNull
    @Override
    public Component getDisplayLabel(@NotNull McRPGPlayer player) {
        return renderTemplate(player, LocalizationKey.UNLOCK_CONDITION_SKILL_LEVEL_LABEL);
    }

    /**
     * Required skill level — exposed for legacy callers (GUI sort, lore appender) that still
     * need the numeric level for compact display.
     *
     * @return the level threshold
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * The skill key this condition targets, or {@code null} on the unconfigured prototype.
     *
     * @return the skill key, or {@code null}
     */
    @Nullable
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    private Component renderTemplate(@NotNull McRPGPlayer player, @NotNull dev.dejvokep.boostedyaml.route.Route template) {
        McRPGLocalizationManager localization = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        if (skillKey == null) {
            return Component.empty();
        }
        var formatter = localization.getDisplayDecimalFormatter();
        SkillRegistry skillRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL);
        Skill skill = skillRegistry.getRegisteredSkill(skillKey);
        String skillName = skill != null ? skill.getColoredName(player) : skillKey.toString();
        long current = player.asSkillHolder().getSkillHolderData(skillKey)
                .map(data -> (long) data.getCurrentLevel()).orElse(0L);
        return localization.getLocalizedMessageAsComponent(player, template, Map.of(
                "skill", skillName,
                "required", formatter.formatDisplayDecimal(player, (long) requiredLevel),
                "current", formatter.formatDisplayDecimal(player, current)));
    }
}
