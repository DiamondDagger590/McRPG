package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.task.core.RepeatableCoreTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public class PlayerActionBarTask extends RepeatableCoreTask {

    private static final Key ABILITY_FONT = Key.key("minecraft", "ability");
    private static final String WIP = "Ɲ";
    private static final String WIP_S1 = "ƞ";

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
            player.sendActionBar(Component.text().append(toGlyph(WIP)
                    .append(Component.text(" "))
                    .append(toGlyph(WIP_S1))));
        }
    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    private Component toGlyph(@NotNull String string) {
        return Component.text(string).font(ABILITY_FONT).decoration(TextDecoration.ITALIC, false);
    }
}
