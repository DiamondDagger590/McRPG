package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;
import us.eunoians.mcrpg.event.board.TemplateQuestGenerateEvent;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;

import com.diamonddagger590.mccore.util.TimeProvider;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates the logic of assembling the pool of eligible quest definitions and
 * templates for board generation. Draws from both {@link QuestDefinitionRegistry}
 * (hand-crafted quests) and {@link QuestTemplateRegistry} (template-generated quests).
 * <p>
 * The definition registry, template registry, and logger are injected at construction
 * time so this class carries no static dependencies.
 */
public class QuestPool {

    private final QuestDefinitionRegistry definitionRegistry;
    private final QuestTemplateRegistry templateRegistry;
    private final Logger logger;
    private final TimeProvider timeProvider;

    /**
     * Creates a new quest pool.
     *
     * @param definitionRegistry the hand-crafted quest definition registry
     * @param templateRegistry   the template registry
     * @param logger             the logger for generation warnings
     * @param timeProvider       the time provider used for evaluating template availability windows
     */
    public QuestPool(@NotNull QuestDefinitionRegistry definitionRegistry,
                     @NotNull QuestTemplateRegistry templateRegistry,
                     @NotNull Logger logger,
                     @NotNull TimeProvider timeProvider) {
        this.definitionRegistry = definitionRegistry;
        this.templateRegistry = templateRegistry;
        this.logger = logger;
        this.timeProvider = timeProvider;
    }

    /**
     * Gets hand-crafted definitions eligible for a specific rolled rarity.
     *
     * @param rolledRarity the rarity to filter by
     * @return the list of eligible definition keys
     */
    @NotNull
    public List<NamespacedKey> getEligibleDefinitions(@NotNull NamespacedKey rolledRarity) {
        return getEligibleDefinitions(rolledRarity, null, Set.of());
    }

    /**
     * Gets hand-crafted definitions eligible for a specific rolled rarity, optionally
     * filtered by refresh type and excluding already-selected keys.
     *
     * @param rolledRarity    the rarity to filter by
     * @param refreshType     the refresh type string (e.g. "DAILY") to filter by, or {@code null} to skip
     * @param excludeKeys     definition keys to exclude from the results
     * @return the list of eligible definition keys
     */
    @NotNull
    public List<NamespacedKey> getEligibleDefinitions(@NotNull NamespacedKey rolledRarity,
                                                      @Nullable String refreshType,
                                                      @NotNull Set<NamespacedKey> excludeKeys) {
        return definitionRegistry.getAll().stream()
                .filter(def -> !excludeKeys.contains(def.getQuestKey()))
                .filter(def -> def.getBoardMetadata()
                        .filter(meta -> meta.boardEligible()
                                && meta.supportedRarities().contains(rolledRarity)
                                && (refreshType == null
                                    || meta.supportedRefreshTypes().isEmpty()
                                    || meta.supportedRefreshTypes().contains(refreshType)))
                        .isPresent())
                .map(QuestDefinition::getQuestKey)
                .toList();
    }

    /**
     * Gets all board-eligible definition keys regardless of rarity or cooldown state.
     * Used for backfill when not enough quests match a specific rarity.
     *
     * @return the list of eligible definition keys
     */
    @NotNull
    public List<NamespacedKey> getAllBoardEligibleDefinitions() {
        return definitionRegistry.getAll().stream()
                .filter(def -> def.getBoardMetadata()
                        .map(BoardMetadata::boardEligible)
                        .orElse(false))
                .map(QuestDefinition::getQuestKey)
                .toList();
    }

    /**
     * Gets templates eligible for a specific rolled rarity.
     *
     * @param rolledRarity the rarity to filter by
     * @return the list of eligible templates
     */
    @NotNull
    public List<QuestTemplate> getEligibleTemplates(@NotNull NamespacedKey rolledRarity) {
        return filterByAvailability(templateRegistry.getEligibleTemplates(rolledRarity));
    }

