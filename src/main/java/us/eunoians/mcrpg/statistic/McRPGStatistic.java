package us.eunoians.mcrpg.statistic;

import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * All McRPG-tracked statistics, defined as constants. Follows the same pattern
 * as {@link us.eunoians.mcrpg.entity.player.McRPGSetting} for player settings.
 * <p>
 * Statistics are registered during bootstrap via
 * {@link us.eunoians.mcrpg.bootstrap.McRPGStatisticRegistrar}.
 * <p>
 * <b>Note on display names and descriptions:</b> The {@code displayName} and
 * {@code description} fields on each statistic are plain-English metadata used for
 * internal/admin-facing purposes (e.g., debug commands, database inspection, admin UIs).
 * They are <b>not</b> player-facing chat text and therefore do not need to go through the
 * localization system. If statistics are ever surfaced in player-visible messages or GUIs,
 * those messages should use
 * {@link us.eunoians.mcrpg.configuration.file.localization.LocalizationKey} entries that
 * reference the statistic by its {@link org.bukkit.NamespacedKey} rather than displaying
 * these fields directly.
 */
public final class McRPGStatistic {

    private McRPGStatistic() {
        // Utility class — no instantiation
    }

    private static final String NAMESPACE = McRPGMethods.getMcRPGNamespace();

    // ── Global Gameplay Statistics ──────────────────────────────────────────

    /** Total blocks mined that grant skill XP (any skill). */
    public static final Statistic BLOCKS_MINED = longStat("blocks_mined",
            "Blocks Mined", "Total blocks mined that grant skill XP");

    /** Ore blocks specifically. */
    public static final Statistic ORES_MINED = longStat("ores_mined",
            "Ores Mined", "Ore blocks mined");

    /** Logs broken that grant skill XP. */
    public static final Statistic TREES_CHOPPED = longStat("trees_chopped",
            "Trees Chopped", "Logs broken that grant skill XP");

    /** Crops harvested that grant skill XP. */
    public static final Statistic CROPS_HARVESTED = longStat("crops_harvested",
            "Crops Harvested", "Crops harvested that grant skill XP");

    /** Total mobs killed (any combat). */
    public static final Statistic MOBS_KILLED = longStat("mobs_killed",
            "Mobs Killed", "Total mobs killed");

    /** Total damage dealt (all sources). */
    public static final Statistic DAMAGE_DEALT = new SimpleStatistic(
            key("damage_dealt"), StatisticType.DOUBLE, 0.0,
            "Damage Dealt", "Total damage dealt");

    /** Total damage taken (all sources). */
    public static final Statistic DAMAGE_TAKEN = new SimpleStatistic(
            key("damage_taken"), StatisticType.DOUBLE, 0.0,
            "Damage Taken", "Total damage taken");

    // ── Skill Progression Statistics ───────────────────────────────────────

    /** Sum of all levels gained across all skills. */
    public static final Statistic TOTAL_SKILL_LEVELS_GAINED = longStat("total_skill_levels_gained",
            "Total Skill Levels Gained", "Sum of all levels gained across all skills");

    /** Sum of all XP earned across all skills (including overflow). */
    public static final Statistic TOTAL_SKILL_EXPERIENCE = longStat("total_skill_experience",
            "Total Skill Experience", "Sum of all XP earned across all skills");

    /** Total Mining XP earned (including overflow). */
    public static final Statistic MINING_EXPERIENCE = longStat("mining_experience",
            "Mining Experience", "Total Mining XP earned");

    /** Total Swords XP earned (including overflow). */
    public static final Statistic SWORDS_EXPERIENCE = longStat("swords_experience",
            "Swords Experience", "Total Swords XP earned");

    /** Total Herbalism XP earned (including overflow). */
    public static final Statistic HERBALISM_EXPERIENCE = longStat("herbalism_experience",
            "Herbalism Experience", "Total Herbalism XP earned");

    /** Total WoodCutting XP earned (including overflow). */
    public static final Statistic WOODCUTTING_EXPERIENCE = longStat("woodcutting_experience",
            "WoodCutting Experience", "Total WoodCutting XP earned");

    /** Highest Mining level reached. */
    public static final Statistic MINING_MAX_LEVEL = intStat("mining_max_level",
            "Mining Max Level", "Highest Mining level reached");

    /** Highest Swords level reached. */
    public static final Statistic SWORDS_MAX_LEVEL = intStat("swords_max_level",
            "Swords Max Level", "Highest Swords level reached");

    /** Highest Herbalism level reached. */
    public static final Statistic HERBALISM_MAX_LEVEL = intStat("herbalism_max_level",
            "Herbalism Max Level", "Highest Herbalism level reached");

    /** Highest WoodCutting level reached. */
    public static final Statistic WOODCUTTING_MAX_LEVEL = intStat("woodcutting_max_level",
            "WoodCutting Max Level", "Highest WoodCutting level reached");

    // ── Ability Statistics ─────────────────────────────────────────────────

