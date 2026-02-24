package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The central data class representing a quest template definition. Immutable
 * after construction. Templates are loaded from YAML files under
 * {@code quest-board/templates/} and registered in
 * {@link us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry}.
 * <p>
 * A template contains unresolved variable references and template-specific
 * structure (pools, ranges, rarity overrides). The
 * {@code QuestTemplateEngine} transforms a template into a concrete
 * {@code QuestDefinition} at generation time.
 */
public final class QuestTemplate implements McRPGContent {

    private final NamespacedKey key;
    private final Route displayNameRoute;
    private final boolean boardEligible;
    private final NamespacedKey scopeProviderKey;
    private final Set<NamespacedKey> supportedRarities;
    private final Map<NamespacedKey, RarityOverride> rarityOverrides;
    private final Map<String, TemplateVariable> variables;
    private final List<TemplatePhaseDefinition> phases;
    private final List<TemplateRewardDefinition> rewards;
    private final NamespacedKey expansionKey;

    public QuestTemplate(@NotNull NamespacedKey key,
                         @NotNull Route displayNameRoute,
                         boolean boardEligible,
                         @NotNull NamespacedKey scopeProviderKey,
                         @NotNull Set<NamespacedKey> supportedRarities,
                         @NotNull Map<NamespacedKey, RarityOverride> rarityOverrides,
                         @NotNull Map<String, TemplateVariable> variables,
                         @NotNull List<TemplatePhaseDefinition> phases,
                         @NotNull List<TemplateRewardDefinition> rewards) {
        this(key, displayNameRoute, boardEligible, scopeProviderKey, supportedRarities,
                rarityOverrides, variables, phases, rewards, null);
    }

    public QuestTemplate(@NotNull NamespacedKey key,
                         @NotNull Route displayNameRoute,
                         boolean boardEligible,
                         @NotNull NamespacedKey scopeProviderKey,
                         @NotNull Set<NamespacedKey> supportedRarities,
                         @NotNull Map<NamespacedKey, RarityOverride> rarityOverrides,
                         @NotNull Map<String, TemplateVariable> variables,
                         @NotNull List<TemplatePhaseDefinition> phases,
                         @NotNull List<TemplateRewardDefinition> rewards,
                         @Nullable NamespacedKey expansionKey) {
        this.key = key;
        this.displayNameRoute = displayNameRoute;
        this.boardEligible = boardEligible;
        this.scopeProviderKey = scopeProviderKey;
        this.supportedRarities = Set.copyOf(supportedRarities);
        this.rarityOverrides = Map.copyOf(rarityOverrides);
        this.variables = Map.copyOf(variables);
        this.phases = List.copyOf(phases);
        this.rewards = List.copyOf(rewards);
        this.expansionKey = expansionKey;
    }

    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }

    /**
     * Returns the unique namespaced key identifying this template.
     *
     * @return the template key
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Returns the localization route used to resolve this template's display name.
     *
     * @return the display name route
     */
    @NotNull
    public Route getDisplayNameRoute() {
        return displayNameRoute;
    }

    /**
     * Returns whether this template is eligible for automatic selection by the
     * quest board offering system. Templates with this set to {@code false} can
     * only be used via programmatic registration.
     *
     * @return {@code true} if this template may appear on quest boards
     */
    public boolean isBoardEligible() {
        return boardEligible;
    }

    /**
     * Returns the key of the scope provider that quests generated from this
     * template will use (e.g. single-player, land-based).
     *
     * @return the scope provider key
     */
    @NotNull
    public NamespacedKey getScopeProviderKey() {
        return scopeProviderKey;
    }

    /**
     * Returns the immutable set of rarity keys this template supports. Only
     * these rarities may be passed to the template engine during generation.
     *
     * @return the supported rarity keys
     */
    @NotNull
    public Set<NamespacedKey> getSupportedRarities() {
        return supportedRarities;
    }

    /**
     * Returns the immutable map of per-rarity multiplier overrides. Entries
     * here take precedence over the global values in the rarity registry.
     *
     * @return rarity key to override mappings
     */
    @NotNull
    public Map<NamespacedKey, RarityOverride> getRarityOverrides() {
        return rarityOverrides;
    }

    /**
     * Returns the immutable map of template variables keyed by variable name.
     * These are resolved at generation time to produce concrete quest values.
     *
     * @return variable name to definition mappings
     */
    @NotNull
    public Map<String, TemplateVariable> getVariables() {
        return variables;
    }

    /**
     * Returns the immutable ordered list of phase definitions for this template.
     * Each phase contains stages and objectives that form the quest structure.
     *
     * @return the phase definitions
     */
    @NotNull
    public List<TemplatePhaseDefinition> getPhases() {
        return phases;
    }

    /**
     * Returns the immutable ordered list of reward definitions granted upon
     * quest completion.
     *
     * @return the reward definitions
     */
    @NotNull
    public List<TemplateRewardDefinition> getRewards() {
        return rewards;
    }

    /**
     * Validates that this template supports the given rarity key.
     *
     * @param rarityKey the rarity to validate
     * @throws IllegalArgumentException if the rarity is not in {@code supportedRarities}
     */
    public void validateRaritySupported(@NotNull NamespacedKey rarityKey) {
        if (!supportedRarities.contains(rarityKey)) {
            throw new IllegalArgumentException("Template " + key + " does not support rarity "
                    + rarityKey + ". Supported: " + supportedRarities);
        }
    }

    /**
     * Returns the effective difficulty multiplier for a rarity, checking
     * template-level overrides first and falling back to the global rarity
     * registry value.
     * <p>
     * Does NOT validate that the rarity is in {@code supportedRarities} --
     * call {@link #validateRaritySupported(NamespacedKey)} first if validation
     * is needed.
     *
     * @param rarityKey the rarity to look up
     * @param registry  the global rarity registry used as fallback
     * @return the difficulty multiplier (template override if present, otherwise registry default, otherwise {@code 1.0})
     */
    public double getEffectiveDifficultyMultiplier(@NotNull NamespacedKey rarityKey,
                                                    @NotNull QuestRarityRegistry registry) {
        RarityOverride override = rarityOverrides.get(rarityKey);
        if (override != null && override.difficultyMultiplier() != null) {
            return override.difficultyMultiplier();
        }
        return registry.get(rarityKey)
                .map(QuestRarity::getDifficultyMultiplier)
                .orElse(1.0);
    }

    /**
     * Returns the effective reward multiplier for a rarity, checking
     * template-level overrides first and falling back to the global rarity
     * registry value.
     * <p>
     * Does NOT validate that the rarity is in {@code supportedRarities} --
     * call {@link #validateRaritySupported(NamespacedKey)} first if validation
     * is needed.
     *
     * @param rarityKey the rarity to look up
     * @param registry  the global rarity registry used as fallback
     * @return the reward multiplier (template override if present, otherwise registry default, otherwise {@code 1.0})
     */
    public double getEffectiveRewardMultiplier(@NotNull NamespacedKey rarityKey,
                                                @NotNull QuestRarityRegistry registry) {
        RarityOverride override = rarityOverrides.get(rarityKey);
        if (override != null && override.rewardMultiplier() != null) {
            return override.rewardMultiplier();
        }
        return registry.get(rarityKey)
                .map(QuestRarity::getRewardMultiplier)
                .orElse(1.0);
    }
}
