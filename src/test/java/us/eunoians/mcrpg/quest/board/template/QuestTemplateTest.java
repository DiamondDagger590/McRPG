package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestTemplateTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey UNSUPPORTED = NamespacedKey.fromString("mcrpg:epic");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("mcrpg:mcrpg");
    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:test_template");

    @Test
    @DisplayName("Given all fields, when QuestTemplate is constructed, then construction succeeds")
    void constructionWithAllFields_succeeds() {
        QuestTemplate template = createMinimalTemplate();

        assertEquals(TEMPLATE_KEY, template.getKey());
        assertEquals(Route.fromString("quests.templates.test.display-name"), template.getDisplayNameRoute());
        assertNotNull(template.getSupportedRarities());
        assertNotNull(template.getRarityOverrides());
        assertNotNull(template.getVariables());
        assertNotNull(template.getPhases());
        assertNotNull(template.getRewards());
    }

    @Test
    @DisplayName("Given rarity override for difficulty, when getEffectiveDifficultyMultiplier is called, then returns override value")
    void getEffectiveDifficultyMultiplier_withOverride_returnsOverride() {
        RarityOverride override = new RarityOverride(2.5, null);
        QuestTemplate template = createTemplateWithOverride(COMMON, override);
        QuestRarityRegistry registry = mock(QuestRarityRegistry.class);
        when(registry.get(COMMON)).thenReturn(Optional.of(new QuestRarity(COMMON, 10, 1.5, 1.2, EXPANSION_KEY)));

        double result = template.getEffectiveDifficultyMultiplier(COMMON, registry);

        assertEquals(2.5, result);
    }

    @Test
    @DisplayName("Given no override for difficulty, when getEffectiveDifficultyMultiplier is called, then returns global rarity value")
    void getEffectiveDifficultyMultiplier_withoutOverride_returnsGlobalRarity() {
        QuestTemplate template = createMinimalTemplate();
        QuestRarityRegistry registry = mock(QuestRarityRegistry.class);
        QuestRarity rarity = new QuestRarity(COMMON, 10, 1.5, 1.2, EXPANSION_KEY);
        when(registry.get(COMMON)).thenReturn(Optional.of(rarity));

        double result = template.getEffectiveDifficultyMultiplier(COMMON, registry);

        assertEquals(1.5, result);
    }

    @Test
    @DisplayName("Given rarity override for reward, when getEffectiveRewardMultiplier is called, then returns override value")
    void getEffectiveRewardMultiplier_withOverride_returnsOverride() {
        RarityOverride override = new RarityOverride(null, 3.0);
        QuestTemplate template = createTemplateWithOverride(COMMON, override);
        QuestRarityRegistry registry = mock(QuestRarityRegistry.class);
        when(registry.get(COMMON)).thenReturn(Optional.of(new QuestRarity(COMMON, 10, 1.5, 1.2, EXPANSION_KEY)));

        double result = template.getEffectiveRewardMultiplier(COMMON, registry);

        assertEquals(3.0, result);
    }

    @Test
    @DisplayName("Given no override for reward, when getEffectiveRewardMultiplier is called, then returns global rarity value")
    void getEffectiveRewardMultiplier_withoutOverride_returnsGlobalRarity() {
        QuestTemplate template = createMinimalTemplate();
        QuestRarityRegistry registry = mock(QuestRarityRegistry.class);
        QuestRarity rarity = new QuestRarity(COMMON, 10, 1.5, 1.2, EXPANSION_KEY);
        when(registry.get(COMMON)).thenReturn(Optional.of(rarity));

        double result = template.getEffectiveRewardMultiplier(COMMON, registry);

        assertEquals(1.2, result);
    }

    @Test
    @DisplayName("Given boardEligible true, when isBoardEligible is called, then returns true")
    void isBoardEligible_returnsCorrectValue_true() {
        QuestTemplate template = createTemplateWithBoardEligible(true);
        assertTrue(template.isBoardEligible());
    }

    @Test
    @DisplayName("Given boardEligible false, when isBoardEligible is called, then returns false")
    void isBoardEligible_returnsCorrectValue_false() {
        QuestTemplate template = createTemplateWithBoardEligible(false);
        assertFalse(template.isBoardEligible());
    }

    @Test
    @DisplayName("Given supported rarities, when getSupportedRarities is called, then returns correct set")
    void getSupportedRarities_filtering() {
        QuestTemplate template = createTemplateWithSupportedRarities(Set.of(COMMON, RARE));

        Set<NamespacedKey> supported = template.getSupportedRarities();
        assertEquals(2, supported.size());
        assertTrue(supported.contains(COMMON));
        assertTrue(supported.contains(RARE));
        assertFalse(supported.contains(UNSUPPORTED));
    }

    @Test
    @DisplayName("Given supported rarity, when validateRaritySupported is called, then no exception is thrown")
    void validateRaritySupported_withSupportedRarity_noException() {
        QuestTemplate template = createTemplateWithSupportedRarities(Set.of(COMMON, RARE));

        assertDoesNotThrow(() -> template.validateRaritySupported(COMMON));
        assertDoesNotThrow(() -> template.validateRaritySupported(RARE));
    }

    @Test
    @DisplayName("Given unsupported rarity, when validateRaritySupported is called, then throws IllegalArgumentException")
    void validateRaritySupported_withUnsupportedRarity_throws() {
        QuestTemplate template = createTemplateWithSupportedRarities(Set.of(COMMON, RARE));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> template.validateRaritySupported(UNSUPPORTED));

        assertTrue(ex.getMessage().contains("does not support rarity"));
        assertTrue(ex.getMessage().contains(UNSUPPORTED.toString()));
    }

    private QuestTemplate createMinimalTemplate() {
        return createTemplateWithBoardEligible(true);
    }

    private QuestTemplate createTemplateWithBoardEligible(boolean boardEligible) {
        return createTemplateWithSupportedRaritiesAndBoardEligible(Set.of(COMMON, RARE), boardEligible, Map.of());
    }

    private QuestTemplate createTemplateWithSupportedRarities(Set<NamespacedKey> supportedRarities) {
        return createTemplateWithSupportedRaritiesAndBoardEligible(supportedRarities, true, Map.of());
    }

    private QuestTemplate createTemplateWithOverride(NamespacedKey key, RarityOverride override) {
        return createTemplateWithSupportedRaritiesAndBoardEligible(
                Set.of(COMMON, RARE), true, Map.of(key, override));
    }

    private QuestTemplate createTemplateWithSupportedRaritiesAndBoardEligible(
            Set<NamespacedKey> supportedRarities, boolean boardEligible,
            Map<NamespacedKey, RarityOverride> rarityOverrides) {

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                NamespacedKey.fromString("mcrpg:block_break"),
                "block_count",
                Map.of("blocks", "target_blocks"));

        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        TemplateRewardDefinition reward = new TemplateRewardDefinition(
                NamespacedKey.fromString("mcrpg:experience"),
                Map.of("amount", "block_count * 5"));

        Map<String, TemplateVariable> variables = Map.of("block_count", new RangeVariable("block_count", 10.0, 50.0));

        return new QuestTemplate(
                TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"),
                boardEligible,
                EXPANSION_KEY,
                supportedRarities,
                rarityOverrides,
                variables,
                List.of(phase),
                List.of(reward));
    }
}
