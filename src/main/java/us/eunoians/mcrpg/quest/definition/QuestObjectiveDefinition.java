package us.eunoians.mcrpg.quest.definition;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An immutable definition (frame) for a single quest objective.
 * <p>
 * Contains the objective's identity, a configured objective type (which holds any type-specific
 * configuration internally), the amount of progress required, and optional rewards.
 */
public class QuestObjectiveDefinition {

    private static final Pattern TIER_TOKEN_PATTERN = Pattern.compile("\\btier\\b");

    private final NamespacedKey objectiveKey;
    private final QuestObjectiveType objectiveType;
    private final Long requiredProgress;
    private final String requiredProgressExpression;
    private final List<QuestRewardType> rewards;
    private final RewardDistributionConfig rewardDistribution;

    /**
     * Creates a new objective definition.
     *
     * @param objectiveKey        the unique key identifying this objective within its parent quest
     * @param objectiveType       a configured objective type (produced by {@link QuestObjectiveType#parseConfig})
     * @param requiredProgress    the total progress required to complete this objective (must be positive)
     * @param rewards             the rewards granted upon objective completion
     * @param rewardDistribution  the distribution configuration for objective-level rewards, or {@code null} if none
     * @throws IllegalArgumentException if {@code requiredProgress} is not positive
     */
    public QuestObjectiveDefinition(@NotNull NamespacedKey objectiveKey,
                                    @NotNull QuestObjectiveType objectiveType,
                                    long requiredProgress,
                                    @NotNull List<QuestRewardType> rewards,
                                    @Nullable RewardDistributionConfig rewardDistribution) {
        if (requiredProgress <= 0) {
            throw new IllegalArgumentException("requiredProgress must be positive");
        }
        this.objectiveKey = objectiveKey;
        this.objectiveType = objectiveType;
        this.requiredProgress = requiredProgress;
        this.requiredProgressExpression = null;
        this.rewards = List.copyOf(rewards);
        this.rewardDistribution = rewardDistribution;
    }

    /**
     * Creates a new objective definition whose required progress is computed from a Parser expression
     * at quest instance creation time (e.g. {@code "20*(tier^2)"}).
     *
     * @param objectiveKey               the unique key identifying this objective within its parent quest
     * @param objectiveType              a configured objective type (produced by {@link QuestObjectiveType#parseConfig})
     * @param requiredProgressExpression a Parser expression string that resolves to a positive number
     * @param rewards                    the rewards granted upon objective completion
     * @param rewardDistribution         the distribution configuration for objective-level rewards, or {@code null} if none
     * @throws IllegalArgumentException if the expression is null/blank
     */
    public QuestObjectiveDefinition(@NotNull NamespacedKey objectiveKey,
                                    @NotNull QuestObjectiveType objectiveType,
                                    @NotNull String requiredProgressExpression,
                                    @NotNull List<QuestRewardType> rewards,
                                    @Nullable RewardDistributionConfig rewardDistribution) {
        if (requiredProgressExpression.isBlank()) {
            throw new IllegalArgumentException("requiredProgressExpression must not be blank");
        }
        this.objectiveKey = objectiveKey;
        this.objectiveType = objectiveType;
        this.requiredProgress = null;
        this.requiredProgressExpression = requiredProgressExpression;
        this.rewards = List.copyOf(rewards);
        this.rewardDistribution = rewardDistribution;
    }

    /**
     * Gets the unique key identifying this objective within its parent quest.
     *
     * @return the objective's namespaced key
     */
    @NotNull
    public NamespacedKey getObjectiveKey() {
        return objectiveKey;
    }

    /**
     * Gets the configured objective type that determines how progress is tracked and events
     * are processed. This instance holds any type-specific configuration internally.
     *
     * @return the configured objective type
     */
    @NotNull
    public QuestObjectiveType getObjectiveType() {
        return objectiveType;
    }

    /**
     * Gets the total amount of progress required to complete objectives created from this definition.
     *
     * @return the required progress amount
     */
    public long getRequiredProgress() {
        if (requiredProgress == null) {
            throw new IllegalStateException("Required progress is expression-based for objective " + objectiveKey
                    + "; call resolveRequiredProgress(vars) instead");
        }
        return requiredProgress;
    }