    /**
     * Gets templates eligible for a specific rolled rarity and scope. When
     * {@code scopeProviderKey} is non-null, only templates with that scope are included
     * (prevents land-scoped templates from appearing on personal/shared boards).
     *
     * @param rolledRarity     the rarity to filter by
     * @param scopeProviderKey the scope to filter by, or {@code null} to skip scope filtering
     * @return the list of eligible templates
     */
    @NotNull
    public List<QuestTemplate> getEligibleTemplates(@NotNull NamespacedKey rolledRarity,
                                                     @Nullable NamespacedKey scopeProviderKey) {
        return filterByAvailability(templateRegistry.getEligibleTemplates(rolledRarity, scopeProviderKey));
    }

    /**
     * Gets templates eligible for a specific rolled rarity and scope, filtering by
     * template-level prerequisite conditions. Templates without a prerequisite are
     * always included. Templates with a prerequisite are included only if a non-null
     * context is provided and the prerequisite evaluates to {@code true}; when the
     * context is {@code null} (shared generation), prerequisite-gated templates are
     * excluded.
     *
     * @param rolledRarity     the rarity to filter by
     * @param scopeProviderKey the scope to filter by, or {@code null} to skip scope filtering
     * @param context          the condition context for prerequisite evaluation, or {@code null} for shared generation
     * @return the list of eligible templates passing both scope and prerequisite checks
     */
    @NotNull
    public List<QuestTemplate> getEligibleTemplates(@NotNull NamespacedKey rolledRarity,
                                                     @Nullable NamespacedKey scopeProviderKey,
                                                     @Nullable ConditionContext context) {
        return filterByAvailability(templateRegistry.getEligibleTemplates(rolledRarity, scopeProviderKey)).stream()
                .filter(t -> t.getPrerequisite()
                        .map(prereq -> context != null && prereq.evaluate(context))
                        .orElse(true))
                .toList();
    }

    /**
     * Attempts to generate a quest from a randomly selected eligible template.
     * Template generation failures are logged and return empty rather than propagating,
     * so a single broken template does not break the entire board.
     *
     * @param rolledRarity   the rolled rarity
     * @param random         the random source
     * @param templateEngine the template engine to use for generation
     * @return the generated result, or empty if no eligible templates or generation failed
     */
    @NotNull
    public Optional<GeneratedQuestResult> generateFromTemplate(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine) {
        return generateFromTemplate(rolledRarity, random, templateEngine, null);
    }

    /**
     * Attempts to generate a quest from a randomly selected eligible template, filtered to
     * templates that match the given scope provider. When {@code scopeProviderKey} is non-null,
     * only templates whose declared scope matches it are considered, preventing land-scoped
     * templates from appearing on personal or shared boards.
     *
     * @param rolledRarity     the rolled rarity
     * @param random           the random source
     * @param templateEngine   the template engine to use for generation
     * @param scopeProviderKey the scope to filter by, or {@code null} to skip scope filtering
     * @return the generated result, or empty if no eligible templates or generation failed
     */
    @NotNull
    public Optional<GeneratedQuestResult> generateFromTemplate(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            @Nullable NamespacedKey scopeProviderKey) {
        List<QuestTemplate> eligible = getEligibleTemplates(rolledRarity, scopeProviderKey);
        if (eligible.isEmpty()) return Optional.empty();

        QuestTemplate selected = eligible.get(random.nextInt(eligible.size()));
        try {
            return Optional.of(templateEngine.generate(selected, rolledRarity, random));
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Template generation failed for " + selected.getKey() + " with rarity " + rolledRarity, e);
            return Optional.empty();
        }
    }

