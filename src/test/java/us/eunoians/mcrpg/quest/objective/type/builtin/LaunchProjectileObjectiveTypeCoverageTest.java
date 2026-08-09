package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LaunchProjectileObjectiveType — extended coverage")
public class LaunchProjectileObjectiveTypeCoverageTest extends McRPGBaseTest {

    private LaunchProjectileObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new LaunchProjectileObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for LaunchProjectileQuestContext")
        public void canProcess_returnsTrue_forLaunchProjectileContext() {
            ProjectileLaunchEvent mockEvent = mock(ProjectileLaunchEvent.class);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BlockBreakQuestContext")
        public void canProcess_returnsFalse_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty projectile filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any projectile when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.ARROW);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for snowball when no filter is set")
        public void processProgress_returnsOne_forSnowball_whenNoFilter() {
            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.SNOWBALL);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with projectile filter via parseConfig")
    class ProcessProgressWithFilter {

        private LaunchProjectileObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("ARROW", "SNOWBALL"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching projectile")
        public void processProgress_returnsOne_forMatchingProjectile() {
            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.ARROW);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching projectile")
        public void processProgress_returnsZero_forNonMatchingProjectile() {
            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.EGG);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching projectile in filter")
        public void processProgress_returnsOne_forSecondMatchingProjectile() {
            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.SNOWBALL);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for generic mock context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for BlockBreakQuestContext")
        public void processProgress_returnsZero_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is launch_projectile")
        public void getKey_valueIsLaunchProjectile() {
            assertEquals("launch_projectile", baseType.getKey().getKey());
        }

        @Test
        @DisplayName("expansion key is present")
        public void getExpansionKey_isPresent() {
            assertTrue(baseType.getExpansionKey().isPresent());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(false);

            LaunchProjectileObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no projectiles key produces empty filter that accepts any projectile")
        public void parseConfig_noProjectilesKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(false);

            LaunchProjectileObjectiveType parsed = baseType.parseConfig(section);

            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.EGG);
            LaunchProjectileQuestContext context = new LaunchProjectileQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("projectiles key with entries produces filtering type")
        public void parseConfig_withProjectiles_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("ARROW"));

            LaunchProjectileObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            ProjectileLaunchEvent matchEvent = createMockLaunchEvent(EntityType.ARROW);
            assertEquals(1, parsed.processProgress(instance, new LaunchProjectileQuestContext(matchEvent)));

            ProjectileLaunchEvent noMatchEvent = createMockLaunchEvent(EntityType.SNOWBALL);
            assertEquals(0, parsed.processProgress(instance, new LaunchProjectileQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("projectiles key with empty list produces empty filter")
        public void parseConfig_emptyProjectilesList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of());

            LaunchProjectileObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            ProjectileLaunchEvent event = createMockLaunchEvent(EntityType.ARROW);
            assertEquals(1, parsed.processProgress(instance, new LaunchProjectileQuestContext(event)));
        }
    }

    private ProjectileLaunchEvent createMockLaunchEvent(EntityType entityType) {
        ProjectileLaunchEvent event = mock(ProjectileLaunchEvent.class);
        Projectile projectile = mock(Projectile.class);
        when(projectile.getType()).thenReturn(entityType);
        when(event.getEntity()).thenReturn(projectile);
        return event;
    }
}
