package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;

/**
 * {@link FontWidthTable} implementation backed by hard-coded widths for the
 * vanilla Minecraft default font at the default GUI scale.
 * <p>
 * The widths cover the common ASCII subset plus the non-ASCII glyphs that the
 * action bar HUD uses for stat symbols and combo dots ({@code ❤}, {@code ✦},
 * {@code ⬤}, {@code ○}). Fonts supplied by a resource pack or an alternative
 * GUI scale may render differently — plug a different {@link FontWidthTable}
 * in at that point rather than mutating this table.
 */
public final class MinecraftDefaultFontWidthTable implements FontWidthTable {

    /**
     * Width of a space character in pixels (3px glyph + 1px gap).
     */
    public static final int SPACE_WIDTH = 4;

    @Override
    public int getCharWidth(char c) {
        return switch (c) {
            case ' ' -> SPACE_WIDTH;
            case '!', ',', '.', ':', ';', 'i', '|' -> 2;
            case '\'', 'l' -> 3;
            case '`' -> 3;
            case '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}' -> 4;
            case '<', '>', 'f', 'k' -> 5;
            case '@', '~' -> 7;
            case '❤' -> 9;   // U+2764 HEAVY BLACK HEART
            case '✦' -> 8;   // U+2726 BLACK FOUR POINTED STAR
            case '⬤' -> 11;  // U+2B24 BLACK LARGE CIRCLE
            case '○' -> 8;   // U+25CB WHITE CIRCLE
            default -> 6;
        };
    }

    @Override
    public int getWidth(@NotNull String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += getCharWidth(text.charAt(i));
        }
        return width;
    }

    @Override
    public int getWidth(@NotNull Component component) {
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(component, sb::append);
        return getWidth(sb.toString());
    }

    @Override
    public int getSpaceWidth() {
        return SPACE_WIDTH;
    }
}