    /** Total ability activations across all abilities. */
    public static final Statistic ABILITIES_ACTIVATED = longStat("abilities_activated",
            "Abilities Activated", "Total ability activations across all abilities");

    // ── All Static Constants ───────────────────────────────────────────────

    /**
     * All statically-defined statistics. Does not include dynamically-generated
     * per-ability activation statistics.
     */
    public static final Set<Statistic> ALL_STATIC_STATISTICS;

    static {
        Set<Statistic> stats = new LinkedHashSet<>();
        // Global gameplay
        stats.add(BLOCKS_MINED);
        stats.add(ORES_MINED);
        stats.add(TREES_CHOPPED);
        stats.add(CROPS_HARVESTED);
        stats.add(MOBS_KILLED);
        stats.add(DAMAGE_DEALT);
        stats.add(DAMAGE_TAKEN);
        // Skill progression
        stats.add(TOTAL_SKILL_LEVELS_GAINED);
        stats.add(TOTAL_SKILL_EXPERIENCE);
        stats.add(MINING_EXPERIENCE);
        stats.add(SWORDS_EXPERIENCE);
        stats.add(HERBALISM_EXPERIENCE);
        stats.add(WOODCUTTING_EXPERIENCE);
        stats.add(MINING_MAX_LEVEL);
        stats.add(SWORDS_MAX_LEVEL);
        stats.add(HERBALISM_MAX_LEVEL);
        stats.add(WOODCUTTING_MAX_LEVEL);
        // Ability
        stats.add(ABILITIES_ACTIVATED);
        ALL_STATIC_STATISTICS = Collections.unmodifiableSet(stats);
    }

    // ── Dynamic Key Helpers ────────────────────────────────────────────────

    /**
     * Gets the per-skill experience statistic key for a given skill key.
     * <p>
     * Example: {@code getSkillExperienceKey(Mining.MINING_KEY)} returns
     * the key {@code mcrpg:mining_experience}.
     *
     * @param skillKey The skill's {@link NamespacedKey}.
     * @return The experience statistic key for the given skill.
     */
    @NotNull
    public static NamespacedKey getSkillExperienceKey(@NotNull NamespacedKey skillKey) {
        return key(skillKey.getKey() + "_experience");
    }

    /**
     * Gets the per-skill max level statistic key for a given skill key.
     * <p>
     * Example: {@code getSkillMaxLevelKey(Mining.MINING_KEY)} returns
     * the key {@code mcrpg:mining_max_level}.
     *
     * @param skillKey The skill's {@link NamespacedKey}.
     * @return The max level statistic key for the given skill.
     */
    @NotNull
    public static NamespacedKey getSkillMaxLevelKey(@NotNull NamespacedKey skillKey) {
        return key(skillKey.getKey() + "_max_level");
    }

    /**
     * Gets the per-ability activation count statistic key for a given ability key.
     * <p>
     * Example: {@code getAbilityActivationKey(Bleed.BLEED_KEY)} returns
     * the key {@code mcrpg:bleed_activations}.
     *
     * @param abilityKey The ability's {@link NamespacedKey}.
     * @return The activation count statistic key for the given ability.
     */
    @NotNull
    public static NamespacedKey getAbilityActivationKey(@NotNull NamespacedKey abilityKey) {
        return key(abilityKey.getKey() + "_activations");
    }

    /**
     * Creates a per-ability activation {@link Statistic} definition for dynamic registration.
     * <p>
     * The generated display name and description are admin-facing metadata — see the class-level
     * javadoc for why they are not localized.
     *
     * @param abilityKey   The ability's {@link NamespacedKey}.
     * @param displayName  The human-readable ability name (used for the statistic display name).
     * @return A new {@link Statistic} for tracking the ability's activation count.
     */
    @NotNull
    public static Statistic createAbilityActivationStatistic(@NotNull NamespacedKey abilityKey, @NotNull String displayName) {
        return new SimpleStatistic(
                getAbilityActivationKey(abilityKey),
                StatisticType.LONG,
                0L,
                displayName + " Activations",
                "Times " + displayName + " has been activated"
        );
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Creates a {@link NamespacedKey} under the McRPG namespace. Uses the deprecated
     * {@code NamespacedKey(String, String)} constructor because these are static constants
     * initialized before a {@link org.bukkit.plugin.Plugin} instance is available.
     *
     * @param key The key portion of the {@link NamespacedKey}.
     * @return A new {@link NamespacedKey} under the McRPG namespace.
     */
    @NotNull
    @SuppressWarnings("deprecation") // NamespacedKey(String, String) — no Plugin instance in static context
    private static NamespacedKey key(@NotNull String key) {
        return new NamespacedKey(NAMESPACE, key);
    }

    @NotNull
    private static Statistic longStat(@NotNull String key, @NotNull String displayName, @NotNull String description) {
        return new SimpleStatistic(key(key), StatisticType.LONG, 0L, displayName, description);
    }

    @NotNull
    private static Statistic intStat(@NotNull String key, @NotNull String displayName, @NotNull String description) {
        return new SimpleStatistic(key(key), StatisticType.INT, 0, displayName, description);
    }
}