    /**
     * Resolves the required progress for this objective using the provided variables.
     * If this objective was configured with a static numeric required progress, returns it directly.
     *
     * @param variables variables available to the Parser (e.g. {@code tier})
     * @return resolved required progress (must be positive)
     */
    public long resolveRequiredProgress(@NotNull Map<String, Object> variables) {
        if (requiredProgress != null) {
            return requiredProgress;
        }
        if (TIER_TOKEN_PATTERN.matcher(requiredProgressExpression).find() && !variables.containsKey("tier")) {
            throw new IllegalArgumentException("Missing required variable 'tier' for objective " + objectiveKey
                    + " (expr: " + requiredProgressExpression + ")");
        }
        Parser parser = new Parser(requiredProgressExpression);
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number n) {
                parser.setVariable(entry.getKey(), n.doubleValue());
            }
        }
        long resolved = (long) parser.getValue();
        if (resolved <= 0) {
            throw new IllegalArgumentException("Resolved required progress must be positive for objective "
                    + objectiveKey + ", was: " + resolved + " (expr: " + requiredProgressExpression + ")");
        }
        return resolved;
    }

    /**
     * Gets the immutable list of rewards granted when an objective created from this definition completes.
     *
     * @return an immutable list of configured reward types
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewards;
    }

    /**
     * Gets the {@link Route} used to look up this objective's description in the localization system.
     * The route follows the pattern {@code quests.{namespace}.{quest_key}.objectives.{objective_key}.description}.
     *
     * @param questKey the parent quest's namespaced key, used to scope the route
     * @return the localization route for the objective description
     */
    @NotNull
    public Route getDescriptionRoute(@NotNull NamespacedKey questKey) {
        return Route.fromString("quests." + questKey.getNamespace() + "." + questKey.getKey()
                + ".objectives." + objectiveKey.getKey() + ".description");
    }

    /**
     * Gets the localized description for this objective, resolved through the player's locale chain.
     * Falls back to a formatted version of the objective key if no localization entry exists.
     *
     * @param player   the player whose locale chain determines the language
     * @param questKey the parent quest's namespaced key, used to scope the localization route
     * @return the localized objective description, or a key-derived fallback
     */
    /**
     * Gets the localized description for this objective, resolved through the player's locale chain.
     * Falls back to a formatted version of the objective key if no localization entry exists.
     *
     * @param player   the player whose locale chain determines the language
     * @param questKey the parent quest's namespaced key, used to scope the localization route
     * @return the localized objective description, or a key-derived fallback
     */
    @NotNull
    public String getDescription(@NotNull McRPGPlayer player, @NotNull NamespacedKey questKey) {
        return getDescription(player, questKey, null);
    }

    /**
     * Gets the localized description with an optional inline display fallback from the
     * quest/template YAML. Falls back through: localization -> inline display -> auto-generated.
     *
     * @param player              the player whose locale chain determines the language
     * @param questKey            the parent quest's namespaced key
     * @param inlineDescription   an inline display string from the quest YAML, or {@code null}
     * @return the resolved description
     */
    @NotNull
    public String getDescription(@NotNull McRPGPlayer player, @NotNull NamespacedKey questKey,
                                 @Nullable String inlineDescription) {
        try {
            return RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessage(player, getDescriptionRoute(questKey));
        } catch (Exception e) {
            if (inlineDescription != null && !inlineDescription.isEmpty()) {
                return inlineDescription;
            }
            long required;
            try {
                required = getRequiredProgress();
            } catch (IllegalStateException ex) {
                required = 0;
            }
            return objectiveType.describeObjective(required);
        }
    }

    /**
     * Gets the optional reward distribution configuration for objective-level completion rewards.
     *
     * @return an {@link Optional} containing the distribution config, or empty if standard (non-distributed) rewards apply
     */
    @NotNull
    public Optional<RewardDistributionConfig> getRewardDistribution() {
        return Optional.ofNullable(rewardDistribution);
    }
}
