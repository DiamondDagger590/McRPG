package us.eunoians.mcrpg.builder.item;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link BaseItemBuilder#applyTagReplacements(Map)}.
 * <p>
 * Assertions read back the private {@code displayName} and {@code lore} string fields directly
 * via reflection to verify the method's string-replacement contract without going through
 * MiniMessage deserialization (which strips unknown tags and would obscure no-op assertions).
 * <p>
 * Extends {@link McRPGBaseTest} so that Bukkit is mocked and {@link ItemBuilder#from(ItemType)}
 * can construct items.
 */
class ItemBuilderTagReplacementTest extends McRPGBaseTest {

    /**
     * Reads the private {@code displayName} String field from the given builder.
     *
     * @param builder The builder to inspect.
     * @return The current value of the {@code displayName} field.
     */
    private String readDisplayName(ItemBuilder builder) throws Exception {
        Field field = BaseItemBuilder.class.getDeclaredField("displayName");
        field.setAccessible(true);
        return (String) field.get(builder);
    }

    /**
     * Reads the private {@code lore} List field from the given builder.
     *
     * @param builder The builder to inspect.
     * @return The current value of the {@code lore} field.
     */
    @SuppressWarnings("unchecked")
    private List<String> readLore(ItemBuilder builder) throws Exception {
        Field field = BaseItemBuilder.class.getDeclaredField("lore");
        field.setAccessible(true);
        return (List<String>) field.get(builder);
    }

    @Test
    @DisplayName("Given a display name with a palette placeholder, when applyTagReplacements is called, then the name is updated")
    void applyTagReplacements_displayName_placeholderIsReplaced() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .setDisplayName("<primary>Title");

        builder.applyTagReplacements(Map.of("<primary>", "<color:#D4A76A>"));

        assertEquals("<color:#D4A76A>Title", readDisplayName(builder));
    }

    @Test
    @DisplayName("Given lore lines with palette placeholders, when applyTagReplacements is called, then all lore lines are updated")
    void applyTagReplacements_loreLines_placeholdersAreReplaced() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .withDisplayLore(List.of("<body>First line", "<hint>Second line"));

        builder.applyTagReplacements(Map.of("<body>", "<gray>", "<hint>", "<color:#E8C97A>"));

        List<String> lore = readLore(builder);
        assertEquals("<gray>First line", lore.get(0));
        assertEquals("<color:#E8C97A>Second line", lore.get(1));
    }

    @Test
    @DisplayName("Given an empty replacement map, when applyTagReplacements is called, then the display name is unchanged")
    void applyTagReplacements_emptyMap_displayNameIsUnchanged() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .setDisplayName("<primary>Title");

        builder.applyTagReplacements(Map.of());

        assertEquals("<primary>Title", readDisplayName(builder));
    }

    @Test
    @DisplayName("Given an empty replacement map, when applyTagReplacements is called, then lore is unchanged")
    void applyTagReplacements_emptyMap_loreIsUnchanged() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .withDisplayLore(List.of("<body>Line"));

        builder.applyTagReplacements(Map.of());

        assertEquals("<body>Line", readLore(builder).get(0));
    }

    @Test
    @DisplayName("Given a replacement map with no matching tags, when applyTagReplacements is called, then the display name is unchanged")
    void applyTagReplacements_noMatchingTags_isNoOp() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .setDisplayName("<primary>Title");

        builder.applyTagReplacements(Map.of("<unknown>", "SomeValue"));

        assertEquals("<primary>Title", readDisplayName(builder));
    }

    @Test
    @DisplayName("Given a display name with multiple palette placeholders, when applyTagReplacements is called, then all are replaced")
    void applyTagReplacements_multipleTagsInDisplayName_allReplaced() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE)
                .setDisplayName("<primary>Name <body>label");

        builder.applyTagReplacements(Map.of("<primary>", "A", "<body>", "B"));

        assertEquals("AName Blabel", readDisplayName(builder));
    }

    @Test
    @DisplayName("Given a builder with no display name set, when applyTagReplacements is called, then the display name remains null")
    void applyTagReplacements_nullDisplayName_remainsNull() throws Exception {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE);

        builder.applyTagReplacements(Map.of("<primary>", "<color:#D4A76A>"));

        assertNull(readDisplayName(builder));
    }

    @Test
    @DisplayName("applyTagReplacements returns the same builder instance for method chaining")
    void applyTagReplacements_returnsBuilderForChaining() {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
        ItemBuilder result = builder.applyTagReplacements(Map.of("<primary>", "A"));

        assertEquals(builder, result);
    }
}
