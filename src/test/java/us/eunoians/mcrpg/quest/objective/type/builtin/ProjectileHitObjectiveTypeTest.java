package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectileHitObjectiveTypeTest extends McRPGBaseTest {

    private ProjectileHitObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new ProjectileHitObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns projectile_hit key")
        @Test
        public void getKey_returnsProjectileHitKey() {
            assertEquals(ProjectileHitObjectiveType.KEY, type.getKey());
        }

        @DisplayName("getExpansionKey returns McRPGExpansion key")
        @Test
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("CanProcess")
    class CanProcess {

        @DisplayName("canProcess returns true for ProjectileHitQuestContext")
        @Test
        public void canProcess_returnsTrue_forProjectileHitContext() {
            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("canProcess returns false for other context type")
        @Test
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("ProcessProgress")
    class ProcessProgress {

        @DisplayName("processProgress returns 0 for wrong context type")
        @Test
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, wrongContext));
        }

        @DisplayName("processProgress returns 0 when hit entity is null")
        @Test
        public void processProgress_returnsZero_whenHitEntityIsNull() {
            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(null);
            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 1 for any entity hit with empty filters")
        @Test
        public void processProgress_returnsOne_whenNoFilters() {
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);
            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("parseConfig with empty section accepts any hit")
        @Test
        public void parseConfig_acceptsAnyHit_whenSectionEmpty() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(false);
            when(section.contains("entities")).thenReturn(false);

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with projectile filter accepts matching projectile")
        @Test
        public void parseConfig_acceptsMatchingProjectile() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("ARROW"));
            when(section.contains("entities")).thenReturn(false);

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with projectile filter rejects non-matching projectile")
        @Test
        public void parseConfig_rejectsNonMatchingProjectile() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("TRIDENT"));
            when(section.contains("entities")).thenReturn(false);

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with entity filter accepts matching entity")
        @Test
        public void parseConfig_acceptsMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(false);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with entity filter rejects non-matching entity")
        @Test
        public void parseConfig_rejectsNonMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(false);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("SKELETON"));

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with both filters requires both to match")
        @Test
        public void parseConfig_requiresBothMatch_whenBothFiltersSet() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("ARROW"));
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with both filters rejects when projectile mismatches")
        @Test
        public void parseConfig_rejects_whenProjectileMismatchesWithBothFilters() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("TRIDENT"));
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig is case-insensitive for projectile type names")
        @Test
        public void parseConfig_caseInsensitive_forProjectileTypeNames() {
            Section section = mock(Section.class);
            when(section.contains("projectiles")).thenReturn(true);
            when(section.getStringList("projectiles")).thenReturn(List.of("arrow"));
            when(section.contains("entities")).thenReturn(false);

            ProjectileHitObjectiveType configured = type.parseConfig(section);

            Arrow projectile = mock(Arrow.class);
            when(projectile.getType()).thenReturn(EntityType.ARROW);
            Zombie hitEntity = mock(Zombie.class);
            when(hitEntity.getType()).thenReturn(EntityType.ZOMBIE);

            ProjectileHitEvent mockEvent = mock(ProjectileHitEvent.class);
            when(mockEvent.getHitEntity()).thenReturn(hitEntity);
            when(mockEvent.getEntity()).thenReturn(projectile);

            ProjectileHitQuestContext context = new ProjectileHitQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }
}
