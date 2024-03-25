package us.eunoians.mcrpg.command;

import cloud.commandframework.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;

public class GiveLevelsCommand {

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getBukkitCommandManager();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("give")
                .literal("level", "levels")

                .permission("mcrpg.give")
                .senderType(Player.class)
                .handler(commandContext -> {
                        }
                ));

    }
}
