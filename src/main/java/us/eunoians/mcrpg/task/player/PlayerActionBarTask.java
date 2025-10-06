package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.task.core.RepeatableCoreTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public class PlayerActionBarTask extends RepeatableCoreTask {

    private static final Key ABILITY_FONT = Key.key("mcrpg", "ability");
    private static final Key DEFAULT_FONT  = Key.key("minecraft", "default");
    private static final String WIP = "Ɲ";
    private static final String WIP_S1 = "ƞ";
    private static final String RIGHT_CLICK_MOUSE = "þ";
    private static final String LEFT_CLICK_MOUSE = "ÿ";
    private static final String BACK_16 = "ü";
    private static final String FWD_16 = "û";
    private static final String BACK_8 = "ú";
    private static final String FWD_8 = "ù";
    private static final String BACK_2 = "ø";
    private static final String FWD_2 = "÷";
    private static final String BACK_1 = "õ";
    private static final String FWD_1 = "ö";

    private static final Component SPACE = Component.text(" ");
    private static final Component WIP_COMPONENT = toGlyph(WIP);
    private static final Component WIP_S1_COMPONENT = toGlyph(WIP_S1);
    private static final Component RIGHT_CLICK_MOUSE_COMPONENT = toGlyph(RIGHT_CLICK_MOUSE);
    private static final Component LEFT_CLICK_MOUSE_COMPONENT = toGlyph(LEFT_CLICK_MOUSE);

    public PlayerActionBarTask(@NotNull CorePlugin plugin) {
        super(plugin, 0, 0.05);
    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {

    }

    @Override
    protected void onIntervalComplete() {
        McRPG mcRPG = McRPG.getInstance();
        MiniMessage miniMessage = mcRPG.getMiniMessage();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(Component.text().append(toGlyph(WIP)).append(toGlyph(BACK_16)).append(toGlyph(FWD_2)).append(toGlyph(FWD_1))
                    .append(toGlyph(RIGHT_CLICK_MOUSE)).append(toGlyph(BACK_1)).append(toGlyph(LEFT_CLICK_MOUSE))
                    .append(Component.text(" ").font(DEFAULT_FONT))
                    .append(toGlyph(WIP_S1)).append(toGlyph(BACK_16)).append(toGlyph(FWD_2)).append(toGlyph(FWD_1)).append(toGlyph(RIGHT_CLICK_MOUSE))
                    .append(toGlyph(BACK_1)).append(toGlyph(RIGHT_CLICK_MOUSE)));
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    private static Component toGlyph(@NotNull String string) {
        return Component.text(string).font(ABILITY_FONT).decoration(TextDecoration.ITALIC, false).shadowColor(ShadowColor.shadowColor(0));
    }
}
