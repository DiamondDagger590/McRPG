package us.eunoians.mcrpg.command.quest.admin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.command.quest.QuestCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.AFTER_COUNT;
import static us.eunoians.mcrpg.command.CommandPlaceholders.BEFORE_COUNT;

/**
 * Command: {@code /mcrpg quest admin reload}
 * <p>
 * Reloads quest definitions from the {@code quests/} directory. Active quest instances
 * are not affected -- they continue running and reference definitions by key.
 */
public class QuestAdminReloadCommand extends QuestCommandBase {

    private static final Permission QUEST_ADMIN_RELOAD_PERMISSION = Permission.of("mcrpg.quest.admin.reload");

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("quest")
                .literal("admin")
                .literal("reload")
                .permission(Permission.anyOf(ROOT_PERMISSION, QUEST_ADMIN_PERMISSION, QUEST_ADMIN_RELOAD_PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
                    QuestManager questManager = RegistryAccess.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

                    int beforeCount = questManager.getQuestDefinitionRegistry().getRegisteredKeys().size();
                    questManager.loadQuestDefinitions();
                    int afterCount = questManager.getQuestDefinitionRegistry().getRegisteredKeys().size();

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.QUEST_ADMIN_RELOAD_SUCCESS,
                            Map.of(AFTER_COUNT.getPlaceholder(), String.valueOf(afterCount),
                                    BEFORE_COUNT.getPlaceholder(), String.valueOf(beforeCount))));
                }));
    }
}
