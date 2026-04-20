package us.eunoians.mcrpg.display.hud;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the render-mode surface of {@link ActionBarHudRenderer} that the
 * action bar HUD display dispatches on:
 * <ul>
 *   <li>{@link ActionBarHudRenderer#isPersistentPoolDisplayEnabled()} reads the
 *       backing {@link ReloadableContent} live, so toggling the YAML flag at
 *       runtime flips the renderer between full and center-only modes;</li>
 *   <li>{@link ActionBarHudRenderer#buildFull(int, int, String, int, int, String, Component)}
 *       emits a component that contains HP/mana, whereas
 *       {@link ActionBarHudRenderer#buildCenterOnly(Component)} emits only the
 *       center content.</li>
 * </ul>
 */
public class ActionBarHudRendererTest {

    private ReloadableContent<Boolean> reloadableFlag(boolean initialValue) {
        YamlDocument doc = mock(YamlDocument.class);
        Route route = Route.fromString("hud.action-bar.persistent-pool-display");
        // Constructor runs the reload callback eagerly; stub getBoolean accordingly.
        when(doc.getBoolean(any(Route.class), anyBoolean())).thenReturn(initialValue);
        return new ReloadableContent<>(doc, route, (d, r) -> d.getBoolean(r, true));
    }

    private String plain(Component component) {
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(component, sb::append);
        return sb.toString();
    }

    @Test
    @DisplayName("Given the persistent pool flag is true, when isPersistentPoolDisplayEnabled is called, then it returns true")
    void isPersistentPoolDisplayEnabled_returnsTrue_whenFlagIsTrue() {
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), reloadableFlag(true));

        assertTrue(renderer.isPersistentPoolDisplayEnabled());
    }

    @Test
    @DisplayName("Given the persistent pool flag is false, when isPersistentPoolDisplayEnabled is called, then it returns false")
    void isPersistentPoolDisplayEnabled_returnsFalse_whenFlagIsFalse() {
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), reloadableFlag(false));

        assertFalse(renderer.isPersistentPoolDisplayEnabled());
    }

    @Test
    @DisplayName("Given HP, mana, and a center component, when buildFull is called, then the composed component contains HP, mana, and the center content")
    void buildFull_containsAllZones_whenGivenHealthManaAndCenter() {
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), reloadableFlag(true));

        Component hud = renderer.buildFull(
                25, 30, "\u2764",
                10, 20, "\u2726",
                Component.text("CENTER"));

        String text = plain(hud);
        assertTrue(text.contains("25/30"), "HP zone should include current/max HP");
        assertTrue(text.contains("10/20"), "Mana zone should include current/max mana");
        assertTrue(text.contains("CENTER"), "Center content should be embedded between pools");
    }

    @Test
    @DisplayName("Given a null center component, when buildFull is called, then the idle HUD still renders HP and mana")
    void buildFull_rendersHealthAndMana_whenCenterContentIsNull() {
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), reloadableFlag(true));

        Component hud = renderer.buildFull(
                10, 10, "\u2764",
                5, 5, "\u2726",
                null);

        String text = plain(hud);
        assertTrue(text.contains("10/10"));
        assertTrue(text.contains("5/5"));
    }

    @Test
    @DisplayName("Given center-only mode, when buildCenterOnly is called, then the component is returned untouched (no HP/mana wrapping)")
    void buildCenterOnly_returnsCenterUntouched_whenInvokedDirectly() {
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), reloadableFlag(false));

        Component center = Component.text("+12 XP");
        Component emitted = renderer.buildCenterOnly(center);

        // buildCenterOnly is explicitly documented as a pass-through so server
        // owners keep auto-fade semantics; if the implementation starts wrapping
        // it, this test fails and prompts a fresh design decision rather than
        // silently changing the contract.
        assertSame(center, emitted);
    }

    @Test
    @DisplayName("Given the ReloadableContent returns null (not yet loaded), when isPersistentPoolDisplayEnabled is called, then it defaults to enabled")
    void isPersistentPoolDisplayEnabled_defaultsToEnabled_whenReloadableContentReturnsNull() {
        YamlDocument doc = mock(YamlDocument.class);
        // Simulate the ReloadableContent loader returning null (e.g. missing
        // route on first reload, before defaults are applied). The renderer
        // must fail-safe to enabled so players still see HP/mana instead of a
        // silently blank action bar.
        ReloadableContent<Boolean> nullFlag = new ReloadableContent<>(doc,
                Route.fromString("hud.action-bar.persistent-pool-display"),
                (d, r) -> null);

        ActionBarHudRenderer renderer = new ActionBarHudRenderer(
                new MinecraftDefaultFontWidthTable(), nullFlag);

        assertTrue(renderer.isPersistentPoolDisplayEnabled());
    }

    @Test
    @DisplayName("Given two center contents of different pixel widths that both fit inside the reserved zone, when buildFull is called, then the total HUD width is identical (HP/mana do not shift)")
    void buildFull_preservesOverallPixelWidth_whenCenterContentLengthChanges() {
        MinecraftDefaultFontWidthTable widths = new MinecraftDefaultFontWidthTable();
        ActionBarHudRenderer renderer = new ActionBarHudRenderer(widths, reloadableFlag(true));

        // Short center content (e.g. idle combo dots) vs longer content
        // (e.g. a cooldown message). Both must fit inside the reserved 96px
        // center zone so the fixed-width invariant applies.
        Component shortCenter = Component.text("\u25CB"); // 8px
        Component longCenter = Component.text("On Cooldown"); // many more px

        Component shortHud = renderer.buildFull(
                25, 30, "\u2764",
                10, 20, "\u2726",
                shortCenter);
        Component longHud = renderer.buildFull(
                25, 30, "\u2764",
                10, 20, "\u2726",
                longCenter);

        int shortWidth = widths.getWidth(shortHud);
        int longWidth = widths.getWidth(longHud);

        // Padding is built in whole-space increments (each space is
        // {@code widths.getSpaceWidth()} pixels wide), so two different center
        // contents that each fit inside the zone can differ by at most a
        // sub-space rounding remainder. Before the fix, they could differ by
        // tens of pixels — visibly shoving HP/mana around between frames.
        int spaceWidth = widths.getSpaceWidth();
        assertTrue(Math.abs(longWidth - shortWidth) < spaceWidth,
                () -> "HUD pixel width must not drift by more than one space between " +
                        "center-content updates that both fit in the reserved zone; " +
                        "got short=" + shortWidth + " long=" + longWidth +
                        " spaceWidth=" + spaceWidth);
    }
}
