package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Provides pixel-width calculations for Minecraft's default bitmap font, used
 * by the action bar HUD to position HP and mana zones precisely around a
 * fixed-width center region.
 * <p>
 * Implementations are free to source their widths from a hard-coded lookup
 * table (see {@link MinecraftDefaultFontWidthTable}), a resource-pack
 * descriptor, or any other width provider — the renderer depends only on this
 * interface so swapping implementations at bootstrap is a one-line change.
 */
public interface FontWidthTable {

    /**
     * Returns the pixel width of a single character in the target font.
     * The returned width must include the 1-pixel inter-character gap that
     * Minecraft renders between glyphs.
     *
     * @param c The character to measure.
     * @return The pixel width including the trailing gap.
     */
    int getCharWidth(char c);

    /**
     * Returns the pixel width of a plain string rendered in the target font.
     *
     * @param text The string to measure.
     * @return The total pixel width.
     */
    int getWidth(@NotNull String text);

    /**
     * Returns the pixel width of an Adventure {@link Component} by flattening it
     * to plain text and summing per-character widths.
     *
     * @param component The component to measure.
     * @return The total pixel width.
     */
    int getWidth(@NotNull Component component);

    /**
     * Returns the pixel width of a single space character in the target font.
     * Exposed because callers often need to quantize padding in terms of space
     * widths, and different fonts may use different space glyphs.
     *
     * @return The pixel width of a space character including its trailing gap.
     */
    int getSpaceWidth();

    /**
     * Returns the number of space characters required to fill at least
     * {@code pixels} pixels. The result rounds down — callers that care about
     * sub-space precision should add their own remainder handling.
     *
     * @param pixels The target pixel width to fill with spaces.
     * @return The number of space characters, or {@code 0} if {@code pixels} is
     * non-positive.
     */
    default int spacesForWidth(int pixels) {
        if (pixels <= 0) {
            return 0;
        }
        return pixels / getSpaceWidth();
    }

    /**
     * Creates a {@link Component} consisting of the specified number of space
     * characters.
     *
     * @param count The number of spaces.
     * @return A text component containing the spaces, or {@link Component#empty()}
     * if {@code count <= 0}.
     */
    @NotNull
    default Component spaceComponent(int count) {
        if (count <= 0) {
            return Component.empty();
        }
        return Component.text(" ".repeat(count));
    }
}
