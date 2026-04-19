package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftFontWidthUtilTest extends McRPGBaseTest {

    @DisplayName("Space character is 4px")
    @Test
    void spaceWidth() {
        assertEquals(4, MinecraftFontWidthUtil.getCharWidth(' '));
    }

    @DisplayName("Narrow characters are 2px")
    @Test
    void narrowCharacters() {
        assertEquals(2, MinecraftFontWidthUtil.getCharWidth('i'));
        assertEquals(2, MinecraftFontWidthUtil.getCharWidth('!'));
        assertEquals(2, MinecraftFontWidthUtil.getCharWidth('.'));
        assertEquals(2, MinecraftFontWidthUtil.getCharWidth('|'));
    }

    @DisplayName("Standard characters are 6px")
    @Test
    void standardCharacters() {
        assertEquals(6, MinecraftFontWidthUtil.getCharWidth('a'));
        assertEquals(6, MinecraftFontWidthUtil.getCharWidth('A'));
        assertEquals(6, MinecraftFontWidthUtil.getCharWidth('0'));
        assertEquals(6, MinecraftFontWidthUtil.getCharWidth('/'));
    }

    @DisplayName("Wide characters (@ ~) are 7px")
    @Test
    void wideCharacters() {
        assertEquals(7, MinecraftFontWidthUtil.getCharWidth('@'));
        assertEquals(7, MinecraftFontWidthUtil.getCharWidth('~'));
    }

    @DisplayName("String width sums individual char widths")
    @Test
    void stringWidth() {
        // "Hi" = H(6) + i(2) = 8
        assertEquals(8, MinecraftFontWidthUtil.getWidth("Hi"));
    }

    @DisplayName("Component width matches plain text width")
    @Test
    void componentWidth() {
        Component comp = Component.text("Hi", NamedTextColor.RED);
        assertEquals(8, MinecraftFontWidthUtil.getWidth(comp));
    }

    @DisplayName("Compound component sums all children")
    @Test
    void compoundComponentWidth() {
        Component comp = Component.text("A").append(Component.text("B"));
        // A(6) + B(6) = 12
        assertEquals(12, MinecraftFontWidthUtil.getWidth(comp));
    }

    @DisplayName("spacesForWidth returns correct count")
    @Test
    void spacesForWidth() {
        assertEquals(5, MinecraftFontWidthUtil.spacesForWidth(20));
        assertEquals(2, MinecraftFontWidthUtil.spacesForWidth(10));
        assertEquals(0, MinecraftFontWidthUtil.spacesForWidth(0));
        assertEquals(0, MinecraftFontWidthUtil.spacesForWidth(-5));
    }

    @DisplayName("spaceComponent creates correct number of spaces")
    @Test
    void spaceComponentCreatesSpaces() {
        Component result = MinecraftFontWidthUtil.spaceComponent(3);
        assertEquals(12, MinecraftFontWidthUtil.getWidth(result));
    }

    @DisplayName("Empty string has zero width")
    @Test
    void emptyStringZeroWidth() {
        assertEquals(0, MinecraftFontWidthUtil.getWidth(""));
    }

    @DisplayName("Empty component has zero width")
    @Test
    void emptyComponentZeroWidth() {
        assertEquals(0, MinecraftFontWidthUtil.getWidth(Component.empty()));
    }
}
