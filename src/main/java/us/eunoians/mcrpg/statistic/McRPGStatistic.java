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
 * Global McRPG statistics defined as constants. These represent gameplay-wide
 * totals that are not tied to a specific skill or ability.
 * <p>
 * Per-skill statistics (experience, max level) are derived from the {@link us.eunoians.mcrpg.skill.Skill}
 * interface via {@link us.eunoians.mcrpg.skill.Skill#getExperienceStatisticKey()} and
 * {@link us.eunoians.mcrpg.skill.Skill#getMaxLevelStatisticKey()}, and constructed in
 * {@link us.eunoians.mcrpg.skill.impl.McRPGSkill#getDefaultStatistics()}.
 * <p>
 * Per-ability activation statistics are derived from the
 * {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility} interface via
 * {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility#getActivationStatisticKey()}, and
 * constructed in {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility#getDefaultStatistics()}.
 * <p>
 * Statistics are registered during bootstrap via
 * {@link us.eunoians.mcrpg.expansion.content.StatisticContentPack} in the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
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

    /** Sum of all levels gained across all skills. */
    public static final Statistic TOTAL_SKILL_LEVELS_GAINED = longStat("total_skill_levels_gained",
            "Total Skill Levels Gained", "Sum of all levels gained across all skills");

    /** Sum of all XP earned across all skills (including overflow). */
    public static final Statistic TOTAL_SKILL_EXPERIENCE = longStat("total_skill_experience",
            "Total Skill Experience", "Sum of all XP earned across all skills");

    /** Total ability activations across all abilities. */
    public static final Statistic ABILITIES_ACTIVATED = longStat("abilities_activated",
            "Abilities Activated", "Total ability activations across all abilities");

    /** Total healing applied to other entities across all combat sessions. */
    public static final Statistic HEALING_DEALT = doubleStat("healing_dealt",
            "Healing Dealt", "Total healing applied to other entities");

    /**
     * Total healing received across all combat sessions, from any source. Credited from
     * {@code EntityRegainHealthEvent} for any entity with an active combat session, so it counts
     * every point of health regained while combat-tagged — natural regeneration, saturation,
     * potions, and beacons included, not just ability or PvP healing.
     */
    public static final Statistic HEALING_RECEIVED = doubleStat("healing_received",
            "Healing Received", "Total health regained while in combat — includes natural regen, "
                    + "saturation, potions, and beacons, not just ability/PvP healing");

    /** Total attacks landed across all combat sessions. */
    public static final Statistic HITS_LANDED = longStat("hits_landed",
            "Hits Landed", "Total attacks landed");

    /** Total times hit across all combat sessions. */
    public static final Statistic HITS_RECEIVED = longStat("hits_received",
            "Hits Received", "Total times hit");

    /**
     * Total entities killed across all combat sessions. Counts every kill credited to an active
     * combat session — mobs and players alike. Distinct from {@link #MOBS_KILLED}, which is a
     * global, session-independent, mob-only counter. A mob killed during a combat session
     * increments both.
     */
    public static final Statistic COMBAT_KILLS = longStat("combat_kills",
            "Combat Kills", "Total entities killed in combat");

    /**
     * All statically-defined global statistics. Does not include per-skill statistics
     * (which are generated by each skill's {@link us.eunoians.mcrpg.skill.Skill#getDefaultStatistics()})
     * or per-ability activation statistics (generated by
     * {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility#getDefaultStatistics()}).
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
        // Skill progression (global totals only — per-skill stats come from Skill.getDefaultStatistics())
        stats.add(TOTAL_SKILL_LEVELS_GAINED);
        stats.add(TOTAL_SKILL_EXPERIENCE);
        // Ability (global total — per-ability stats come from ActiveAbility.getDefaultStatistics())
        stats.add(ABILITIES_ACTIVATED);
        // Combat session cumulative totals (fed from CombatSessionStatistics on session end)
        stats.add(HEALING_DEALT);
        stats.add(HEALING_RECEIVED);
        stats.add(HITS_LANDED);
        stats.add(HITS_RECEIVED);
        stats.add(COMBAT_KILLS);
        ALL_STATIC_STATISTICS = Collections.unmodifiableSet(stats);
    }

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

    /**
     * Creates a {@link StatisticType#LONG LONG} statistic with a default value of {@code 0L}
     * under the McRPG namespace.
     *
     * @param key         The key portion of the {@link NamespacedKey}.
     * @param displayName The admin-facing display name.
     * @param description The admin-facing description.
     * @return A new {@link Statistic} of type {@link StatisticType#LONG}.
     */
    @NotNull
    private static Statistic longStat(@NotNull String key, @NotNull String displayName, @NotNull String description) {
        return new SimpleStatistic(key(key), StatisticType.LONG, 0L, displayName, description);
    }

    /**
     * Creates a {@link StatisticType#DOUBLE DOUBLE} statistic with a default value of {@code 0.0}
     * under the McRPG namespace.
     *
     * @param key         The key portion of the {@link NamespacedKey}.
     * @param displayName The admin-facing display name.
     * @param description The admin-facing description.
     * @return A new {@link Statistic} of type {@link StatisticType#DOUBLE}.
     */
    @NotNull
    private static Statistic doubleStat(@NotNull String key, @NotNull String displayName, @NotNull String description) {
        return new SimpleStatistic(key(key), StatisticType.DOUBLE, 0.0, displayName, description);
    }
}
