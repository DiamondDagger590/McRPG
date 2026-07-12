package us.eunoians.mcrpg.command.admin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.logging.Level;

import static us.eunoians.mcrpg.command.admin.AdminBaseCommand.ADMIN_BASE_PERMISSION;

/**
 * Reloads all McRPG config files, reloadable content, quest definitions,
 * and invalidates online player level caches.
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
                    McRPG plugin = McRPG.getInstance();

                    // reloadFiles() reloads the YamlDocuments and then calls
                    // ReloadableContentManager.reloadAllContent(), which re-parses the board
                    // rarity/category/template configs (now tracked in QuestBoardManager.initialize).
                    plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).reloadFiles();

                    // Invalidate level caches for all online players since leveling equations may have changed
                    plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getAllPlayers()
                            .forEach(player -> player.asSkillHolder().invalidateAllLevelCaches());

                    // Reload quest definitions from the quests/ and quest-board/quests/ directories
                    QuestManager questManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                    int beforeCount = questManager.getQuestDefinitionRegistry().getRegisteredKeys().size();
                    questManager.loadQuestDefinitions();
                    int afterCount = questManager.getQuestDefinitionRegistry().getRegisteredKeys().size();
                    plugin.getLogger().log(Level.INFO, "[Reload] Quest definitions reloaded: {0} loaded (was {1})",
                            new Object[]{afterCount, beforeCount});

                    senderAudience.sendMessage(plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                            .getLocalizedMessageAsComponent(LocalizationKey.RELOAD_COMMAND_SENDER_SUCCESS_MESSAGE));
                }));
    }

}
