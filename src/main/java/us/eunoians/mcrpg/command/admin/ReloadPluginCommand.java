package us.eunoians.mcrpg.command.admin;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;

import static us.eunoians.mcrpg.command.admin.AdminBaseCommand.ADMIN_BASE_PERMISSION;

/**
 * This command is used to reload all McRPG config files
 */
public class ReloadPluginCommand extends McRPGCommandBase {

    private static final Permission RELOAD_PLUGIN_PERMISSION = Permission.of("mcrpg.admin.reload");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().getCommandManager();
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reload")
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RELOAD_PLUGIN_PERMISSION))
                .handler(commandContext -> {
                            BukkitAudiences adventure = McRPG.getInstance().getAdventure();
                            Audience senderAudience = adventure.sender(commandContext.sender().getSender());
                            McRPG.getInstance().getFileManager().reloadFiles();
                            senderAudience.sendMessage(miniMessage.deserialize("<gray>You have reloaded all McRPG files."));
                        }
                ));
    }

}
