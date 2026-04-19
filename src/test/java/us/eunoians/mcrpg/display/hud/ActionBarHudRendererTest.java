package us.eunoians.mcrpg.display.hud;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionBarHudRendererTest extends McRPGBaseTest {

    @DisplayName("buildHud produces a non-empty component")
    @Test
    void buildHudReturnsComponent() {
        Component result = ActionBarHudRenderer.buildHud(
                147, 200, "❤",
                180, 220, "✦",
                null
        );
        assertNotNull(result);
        String plain = toPlainText(result);
        assertTrue(plain.contains("147/200"), "Should contain health: " + plain);
        assertTrue(plain.contains("180/220"), "Should contain mana: " + plain);
    }

    @DisplayName("buildHud with center content includes it")
    @Test
    void buildHudWithCenter() {
        Component center = Component.text("On Cooldown (33s)", NamedTextColor.RED);
        Component result = ActionBarHudRenderer.buildHud(
                100, 200, "❤",
                50, 220, "✦",
                center
        );
        String plain = toPlainText(result);
        assertTrue(plain.contains("On Cooldown (33s)"), "Should contain cooldown text: " + plain);
    }

    @DisplayName("buildHud with and without center content produces same total width")
    @Test
    void sameWidthWithAndWithoutCenter() {
        Component withCenter = ActionBarHudRenderer.buildHud(
                147, 200, "❤",
                180, 220, "✦",
                Component.text("⬤ ⬤ ○")
        );
        Component withoutCenter = ActionBarHudRenderer.buildHud(
                147, 200, "❤",
                180, 220, "✦",
                null
        );
        int widthWith = MinecraftFontWidthUtil.getWidth(withCenter);
        int widthWithout = MinecraftFontWidthUtil.getWidth(withoutCenter);
        // Widths should be the same or very close (within one space character)
        assertTrue(Math.abs(widthWith - widthWithout) <= MinecraftFontWidthUtil.SPACE_WIDTH,
                "Width difference should be at most one space: with=" + widthWith + " without=" + widthWithout);
    }

    private String toPlainText(Component component) {
        StringBuilder sb = new StringBuilder();
        net.kyori.adventure.text.flattener.ComponentFlattener.basic().flatten(component, sb::append);
        return sb.toString();
    }
}