    /**
     * Unified slot selection that treats hand-crafted definitions and templates as equal
     * sources, weighted by configurable source weights. For the given rolled rarity:
     * <ol>
     *   <li>Gather eligible hand-crafted definitions and eligible templates.</li>
     *   <li>If both pools have candidates, use {@code hcWeight} and {@code templateWeight}
     *       to probabilistically choose which source to draw from.</li>
     *   <li>If only one pool has candidates, use that pool regardless of weights.</li>
     *   <li>If neither pool has candidates for the rolled rarity, fall back to all-rarity
     *       hand-crafted definitions as a backfill.</li>
     *   <li>If the chosen source fails (e.g. template generation error), fall back to
     *       the other source.</li>
     * </ol>
     *
     * @param rolledRarity   the rarity rolled for this slot
     * @param random         the random source
     * @param templateEngine the template engine for generating from templates
     * @param hcWeight       the configured weight for hand-crafted quest selection
     * @param templateWeight the configured weight for template quest selection
     * @param refreshType    the refresh type string (e.g. "DAILY") for filtering, or {@code null} to skip
     * @param excludeKeys    definition keys already selected this rotation to prevent duplicates
     * @return the selection result, or empty if no quest could be produced for this slot
     */
    @NotNull
    public Optional<SlotSelection> selectForSlot(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight,
            @Nullable String refreshType,
            @NotNull Set<NamespacedKey> excludeKeys) {
        return selectForSlot(rolledRarity, random, templateEngine, hcWeight, templateWeight,
                refreshType, excludeKeys, null);
    }

    /**
     * Scope-aware overload of {@link #selectForSlot(NamespacedKey, Random, QuestTemplateEngine, int, int, String, Set)}.
     * When {@code scopeProviderKey} is non-null, only templates whose declared scope matches it are
     * considered, preventing land-scoped templates from appearing on personal or shared boards.
     *
     * @param rolledRarity     the rarity rolled for this slot
     * @param random           the random source
     * @param templateEngine   the template engine for generating from templates
     * @param hcWeight         the configured weight for hand-crafted quest selection
     * @param templateWeight   the configured weight for template quest selection
     * @param refreshType      the refresh type string (e.g. "DAILY") for filtering, or {@code null} to skip
     * @param excludeKeys      definition keys already selected this rotation to prevent duplicates
     * @param scopeProviderKey the scope to filter templates by, or {@code null} to skip scope filtering
     * @return the selection result, or empty if no quest could be produced for this slot
     */
    @NotNull
    public Optional<SlotSelection> selectForSlot(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight,
            @Nullable String refreshType,
            @NotNull Set<NamespacedKey> excludeKeys,
            @Nullable NamespacedKey scopeProviderKey) {

        List<NamespacedKey> hcEligible = getEligibleDefinitions(rolledRarity, refreshType, excludeKeys);
        List<QuestTemplate> tmplEligible = getEligibleTemplates(rolledRarity, scopeProviderKey);

        boolean hasHc = !hcEligible.isEmpty();
        boolean hasTmpl = !tmplEligible.isEmpty();

        if (!hasHc && !hasTmpl) {
            return backfillFromAnyRarity(rolledRarity, random, excludeKeys);
        }

        boolean chooseHandCrafted = resolveSourceChoice(hasHc, hasTmpl, hcWeight, templateWeight, random);

        if (chooseHandCrafted) {
            NamespacedKey selected = hcEligible.get(random.nextInt(hcEligible.size()));
            return Optional.of(new SlotSelection.HandCrafted(selected, rolledRarity));
        } else {
            Optional<GeneratedQuestResult> generated = generateFromTemplate(rolledRarity, random, templateEngine, scopeProviderKey);
            if (generated.isPresent() && !fireTemplateEventCancelled(generated.get(), rolledRarity)) {
                return Optional.of(new SlotSelection.TemplateGenerated(generated.get(), rolledRarity));
            }
            if (hasHc) {
                NamespacedKey selected = hcEligible.get(random.nextInt(hcEligible.size()));
                return Optional.of(new SlotSelection.HandCrafted(selected, rolledRarity));
            }
            return Optional.empty();
        }
    }

    /**
     * Overload for backward compatibility (no refresh type filter or exclusion).
     */
    @NotNull
    public Optional<SlotSelection> selectForSlot(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight) {
        return selectForSlot(rolledRarity, random, templateEngine, hcWeight, templateWeight, null, Set.of(), null);
    }

