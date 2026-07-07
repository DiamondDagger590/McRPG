package us.eunoians.mcrpg.expansion.content;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.ContentExpansion;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("McRPGContentPack")
public class McRPGContentPackTest {

    private ContentExpansion mockExpansion;

    @BeforeEach
    public void setup() {
        mockExpansion = mock(ContentExpansion.class);
    }

    @Nested
    @DisplayName("getContentExpansion")
    class GetContentExpansion {

        @Test
        @DisplayName("returns constructor expansion")
        public void getContentExpansion_returnsConstructorExpansion() {
            var pack = new TestContentPack(mockExpansion);
            assertEquals(mockExpansion, pack.getContentExpansion());
        }
    }

    @Nested
    @DisplayName("getContent")
    class GetContent {

        @Test
        @DisplayName("empty pack returns empty set")
        public void getContent_emptyPack_returnsEmptySet() {
            var pack = new TestContentPack(mockExpansion);
            assertTrue(pack.getContent().isEmpty());
        }

        @Test
        @DisplayName("returns immutable set")
        public void getContent_returnsImmutableSet() {
            var pack = new TestContentPack(mockExpansion);
            Set<TestContent> content = pack.getContent();
            assertThrows(UnsupportedOperationException.class, () -> content.add(new TestContent()));
        }

        @Test
        @DisplayName("returns defensive copy")
        public void getContent_returnsDefensiveCopy() {
            var pack = new TestContentPack(mockExpansion);
            pack.addContent(new TestContent());
            Set<TestContent> first = pack.getContent();
            Set<TestContent> second = pack.getContent();
            assertNotSame(first, second);
            assertEquals(first, second);
        }
    }

    @Nested
    @DisplayName("addContent")
    class AddContent {

        @Test
        @DisplayName("single item increases size to one")
        public void addContent_singleItem_sizeOne() {
            var pack = new TestContentPack(mockExpansion);
            pack.addContent(new TestContent());
            assertEquals(1, pack.getContent().size());
        }

        @Test
        @DisplayName("multiple items increases size")
        public void addContent_multipleItems_correctSize() {
            var pack = new TestContentPack(mockExpansion);
            pack.addContent(new TestContent());
            pack.addContent(new TestContent());
            pack.addContent(new TestContent());
            assertEquals(3, pack.getContent().size());
        }

        @Test
        @DisplayName("duplicate item is deduplicated by set")
        public void addContent_duplicateItem_deduplicated() {
            var pack = new TestContentPack(mockExpansion);
            TestContent content = new TestContent();
            pack.addContent(content);
            pack.addContent(content);
            assertEquals(1, pack.getContent().size());
        }

        @Test
        @DisplayName("added item is retrievable")
        public void addContent_itemIsRetrievable() {
            var pack = new TestContentPack(mockExpansion);
            TestContent content = new TestContent();
            pack.addContent(content);
            assertTrue(pack.getContent().contains(content));
        }
    }

    private static final class TestContentPack extends McRPGContentPack<TestContent> {
        public TestContentPack(@NotNull ContentExpansion contentExpansion) {
            super(contentExpansion);
        }
    }

    private static final class TestContent implements McRPGContent {
        @Override
        @NotNull
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
