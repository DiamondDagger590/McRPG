package us.eunoians.mcrpg.expansion.content;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.template.TemplateObjectiveDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplatePhaseDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateStageDefinition;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestTemplateContentPackTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("test:expansion");

    private QuestTemplateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestTemplateRegistry();
    }

    @Test
    @DisplayName("Content pack registers templates via QuestTemplateRegistry.register()")
    void contentPack_registersTemplates() {
        ContentExpansion expansion = mock(ContentExpansion.class);
        when(expansion.getExpansionKey()).thenReturn(EXPANSION_KEY);

        QuestTemplateContentPack pack = new QuestTemplateContentPack(expansion);
        QuestTemplate template = createTemplate("mcrpg:pack_template");
        pack.addContent(template);

        pack.getContent().forEach(registry::register);

        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:pack_template")).isPresent());
        assertEquals(template, registry.get(NamespacedKey.fromString("mcrpg:pack_template")).get());
    }

    @Test
    @DisplayName("Registered templates survive replaceConfigTemplates() reload")
    void registeredTemplates_surviveReload() {
        ContentExpansion expansion = mock(ContentExpansion.class);
        when(expansion.getExpansionKey()).thenReturn(EXPANSION_KEY);

        QuestTemplateContentPack pack = new QuestTemplateContentPack(expansion);
        QuestTemplate packTemplate = createTemplate("mcrpg:expansion_template");
        pack.addContent(packTemplate);
        pack.getContent().forEach(registry::register);

        QuestTemplate configTemplate = createTemplate("mcrpg:config_template");
        registry.replaceConfigTemplates(Map.of(configTemplate.getKey(), configTemplate));

        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:expansion_template")).isPresent());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:config_template")).isPresent());
        assertEquals(2, registry.getAll().size());

        registry.replaceConfigTemplates(Map.of());

        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:expansion_template")).isPresent());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:config_template")).isEmpty());
        assertEquals(1, registry.getAll().size());
    }

    @Test
    @DisplayName("Content pack getContent returns defensive copy")
    void contentPack_defensiveCopy() {
        ContentExpansion expansion = mock(ContentExpansion.class);
        when(expansion.getExpansionKey()).thenReturn(EXPANSION_KEY);

        QuestTemplateContentPack pack = new QuestTemplateContentPack(expansion);
        pack.addContent(createTemplate("mcrpg:a"));

        Set<QuestTemplate> content = pack.getContent();
        assertEquals(1, content.size());
        assertThrows(UnsupportedOperationException.class, () -> content.add(createTemplate("mcrpg:b")));
    }

    private QuestTemplate createTemplate(String keyStr) {
        NamespacedKey key = NamespacedKey.fromString(keyStr);
        TemplateObjectiveDefinition obj = new TemplateObjectiveDefinition(
                NamespacedKey.fromString("mcrpg:block_break"), "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(obj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage));
        Map<String, TemplateVariable> vars = Map.of("count", new RangeVariable("count", 1, 10));
        return new QuestTemplate(key, Route.fromString("test.display"), true,
                NamespacedKey.fromString("mcrpg:single_player"), Set.of(COMMON), Map.of(),
                vars, List.of(phase), List.of(), null, EXPANSION_KEY);
    }
}