    /**
     * Overload that includes player context for personal offering generation.
     * Player-dependent conditions (permissions, completion prerequisites) are
     * evaluated against the given player during template generation.
     *
     * @param rolledRarity       the rarity rolled for this slot
     * @param random             the random source
     * @param templateEngine     the template engine
     * @param hcWeight           hand-crafted weight
     * @param templateWeight     template weight
     * @param playerUUID         the player UUID, or {@code null} for shared generation
     * @param completionHistory  the completion history, or {@code null}
     * @return the selection result, or empty
     */
    @NotNull
    public Optional<SlotSelection> selectForSlot(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight,
            @Nullable UUID playerUUID,
            @Nullable QuestCompletionHistory completionHistory) {
        return selectForSlot(rolledRarity, random, templateEngine, hcWeight, templateWeight,
                playerUUID, completionHistory, null);
    }

    /**
     * Scope-aware overload of {@link #selectForSlot(NamespacedKey, Random, QuestTemplateEngine, int, int, UUID, QuestCompletionHistory)}.
     * When {@code scopeProviderKey} is non-null, only templates whose declared scope matches it are
     * considered, preventing land-scoped templates from appearing on personal boards.
     *
     * @param rolledRarity       the rarity rolled for this slot
     * @param random             the random source
     * @param templateEngine     the template engine
     * @param hcWeight           hand-crafted weight
     * @param templateWeight     template weight
     * @param playerUUID         the player UUID, or {@code null} for shared generation
     * @param completionHistory  the completion history, or {@code null}
     * @param scopeProviderKey   the scope to filter templates by, or {@code null} to skip scope filtering
     * @return the selection result, or empty
     */
    @NotNull
    public Optional<SlotSelection> selectForSlot(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            int hcWeight,
            int templateWeight,
            @Nullable UUID playerUUID,
            @Nullable QuestCompletionHistory completionHistory,
            @Nullable NamespacedKey scopeProviderKey) {

        List<NamespacedKey> hcEligible = getEligibleDefinitions(rolledRarity, null, Set.of());
        ConditionContext prereqContext = (playerUUID != null && completionHistory != null)
                ? ConditionContext.forPrerequisiteCheck(playerUUID, completionHistory)
                : null;
        List<QuestTemplate> tmplEligible = getEligibleTemplates(rolledRarity, scopeProviderKey, prereqContext);

        boolean hasHc = !hcEligible.isEmpty();
        boolean hasTmpl = !tmplEligible.isEmpty();

        if (!hasHc && !hasTmpl) {
            return backfillFromAnyRarity(rolledRarity, random, Set.of());
        }

        boolean chooseHandCrafted = resolveSourceChoice(hasHc, hasTmpl, hcWeight, templateWeight, random);

        if (chooseHandCrafted) {
            NamespacedKey selected = hcEligible.get(random.nextInt(hcEligible.size()));
            return Optional.of(new SlotSelection.HandCrafted(selected, rolledRarity));
        } else {
            Optional<GeneratedQuestResult> generated = generateFromTemplate(
                    rolledRarity, random, templateEngine, playerUUID, completionHistory, scopeProviderKey);
            if (generated.isPresent() && !fireTemplateEventCancelled(generated.get(), rolledRarity)) {
                return Optional.of(new SlotSelection.TemplateGenerated(generated.get(), rolledRarity));
            }
            if (hasHc) {
                NamespacedKey selected = hcEligible.get(random.nextInt(hcEligible.size()));
                return Optional.of(new SlotSelection.HandCrafted(selected, rolledRarity));
            }
            return Optional.empty();
        }
    }

    /**
     * Overload of {@link #generateFromTemplate} that passes player context through
     * to the template engine for player-dependent condition evaluation. Also filters
     * templates by prerequisite using the player's completion history.
     */
    @NotNull
    private Optional<GeneratedQuestResult> generateFromTemplate(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            @Nullable UUID playerUUID,
            @Nullable QuestCompletionHistory completionHistory) {
        return generateFromTemplate(rolledRarity, random, templateEngine, playerUUID, completionHistory, null);
    }

