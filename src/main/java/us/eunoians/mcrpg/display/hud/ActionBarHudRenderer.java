package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stateless renderer that composes the three-zone action bar HUD into a single
 * {@link Component}.
 * <p>
 * The layout is: {@code [HP zone] [left pad] [center zone] [right pad] [Mana zone]}.
 * The center zone is padded with spaces so its total pixel width (content + padding)
 * remains constant, keeping HP and Mana visually anchored regardless of center content.
 * <p>
 * TODO(#214): Refactor this and {@link MinecraftFontWidthUtil} into the existing
 * display manager system (see {@code us.eunoians.mcrpg.display}) as injected
 * collaborators instead of a static utility. This was intentionally left as a
 * PoC-style helper for the combat rework; it should be promoted once a second
 * caller needs action-bar rendering.
 */
public final class ActionBarHudRenderer {

    /**
     * Fixed pixel width reserved for the center zone (content + padding on both sides).
     * Sized to comfortably fit "On Cooldown (33s)" (~110px) with room for padding.
     * Combo dots "⬤ ⬤ ○" are ~30px, so they get proportionally more padding.
     */
    private static final int FIXED_CENTER_ZONE_WIDTH = 160;

    private ActionBarHudRenderer() {}

    /**
     * Builds the full HUD component for one frame.
     *
     * @param healthCurrent Current health value to display.
     * @param healthMax     Maximum health value to display.
     * @param healthSymbol  Symbol for health (e.g., "❤").
     * @param manaCurrent   Current mana value to display.
     * @param manaMax       Maximum mana value to display.
     * @param manaSymbol    Symbol for mana (e.g., "✦").
     * @param centerContent The center zone content, or {@code null} for idle (empty center).
     * @return The composed action bar component.
     */
    @NotNull
    public static Component buildHud(int healthCurrent, int healthMax,
                                      @NotNull String healthSymbol,
                                      int manaCurrent, int manaMax,
                                      @NotNull String manaSymbol,
                                      @Nullable Component centerContent) {
        Component hpZone = Component.text(healthSymbol + " " + healthCurrent + "/" + healthMax, NamedTextColor.RED);
        Component manaZone = Component.text(manaSymbol + " " + manaCurrent + "/" + manaMax, NamedTextColor.AQUA);

        Component paddedCenter = buildPaddedCenter(centerContent);

        return hpZone.append(paddedCenter).append(manaZone);
    }

    /**
     * Builds the center zone with balanced padding to fill {@link #FIXED_CENTER_ZONE_WIDTH} pixels.
     */
    @NotNull
    private static Component buildPaddedCenter(@Nullable Component centerContent) {
        int contentWidth = (centerContent != null) ? MinecraftFontWidthUtil.getWidth(centerContent) : 0;
        int remainingPixels = FIXED_CENTER_ZONE_WIDTH - contentWidth;

        if (remainingPixels <= 0) {
            int minPad = MinecraftFontWidthUtil.spacesForWidth(MinecraftFontWidthUtil.SPACE_WIDTH * 2);
            Component pad = MinecraftFontWidthUtil.spaceComponent(minPad);
            return pad.append(centerContent).append(pad);
        }

        int leftSpaces = MinecraftFontWidthUtil.spacesForWidth(remainingPixels / 2);
        int rightSpaces = MinecraftFontWidthUtil.spacesForWidth(remainingPixels - (leftSpaces * MinecraftFontWidthUtil.SPACE_WIDTH));

        Component leftPad = MinecraftFontWidthUtil.spaceComponent(leftSpaces);
        Component rightPad = MinecraftFontWidthUtil.spaceComponent(rightSpaces);

        if (centerContent != null) {
            return leftPad.append(centerContent).append(rightPad);
        }
        return MinecraftFontWidthUtil.spaceComponent(leftSpaces + rightSpaces);
    }
}
