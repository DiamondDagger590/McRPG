package us.eunoians.mcrpg.command.admin.board;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.REFRESH_TYPE;

/**
 * Command: {@code /mcrpg board admin rotate}
 * <p>
 * Force-triggers a board rotation, bypassing the scheduled timer.
 */
public class BoardAdminRotateCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.rotate");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin")
                .literal("board")
                .literal("rotate")
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    QuestBoardManager boardManager = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.QUEST_BOARD);
                    NamespacedKey refreshKey = DailyRefreshType.KEY;
                    boardManager.triggerRotation(refreshKey);

                    var sender = commandContext.sender().getSender();
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_ROTATE_SUCCESS,
                            Map.of(REFRESH_TYPE.getPlaceholder(), refreshKey.toString())));
                }));
    }
}
