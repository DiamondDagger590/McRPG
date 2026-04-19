package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.jetbrains.annotations.NotNull;

/**
 * Provides pixel-width calculations for Minecraft's default bitmap font.
 * <p>
 * Each character in the default font occupies a known number of pixels (including
 * the 1px inter-character gap). This utility uses a lookup table to compute the
 * total rendered width of a string or {@link Component}, enabling precise
 * padding calculations for the action bar HUD.
 * <p>
 * The widths used here are for the vanilla default resource pack at the default
 * GUI scale. Custom resource packs or non-default GUI scales will produce
 * different results — this is acceptable for the PoC.
 * <p>
 * TODO(#214): Fold this into the display manager system alongside
 * {@link ActionBarHudRenderer} so font-width awareness is an injectable
 * collaborator rather than a static lookup table. This would also let us load
 * width tables from a resource pack descriptor to support custom fonts.
 */
public final class MinecraftFontWidthUtil {

    /**
     * Width of a space character in pixels (3px glyph + 1px gap).
     */
    public static final int SPACE_WIDTH = 4;

    /**
     * Width of a bold space character in pixels (4px glyph + 1px gap).
     */
    private static final int BOLD_SPACE_WIDTH = 5;

    private MinecraftFontWidthUtil() {}

    /**
     * Returns the pixel width of a single character in the default Minecraft font.
     * The returned width includes the 1px inter-character gap that Minecraft
     * renders between glyphs.
     *
     * @param c The character to measure.
     * @return The pixel width including the trailing gap.
     */
    public static int getCharWidth(char c) {
        return switch (c) {
            case ' ' -> 4;
            case '!', ',', '.', ':', ';', 'i', '|' -> 2;
            case '\'', 'l' -> 3;
            case '`' -> 3;
            case '"', '(', ')', '*', 'I', '[', ']', 't', '{', '}' -> 4;
            case '<', '>', 'f', 'k' -> 5;
            case '@', '~' -> 7;
            default -> 6;
        };
    }

    /**
     * Returns the pixel width of a string rendered in the default Minecraft font.
     *
     * @param text The string to measure.
     * @return The total pixel width.
     */
    public static int getWidth(@NotNull String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += getCharWidth(text.charAt(i));
        }
        return width;
    }

    /**
     * Returns the pixel width of a {@link Component} by flattening it to plain text
     * and summing character widths.
     *
     * @param component The component to measure.
     * @return The total pixel width.
     */
    public static int getWidth(@NotNull Component component) {
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(component, sb::append);
        return getWidth(sb.toString());
    }

    /**
     * Returns the number of space characters needed to fill a given pixel width.
     * Rounds down — the caller should account for any remainder if sub-space
     * precision is needed.
     *
     * @param pixels The target pixel width to fill with spaces.
     * @return The number of space characters, or 0 if pixels is non-positive.
     */
    public static int spacesForWidth(int pixels) {
        if (pixels <= 0) {
            return 0;
        }
        return pixels / SPACE_WIDTH;
    }

    /**
     * Creates a {@link Component} consisting of the specified number of space characters.
     *
     * @param count The number of spaces.
     * @return A text component containing the spaces, or {@link Component#empty()} if count is 0.
     */
    @NotNull
    public static Component spaceComponent(int count) {
        if (count <= 0) {
            return Component.empty();
        }
        return Component.text(" ".repeat(count));
    }
}
