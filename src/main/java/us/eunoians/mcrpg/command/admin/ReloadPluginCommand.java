package us.eunoians.mcrpg.command.admin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static us.eunoians.mcrpg.command.admin.AdminBaseCommand.ADMIN_BASE_PERMISSION;

/**
 * This command is used to reload all McRPG config files
 */
public class ReloadPluginCommand extends McRPGCommandBase {

    private static final Permission RELOAD_PLUGIN_PERMISSION = Permission.of("mcrpg.admin.reload");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("reload")
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, RELOAD_PLUGIN_PERMISSION))
                .handler(commandContext -> {
                            Audience senderAudience = commandContext.sender().getSender();
                            McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).reloadFiles();
                            senderAudience.sendMessage(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                                    .getLocalizedMessageAsComponent(LocalizationKey.RELOAD_COMMAND_SENDER_SUCCESS_MESSAGE));
                        }
                ));
    }

}
