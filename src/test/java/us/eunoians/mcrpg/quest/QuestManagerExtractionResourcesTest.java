package us.eunoians.mcrpg.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the default quest-resource extraction list stays in sync with the packaged jar
 * resources. A missing resource would cause {@code plugin.saveResource} to throw at startup, and an
 * omitted tutorial/guide entry silently reproduces the shipped-but-unreachable bugs from backlog #282.
 */
class QuestManagerExtractionResourcesTest {

    /**
     * Ensures the tutorial chain, a representative tutorial quest, the example chain, and the owner
     * guides are all present in the extraction list so they reach a real server's data folder.
     */
    @Test
    @DisplayName("extraction list includes tutorial, example chain, and owner guides")
    void getDefaultExtractionResources_includesTutorialAndGuides() {
        List<String> resources = QuestManager.getDefaultExtractionResources();

        assertTrue(resources.contains("quests/tutorial/chain.yml"),
                "tutorial chain must be extracted so the first-join tutorial fires on real servers");
        assertTrue(resources.contains("quests/tutorial/first_steps.yml"),
                "tutorial quests must be extracted alongside the tutorial chain");
        assertTrue(resources.contains("quests/example_chain.yml"),
                "example chain must be extracted as a chain reference for owners");
        assertTrue(resources.contains("quests/SERVER-OWNER-GUIDE.md"),
                "owner guides must be extracted so they are reachable from an installed server");
        assertTrue(resources.contains("quests/REWARDS.md"),
                "reward guide must be extracted");
    }

    /**
     * Ensures every path in the extraction list resolves to a real classpath (jar) resource, so
     * {@code saveResource} cannot throw {@code IllegalArgumentException} for a stale path at startup.
     */
    @Test
    @DisplayName("every extraction resource exists on the classpath")
    void getDefaultExtractionResources_allResolveOnClasspath() {
        List<String> resources = QuestManager.getDefaultExtractionResources();

        for (String resource : resources) {
            assertNotNull(getClass().getClassLoader().getResource(resource),
                    "bundled resource missing from jar: " + resource);
        }
    }
}
