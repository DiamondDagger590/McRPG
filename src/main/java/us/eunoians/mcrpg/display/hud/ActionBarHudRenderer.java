package us.eunoians.mcrpg.display.hud;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Instance collaborator that composes the three-zone action bar HUD into a
 * single {@link Component}.
 * <p>
 * The full layout is:
 * <pre>{@code [HP zone] [left pad] [center zone] [right pad] [Mana zone]}</pre>
 * The center zone has a fixed pixel width (content + padding), keeping HP and
 * Mana visually anchored regardless of center content. When the persistent
 * HP/Mana display is disabled (see {@link #isPersistentPoolDisplayEnabled()}),
 * {@link #buildCenterOnly(Component)} emits just the center content so server
 * owners can render HP/Mana through another HUD element.
 */
public final class ActionBarHudRenderer {

    /**
     * Fixed pixel width reserved for the center zone (content + padding on both
     * sides). The value is tuned against Minecraft's 182-pixel hotbar: 96 pixels
     * pulls HP and Mana inward enough to feel grouped with the hotbar while
     * still comfortably fitting the widest center content strings we render
     * (combo dots and cooldown countdowns).
     */
    public static final int DEFAULT_CENTER_ZONE_WIDTH = 96;

    private final FontWidthTable widths;
    private final int centerZoneWidth;
    private final ReloadableContent<Boolean> persistentPoolEnabled;

    /**
     * @param widths                The {@link FontWidthTable} used to measure
     *                              rendered pixel widths.
     * @param persistentPoolEnabled Reloadable flag controlling whether HP/Mana
     *                              are rendered continuously on the action bar.
     */
    public ActionBarHudRenderer(@NotNull FontWidthTable widths,
                                @NotNull ReloadableContent<Boolean> persistentPoolEnabled) {
        this(widths, persistentPoolEnabled, DEFAULT_CENTER_ZONE_WIDTH);
    }

    /**
     * @param widths                The {@link FontWidthTable} used to measure
     *                              rendered pixel widths.
     * @param persistentPoolEnabled Reloadable flag controlling whether HP/Mana
     *                              are rendered continuously on the action bar.
     * @param centerZoneWidth       Pixel width reserved for the fixed-width center
     *                              zone.
     */
    public ActionBarHudRenderer(@NotNull FontWidthTable widths,
                                @NotNull ReloadableContent<Boolean> persistentPoolEnabled,
                                int centerZoneWidth) {
        this.widths = widths;
        this.persistentPoolEnabled = persistentPoolEnabled;
        this.centerZoneWidth = centerZoneWidth;
    }

    /**
     * @return The {@link FontWidthTable} this renderer measures against. Call
     * sites that want to precompute center-content widths and pass them to
     * {@link #buildFull(int, int, String, int, int, String, Component, int)}
     * should measure against this table for consistency.
     */
    @NotNull
    public FontWidthTable getFontWidthTable() {
        return widths;
    }

    /**
     * @return {@code true} if the action bar should continuously render the HP
     * and mana zones alongside the center content, {@code false} if only center
     * content should be rendered (server owner displays pools elsewhere).
     */
    public boolean isPersistentPoolDisplayEnabled() {
        Boolean value = persistentPoolEnabled.getContent();
        return value == null || value;
    }

    /**
     * Builds the full HUD component: HP zone, padded center, Mana zone.
     * <p>
     * This overload measures {@code centerContent}'s pixel width via the
     * renderer's {@link FontWidthTable} every call. Callers that already know
     * the width (e.g. the HUD tick loop, which caches width on each
     * {@link ActionBarCenterContent}) should prefer the
     * {@link #buildFull(int, int, String, int, int, String, Component, int)}
     * overload to avoid re-flattening the component.
     *
     * @param healthCurrent Current health value to display.
     * @param healthMax     Maximum health value to display.
     * @param healthSymbol  Symbol for health (e.g. {@code ❤}).
     * @param manaCurrent   Current mana value to display.
     * @param manaMax       Maximum mana value to display.
     * @param manaSymbol    Symbol for mana (e.g. {@code ✦}).
     * @param centerContent Center-zone content, or {@code null} for idle.
     * @return The composed action bar component.
     */
    @NotNull
    public Component buildFull(int healthCurrent, int healthMax,
                               @NotNull String healthSymbol,
                               int manaCurrent, int manaMax,
                               @NotNull String manaSymbol,
                               @Nullable Component centerContent) {
        int centerWidth = (centerContent != null) ? widths.getWidth(centerContent) : 0;
        return buildFull(healthCurrent, healthMax, healthSymbol,
                manaCurrent, manaMax, manaSymbol,
                centerContent, centerWidth);
    }

    /**
     * Builds the full HUD component using a caller-provided pixel width for
     * {@code centerContent}. This skips the flatten-then-measure path that
     * {@link #buildFull(int, int, String, int, int, String, Component)}
     * performs internally.
     *
     * @param healthCurrent     Current health value to display.
     * @param healthMax         Maximum health value to display.
     * @param healthSymbol      Symbol for health.
     * @param manaCurrent       Current mana value to display.
     * @param manaMax           Maximum mana value to display.
     * @param manaSymbol        Symbol for mana.
     * @param centerContent     Center-zone content, or {@code null} for idle.
     * @param centerContentWidth Precomputed pixel width of {@code centerContent}
     *                          (measured against this renderer's
     *                          {@link FontWidthTable}). Must be {@code 0} when
     *                          {@code centerContent} is {@code null}.
     * @return The composed action bar component.
     */
    @NotNull
    public Component buildFull(int healthCurrent, int healthMax,
                               @NotNull String healthSymbol,
                               int manaCurrent, int manaMax,
                               @NotNull String manaSymbol,
                               @Nullable Component centerContent,
                               int centerContentWidth) {
        Component hpZone = Component.text(healthSymbol + " " + healthCurrent + "/" + healthMax, NamedTextColor.RED);
        Component manaZone = Component.text(manaSymbol + " " + manaCurrent + "/" + manaMax, NamedTextColor.AQUA);
        return hpZone.append(buildPaddedCenter(centerContent, centerContentWidth)).append(manaZone);
    }

    /**
     * Builds a center-only HUD component, used when the server owner has
     * disabled the persistent HP/Mana display but still wants combo dots, XP
     * gains, cooldown messages, etc. to surface on the action bar.
     *
     * @param centerContent The center-zone content to emit. Must not be
     *                      {@code null}; callers that have no content should
     *                      skip the send entirely so the client's auto-fade
     *                      can proceed.
     * @return The composed center-only component.
     */
    @NotNull
    public Component buildCenterOnly(@NotNull Component centerContent) {
        return centerContent;
    }

    /**
     * Builds the center zone with balanced padding so the total pixel width of
     * padding + content equals {@link #centerZoneWidth}. Accepts a precomputed
     * {@code contentWidth} so callers on the HUD tick hot path can skip
     * re-flattening the component.
     */
    @NotNull
    private Component buildPaddedCenter(@Nullable Component centerContent, int contentWidth) {
        int remainingPixels = centerZoneWidth - contentWidth;

        if (remainingPixels <= 0) {
            int minPad = widths.spacesForWidth(widths.getSpaceWidth() * 2);
            Component pad = widths.spaceComponent(minPad);
            if (centerContent == null) {
                return pad.append(pad);
            }
            return pad.append(centerContent).append(pad);
        }

        int halfPixels = remainingPixels / 2;
        int leftSpaces = widths.spacesForWidth(halfPixels);
        int rightSpaces = widths.spacesForWidth(remainingPixels - (leftSpaces * widths.getSpaceWidth()));

        Component leftPad = widths.spaceComponent(leftSpaces);
        Component rightPad = widths.spaceComponent(rightSpaces);

        if (centerContent != null) {
            return leftPad.append(centerContent).append(rightPad);
        }
        return widths.spaceComponent(leftSpaces + rightSpaces);
    }
}
