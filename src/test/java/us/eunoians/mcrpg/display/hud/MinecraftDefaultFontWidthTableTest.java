package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers pixel-width accounting for {@link MinecraftDefaultFontWidthTable}.
 * <p>
 * These widths drive the fixed center-zone padding on the action bar HUD, so any
 * regression here immediately shows up as the HP/mana "jump" bug the renderer
 * refactor was introduced to fix — tests pin the exact widths we expect rather
 * than accepting whatever the table currently returns.
 */
public class MinecraftDefaultFontWidthTableTest {

    private MinecraftDefaultFontWidthTable widths;

    @BeforeEach
    void setUp() {
        widths = new MinecraftDefaultFontWidthTable();
    }

    @Test
    @DisplayName("Given the default Minecraft font, when getSpaceWidth is called, then it returns 4 pixels")
    void getSpaceWidth_returnsFour_whenUsingMinecraftDefaultFont() {
        assertEquals(4, widths.getSpaceWidth());
        assertEquals(4, widths.getCharWidth(' '));
    }

    @Test
    @DisplayName("Given HUD-specific Unicode glyphs, when getCharWidth is called, then it returns the Minecraft default font widths")
    void getCharWidth_returnsKnownUnicodeWidths_whenQueriedWithHudGlyphs() {
        assertEquals(9, widths.getCharWidth('\u2764'), "Heavy black heart ❤");
        assertEquals(8, widths.getCharWidth('\u2726'), "Black four pointed star ✦");
        assertEquals(11, widths.getCharWidth('\u2B24'), "Black large circle ⬤");
        assertEquals(8, widths.getCharWidth('\u25CB'), "White circle ○");
    }

    @Test
    @DisplayName("Given narrow and default-width ASCII characters, when getCharWidth is called, then it returns their specific Minecraft widths rather than a flat fallback")
    void getCharWidth_returnsSpecificAsciiWidths_whenQueriedWithMixedCharacters() {
        // Narrow characters that differ from the 6px default.
        assertEquals(2, widths.getCharWidth('i'));
        assertEquals(2, widths.getCharWidth('|'));
        assertEquals(3, widths.getCharWidth('l'));
        assertEquals(5, widths.getCharWidth('k'));
        assertEquals(4, widths.getCharWidth('t'));
        // Default-width ASCII.
        assertEquals(6, widths.getCharWidth('a'));
        assertEquals(6, widths.getCharWidth('Z'));
    }

    @Test
    @DisplayName("Given a plain string, when getWidth is called, then it returns the sum of per-character widths")
    void getWidth_returnsSumOfCharacterWidths_whenGivenPlainString() {
        int expected = widths.getCharWidth('H') + widths.getCharWidth('i');
        assertEquals(expected, widths.getWidth("Hi"));
    }

    @Test
    @DisplayName("Given a styled Adventure component, when getWidth is called, then it matches the plain-text width of the same characters")
    void getWidth_matchesPlainText_whenGivenStyledComponent() {
        Component component = Component.text("Hi", NamedTextColor.RED);
        assertEquals(widths.getWidth("Hi"), widths.getWidth(component));
    }

    @Test
    @DisplayName("Given a component with nested children, when getWidth is called, then it walks every child into the total width")
    void getWidth_walksChildren_whenGivenNestedComponent() {
        Component component = Component.text("A").append(Component.text("B"));
        assertEquals(widths.getWidth("AB"), widths.getWidth(component));
    }

    @Test
    @DisplayName("Given a pixel budget that is not an even multiple of the space width, when spacesForWidth is called, then the result rounds down to whole spaces")
    void spacesForWidth_roundsDown_whenPixelsAreNotEvenMultiple() {
        assertEquals(0, widths.spacesForWidth(0));
        assertEquals(1, widths.spacesForWidth(4));
        assertEquals(1, widths.spacesForWidth(7), "7px fits exactly one 4px space");
        assertEquals(2, widths.spacesForWidth(8));
        assertEquals(3, widths.spacesForWidth(12));
    }

    @Test
    @DisplayName("Given a positive space count, when spaceComponent is called, then the rendered component measures count * space width")
    void spaceComponent_rendersRequestedSpaceCount_whenCountIsPositive() {
        Component zero = widths.spaceComponent(0);
        Component three = widths.spaceComponent(3);

        assertEquals(0, widths.getWidth(zero));
        assertEquals(3 * widths.getSpaceWidth(), widths.getWidth(three));
        assertTrue(widths.getWidth(three) > widths.getWidth(zero));
    }
}
