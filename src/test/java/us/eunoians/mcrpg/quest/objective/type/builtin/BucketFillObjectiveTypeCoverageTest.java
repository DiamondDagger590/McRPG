package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
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

@DisplayName("BucketFillObjectiveType — extended coverage")
public class BucketFillObjectiveTypeCoverageTest extends McRPGBaseTest {

    private BucketFillObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new BucketFillObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for BucketFillQuestContext")
        public void canProcess_returnsTrue_forBucketFillContext() {
            PlayerBucketFillEvent mockEvent = mock(PlayerBucketFillEvent.class);
            BucketFillQuestContext context = new BucketFillQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BucketEmptyQuestContext")
        public void canProcess_returnsFalse_forBucketEmptyContext() {
            BucketEmptyQuestContext context = mock(BucketEmptyQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty bucket filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any bucket when no filter is set")
        public void processProgress_returnsOne_forWaterBucket_whenNoFilter() {
            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.WATER_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for lava bucket when no filter is set")
        public void processProgress_returnsOne_forLavaBucket_whenNoFilter() {
            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.LAVA_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with bucket filter via parseConfig")
    class ProcessProgressWithFilter {

        private BucketFillObjectiveType filteredType;

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
            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.WATER_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching bucket")
        public void processProgress_returnsZero_forNonMatchingBucket() {
            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.MILK_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching bucket in filter")
        public void processProgress_returnsOne_forSecondMatchingBucket() {
            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.LAVA_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for non-BucketFill context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for BucketEmptyQuestContext")
        public void processProgress_returnsZero_forBucketEmptyContext() {
            BucketEmptyQuestContext context = mock(BucketEmptyQuestContext.class);
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
        @DisplayName("key value is bucket_fill")
        public void getKey_valueIsBucketFill() {
            assertEquals("bucket_fill", baseType.getKey().getKey());
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

            BucketFillObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no buckets key produces empty filter that accepts any bucket")
        public void parseConfig_noBucketsKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(false);

            BucketFillObjectiveType parsed = baseType.parseConfig(section);

            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.POWDER_SNOW_BUCKET);
            BucketFillQuestContext context = new BucketFillQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("buckets key with entries produces filtering type")
        public void parseConfig_withBuckets_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of("WATER_BUCKET"));

            BucketFillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketFillEvent matchEvent = createMockBucketFillEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketFillQuestContext(matchEvent)));

            PlayerBucketFillEvent noMatchEvent = createMockBucketFillEvent(Material.LAVA_BUCKET);
            assertEquals(0, parsed.processProgress(instance, new BucketFillQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("buckets key with empty list produces empty filter")
        public void parseConfig_emptyBucketsList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of());

            BucketFillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketFillEvent event = createMockBucketFillEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketFillQuestContext(event)));
        }

        @Test
        @DisplayName("invalid material names are filtered out")
        public void parseConfig_invalidMaterialNames_areFilteredOut() {
            Section section = mock(Section.class);
            when(section.contains("buckets")).thenReturn(true);
            when(section.getStringList("buckets")).thenReturn(List.of("WATER_BUCKET", "FAKE_BUCKET_TYPE"));

            BucketFillObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerBucketFillEvent waterEvent = createMockBucketFillEvent(Material.WATER_BUCKET);
            assertEquals(1, parsed.processProgress(instance, new BucketFillQuestContext(waterEvent)));
        }
    }

    /**
     * @param filledBucketMaterial the material of the filled bucket item
     * @return a mock event whose {@code getItemStack().getType()} returns the given material
     */
    private PlayerBucketFillEvent createMockBucketFillEvent(Material filledBucketMaterial) {
        PlayerBucketFillEvent event = mock(PlayerBucketFillEvent.class);
        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.getType()).thenReturn(filledBucketMaterial);
        when(event.getItemStack()).thenReturn(itemStack);
        return event;
    }
}
