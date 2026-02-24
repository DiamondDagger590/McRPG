package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestTemplateRegistryTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");

    private QuestTemplateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestTemplateRegistry();
    }

    @Test
    @DisplayName("Register and retrieve template by key")
    void register_andGet_returnsTemplate() {
        QuestTemplate template = createTemplate("mcrpg:test_a", true, Set.of(COMMON));
        registry.register(template);

        Optional<QuestTemplate> result = registry.get(NamespacedKey.fromString("mcrpg:test_a"));
        assertTrue(result.isPresent());
        assertEquals(template, result.get());
    }

    @Test
    @DisplayName("Get for unregistered key returns empty")
    void get_unregisteredKey_returnsEmpty() {
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:missing")).isEmpty());
    }

    @Test
    @DisplayName("getAll returns all registered templates")
    void getAll_returnsAllTemplates() {
        registry.register(createTemplate("mcrpg:a", true, Set.of(COMMON)));
        registry.register(createTemplate("mcrpg:b", true, Set.of(COMMON)));

        Collection<QuestTemplate> all = registry.getAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("getEligibleTemplates filters by boardEligible and supportedRarities")
    void getEligibleTemplates_filtersByBoardEligibleAndRarity() {
        registry.register(createTemplate("mcrpg:eligible", true, Set.of(COMMON, RARE)));
        registry.register(createTemplate("mcrpg:not_eligible", false, Set.of(COMMON)));
        registry.register(createTemplate("mcrpg:wrong_rarity", true, Set.of(RARE)));

        List<QuestTemplate> eligible = registry.getEligibleTemplates(COMMON);
        assertEquals(1, eligible.size());
        assertEquals(NamespacedKey.fromString("mcrpg:eligible"), eligible.get(0).getKey());
    }

    @Test
    @DisplayName("replaceConfigTemplates replaces config-loaded, preserves programmatically registered")
    void replaceConfigTemplates_preservesProgrammatic() {
        QuestTemplate programmatic = createTemplate("mcrpg:programmatic", true, Set.of(COMMON));
        registry.register(programmatic);

        QuestTemplate configLoaded = createTemplate("mcrpg:config_a", true, Set.of(COMMON));
        registry.replaceConfigTemplates(Map.of(configLoaded.getKey(), configLoaded));

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:programmatic")).isPresent());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:config_a")).isPresent());

        QuestTemplate configReplace = createTemplate("mcrpg:config_b", true, Set.of(RARE));
        registry.replaceConfigTemplates(Map.of(configReplace.getKey(), configReplace));

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:programmatic")).isPresent());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:config_b")).isPresent());
        assertTrue(registry.get(NamespacedKey.fromString("mcrpg:config_a")).isEmpty());
    }

    @Test
    @DisplayName("registerTemplateDirectory adds to expansion directories list")
    void registerTemplateDirectory_addsToDirList() {
        File dir = new File("/tmp/test-templates");
        registry.registerTemplateDirectory(dir);

        List<File> dirs = registry.getExpansionDirectories();
        assertEquals(1, dirs.size());
        assertEquals(dir, dirs.get(0));
    }

    @Test
    @DisplayName("registerTemplateDirectory with duplicate directory does not add duplicate")
    void registerTemplateDirectory_noDuplicate() {
        File dir = new File("/tmp/test-templates");
        registry.registerTemplateDirectory(dir);
        registry.registerTemplateDirectory(dir);

        assertEquals(1, registry.getExpansionDirectories().size());
    }

    @Test
    @DisplayName("getExpansionDirectories returns defensive copy")
    void getExpansionDirectories_defensiveCopy() {
        File dir = new File("/tmp/test-templates");
        registry.registerTemplateDirectory(dir);

        List<File> copy = registry.getExpansionDirectories();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(new File("/other")));
    }

    @Test
    @DisplayName("clear empties the registry")
    void clear_emptiesRegistry() {
        registry.register(createTemplate("mcrpg:x", true, Set.of(COMMON)));
        registry.replaceConfigTemplates(Map.of(
                NamespacedKey.fromString("mcrpg:y"), createTemplate("mcrpg:y", true, Set.of(COMMON))));

        registry.clear();
        assertTrue(registry.getAll().isEmpty());
    }

    @Test
    @DisplayName("registered returns true for existing template")
    void registered_trueForExisting() {
        QuestTemplate template = createTemplate("mcrpg:exists", true, Set.of(COMMON));
        registry.register(template);

        assertTrue(registry.registered(template));
    }

    @Test
    @DisplayName("registered returns false for missing template")
    void registered_falseForMissing() {
        QuestTemplate template = createTemplate("mcrpg:nope", true, Set.of(COMMON));
        assertFalse(registry.registered(template));
    }

    private QuestTemplate createTemplate(String keyStr, boolean boardEligible, Set<NamespacedKey> rarities) {
        NamespacedKey key = NamespacedKey.fromString(keyStr);
        TemplateObjectiveDefinition obj = new TemplateObjectiveDefinition(
                NamespacedKey.fromString("mcrpg:block_break"), "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(obj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage));

        Map<String, TemplateVariable> vars = Map.of("count", new RangeVariable("count", 1, 10));

        return new QuestTemplate(key, Route.fromString("test.display"), boardEligible,
                NamespacedKey.fromString("mcrpg:single_player"), rarities, Map.of(),
                vars, List.of(phase), List.of());
    }
}
