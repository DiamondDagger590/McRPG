package us.eunoians.mcrpg.quest.board.generation;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.board.TemplateQuestGenerateEvent;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates the logic of assembling the pool of eligible quest definitions and
 * templates for board generation. Draws from both {@link QuestDefinitionRegistry}
 * (hand-crafted quests) and {@link QuestTemplateRegistry} (template-generated quests).
 * <p>
 * Registries and the template engine are resolved from {@link RegistryAccess} on demand
 * rather than being passed as constructor arguments.
 */
public class QuestPool {

    private final QuestDefinitionRegistry definitionRegistry;

    public QuestPool(@NotNull QuestDefinitionRegistry definitionRegistry) {
        this.definitionRegistry = definitionRegistry;
    }

    private Logger logger() {
        return McRPG.getInstance().getLogger();
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
        QuestTemplateRegistry templateRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_TEMPLATE);
        return templateRegistry.getEligibleTemplates(rolledRarity);
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
        List<QuestTemplate> eligible = getEligibleTemplates(rolledRarity);
        if (eligible.isEmpty()) return Optional.empty();

        QuestTemplate selected = eligible.get(random.nextInt(eligible.size()));
        try {
            GeneratedQuestResult result = templateEngine.generate(selected, rolledRarity, random);

            TemplateQuestGenerateEvent event = new TemplateQuestGenerateEvent(
                    selected, rolledRarity, result.definition());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return Optional.empty();
            }

            return Optional.of(result);
        } catch (Exception e) {
            logger().log(Level.WARNING,
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

        List<NamespacedKey> hcEligible = getEligibleDefinitions(rolledRarity, refreshType, excludeKeys);
        List<QuestTemplate> tmplEligible = getEligibleTemplates(rolledRarity);

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
            Optional<GeneratedQuestResult> generated = generateFromTemplate(rolledRarity, random, templateEngine);
            if (generated.isPresent()) {
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
        return selectForSlot(rolledRarity, random, templateEngine, hcWeight, templateWeight, null, Set.of());
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
