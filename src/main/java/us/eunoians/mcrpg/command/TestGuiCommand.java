package us.eunoians.mcrpg.command;

import cloud.commandframework.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.gui.HomeGui;

public class TestGuiCommand {

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getBukkitCommandManager();
        commandManager.command(commandManager.commandBuilder("test").handler(commandContext -> {
            CommandSender commandSender = commandContext.getSender();
            if (commandSender instanceof Player player) {
                HomeGui homeGui = new HomeGui(player.getUniqueId());
                McRPG.getInstance().getGuiTracker().trackPlayerGui(player, homeGui);
            }
        }));
    }
}
