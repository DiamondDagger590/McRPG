package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
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

@DisplayName("BucketEmptyObjectiveType — extended coverage")
public class BucketEmptyObjectiveTypeCoverageTest extends McRPGBaseTest {

    private BucketEmptyObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new BucketEmptyObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for BucketEmptyQuestContext")
        public void canProcess_returnsTrue_forBucketEmptyContext() {
            PlayerBucketEmptyEvent mockEvent = mock(PlayerBucketEmptyEvent.class);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BucketFillQuestContext")
        public void canProcess_returnsFalse_forBucketFillContext() {
            BucketFillQuestContext context = mock(BucketFillQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty bucket filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any bucket when no filter is set")
        public void processProgress_returnsOne_forWaterBucket_whenNoFilter() {
            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.WATER_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for lava bucket when no filter is set")
        public void processProgress_returnsOne_forLavaBucket_whenNoFilter() {
            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.LAVA_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with bucket filter via parseConfig")
    class ProcessProgressWithFilter {

        private BucketEmptyObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of("WATER_BUCKET", "LAVA_BUCKET"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching bucket")
        public void processProgress_returnsOne_forMatchingBucket() {
            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.WATER_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching bucket")
        public void processProgress_returnsZero_forNonMatchingBucket() {
            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.MILK_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching bucket in filter")
        public void processProgress_returnsOne_forSecondMatchingBucket() {
            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.LAVA_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for non-BucketEmpty context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for BucketFillQuestContext")
        public void processProgress_returnsZero_forBucketFillContext() {
            BucketFillQuestContext context = mock(BucketFillQuestContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is bucket_empty")
        public void getKey_valueIsBucketEmpty() {
            assertEquals("bucket_empty", baseType.getKey().getKey());
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
            when(section.contains("buckets")).thenReturn(false);

            BucketEmptyObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no buckets key produces empty filter that accepts any bucket")
        public void parseConfig_noBucketsKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(false);

            BucketEmptyObjectiveType parsed = baseType.parseConfig(section);

            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.POWDER_SNOW_BUCKET);
            BucketEmptyQuestContext context = new BucketEmptyQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("buckets key with entries produces filtering type")
        public void parseConfig_withBuckets_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of("WATER_BUCKET"));

            BucketEmptyObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketEmptyEvent matchEvent = createMockBucketEmptyEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketEmptyQuestContext(matchEvent)));

            PlayerBucketEmptyEvent noMatchEvent = createMockBucketEmptyEvent(Material.LAVA_BUCKET);
            assertEquals(0, parsed.processProgress(instance, new BucketEmptyQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("buckets key with empty list produces empty filter")
        public void parseConfig_emptyBucketsList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of());

            BucketEmptyObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketEmptyEvent event = createMockBucketEmptyEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketEmptyQuestContext(event)));
        }

        @Test
        @DisplayName("invalid material names are filtered out")
        public void parseConfig_invalidMaterialNames_areFilteredOut() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of("WATER_BUCKET", "NOT_A_REAL_MATERIAL"));

            BucketEmptyObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketEmptyEvent waterEvent = createMockBucketEmptyEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketEmptyQuestContext(waterEvent)));
        }
    }

    /**
     * @param bucketMaterial the material of the bucket being emptied
     * @return a mock event returning the given material from {@code getBucket()}
     */
    private PlayerBucketEmptyEvent createMockBucketEmptyEvent(Material bucketMaterial) {
        PlayerBucketEmptyEvent event = mock(PlayerBucketEmptyEvent.class);
        when(event.getBucket()).thenReturn(bucketMaterial);
        return event;
    }
}
