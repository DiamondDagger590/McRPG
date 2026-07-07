package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
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

@DisplayName("MobKillObjectiveType — extended coverage")
public class MobKillObjectiveTypeCoverageTest extends McRPGBaseTest {

    private MobKillObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new MobKillObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for MobKillQuestContext")
        public void canProcess_returnsTrue_forMobKillContext() {
            EntityDeathEvent mockEvent = mock(EntityDeathEvent.class);
            LivingEntity entity = mock(LivingEntity.class);
            when(mockEvent.getEntity()).thenReturn(entity);
            when(entity.getType()).thenReturn(EntityType.ZOMBIE);
            MobKillQuestContext context = new MobKillQuestContext(mockEvent);
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
    @DisplayName("processProgress — empty entity filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any mob when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            MobKillQuestContext context = createMobKillContext(EntityType.ZOMBIE);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for skeleton when no filter is set")
        public void processProgress_returnsOne_forSkeleton_whenNoFilter() {
            MobKillQuestContext context = createMobKillContext(EntityType.SKELETON);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with entity filter via parseConfig")
    class ProcessProgressWithFilter {

        private MobKillObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(false);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE", "SKELETON"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching entity")
        public void processProgress_returnsOne_forMatchingEntity() {
            MobKillQuestContext context = createMobKillContext(EntityType.ZOMBIE);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching entity")
        public void processProgress_returnsZero_forNonMatchingEntity() {
            MobKillQuestContext context = createMobKillContext(EntityType.CREEPER);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching entity in filter")
        public void processProgress_returnsOne_forSecondMatchingEntity() {
            MobKillQuestContext context = createMobKillContext(EntityType.SKELETON);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for non-MobKill context")
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
        @DisplayName("key value is mob_kill")
        public void getKey_valueIsMobKill() {
            assertEquals("mob_kill", baseType.getKey().getKey());
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
            when(section.contains("mobs")).thenReturn(false);
            when(section.contains("entities")).thenReturn(false);

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no entities key produces empty filter that accepts any mob")
        public void parseConfig_noEntitiesKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(false);
            when(section.contains("entities")).thenReturn(false);

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            MobKillQuestContext context = createMobKillContext(EntityType.SPIDER);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("entities key with entries produces filtering type")
        public void parseConfig_withEntities_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(false);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            MobKillQuestContext matchContext = createMobKillContext(EntityType.ZOMBIE);
            assertEquals(1, parsed.processProgress(instance, matchContext));

            MobKillQuestContext noMatchContext = createMobKillContext(EntityType.CREEPER);
            assertEquals(0, parsed.processProgress(instance, noMatchContext));
        }

        @Test
        @DisplayName("legacy mobs key is supported")
        public void parseConfig_legacyMobsKey_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(true);
            when(section.contains("entities")).thenReturn(false);
            when(section.getStringList("mobs")).thenReturn(List.of("ZOMBIE"));

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            MobKillQuestContext matchContext = createMobKillContext(EntityType.ZOMBIE);
            assertEquals(1, parsed.processProgress(instance, matchContext));

            MobKillQuestContext noMatchContext = createMobKillContext(EntityType.SKELETON);
            assertEquals(0, parsed.processProgress(instance, noMatchContext));
        }

        @Test
        @DisplayName("entities key with empty list produces empty filter")
        public void parseConfig_emptyEntitiesList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(false);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of());

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            MobKillQuestContext context = createMobKillContext(EntityType.ENDERMAN);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("mobs key takes precedence when both keys exist")
        public void parseConfig_mobsKeyPrecedence_whenBothExist() {
            Section section = mock(Section.class);
            when(section.contains("mobs")).thenReturn(true);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("mobs")).thenReturn(List.of("ZOMBIE"));

            MobKillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            MobKillQuestContext zombieContext = createMobKillContext(EntityType.ZOMBIE);
            assertEquals(1, parsed.processProgress(instance, zombieContext));
        }
    }

    private MobKillQuestContext createMobKillContext(EntityType entityType) {
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        LivingEntity entity = mock(LivingEntity.class);
        when(event.getEntity()).thenReturn(entity);
        when(entity.getType()).thenReturn(entityType);
        return new MobKillQuestContext(event);
    }
}