    /**
     * Scope-aware overload of the private {@code generateFromTemplate} that also filters
     * templates by the given scope provider key.
     */
    @NotNull
    private Optional<GeneratedQuestResult> generateFromTemplate(
            @NotNull NamespacedKey rolledRarity,
            @NotNull Random random,
            @NotNull QuestTemplateEngine templateEngine,
            @Nullable UUID playerUUID,
            @Nullable QuestCompletionHistory completionHistory,
            @Nullable NamespacedKey scopeProviderKey) {
        ConditionContext prereqContext = (playerUUID != null && completionHistory != null)
                ? ConditionContext.forPrerequisiteCheck(playerUUID, completionHistory)
                : null;
        List<QuestTemplate> eligible = getEligibleTemplates(rolledRarity, scopeProviderKey, prereqContext);
        if (eligible.isEmpty()) return Optional.empty();

        QuestTemplate selected = eligible.get(random.nextInt(eligible.size()));
        try {
            return Optional.of(templateEngine.generate(
                    selected, rolledRarity, random, playerUUID, completionHistory));
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Template generation failed for " + selected.getKey() + " with rarity " + rolledRarity, e);
            return Optional.empty();
        }
    }

    /**
     * Fires {@link TemplateQuestGenerateEvent} for a generated result and returns whether
     * the event was cancelled. If the template is no longer registered, the event is not
     * fired and {@code false} is returned so the result is still used.
     *
     * @param result       the generated quest result
     * @param rolledRarity the rarity used during generation
     * @return {@code true} if the event was fired and cancelled; {@code false} otherwise
     */
    private boolean fireTemplateEventCancelled(@NotNull GeneratedQuestResult result,
                                               @NotNull NamespacedKey rolledRarity) {
        Optional<QuestTemplate> template = templateRegistry.get(result.templateKey());
        if (template.isEmpty()) return false;
        TemplateQuestGenerateEvent event = new TemplateQuestGenerateEvent(
                template.get(), rolledRarity, result.definition());
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    /**
     * Determines whether to choose hand-crafted or template based on source weights
     * and pool availability.
     *
     * @param hasHc          whether hand-crafted candidates exist
     * @param hasTmpl        whether template candidates exist
     * @param hcWeight       configured hand-crafted weight
     * @param templateWeight configured template weight
     * @param random         the random source
     * @return {@code true} to choose hand-crafted, {@code false} to choose template
     */
    private boolean resolveSourceChoice(boolean hasHc, boolean hasTmpl,
                                        int hcWeight, int templateWeight,
                                        @NotNull Random random) {
        if (hasHc && !hasTmpl) return true;
        if (!hasHc && hasTmpl) return false;

        int effectiveHc = Math.max(0, hcWeight);
        int effectiveTmpl = Math.max(0, templateWeight);
        int total = effectiveHc + effectiveTmpl;

        if (total <= 0) return random.nextBoolean();

        return random.nextInt(total) < effectiveHc;
    }

    /**
     * Filters out templates whose availability window is not currently active.
     * Templates without an {@link us.eunoians.mcrpg.quest.chain.availability.AvailabilityConfig}
     * are always included (no time restriction). Templates with an availability config
     * are included only if at least one window is currently active.
     *
     * @param templates the unfiltered template list
     * @return a new list containing only currently-available templates
     */
    @NotNull
    private List<QuestTemplate> filterByAvailability(@NotNull List<QuestTemplate> templates) {
        return templates.stream()
                .filter(t -> t.getAvailabilityConfig()
                        .map(config -> config.isCurrentlyAvailable(
                                timeProvider.now().atZone(config.timezone())))
                        .orElse(true))
                .toList();
    }

    /**
     * Backfill: when neither hand-crafted nor templates match the rolled rarity,
     * try all board-eligible hand-crafted definitions regardless of rarity (but still excluding
     * already-selected keys).
     */
    @NotNull
    private Optional<SlotSelection> backfillFromAnyRarity(@NotNull NamespacedKey rolledRarity,
                                                          @NotNull Random random,
                                                          @NotNull Set<NamespacedKey> excludeKeys) {
        List<NamespacedKey> allEligible = getAllBoardEligibleDefinitions().stream()
                .filter(key -> !excludeKeys.contains(key))
                .toList();
        if (allEligible.isEmpty()) return Optional.empty();
        NamespacedKey selected = allEligible.get(random.nextInt(allEligible.size()));
        return Optional.of(new SlotSelection.HandCrafted(selected, rolledRarity));
    }
}
