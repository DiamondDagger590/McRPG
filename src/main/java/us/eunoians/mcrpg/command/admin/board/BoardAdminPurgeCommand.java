package us.eunoians.mcrpg.command.admin.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.DELETED_COUNT;

/**
 * Command: {@code /mcrpg board admin purge expired}
 * <p>
 * Runs cleanup operations: prunes expired cooldowns and reports counts.
 */
public class BoardAdminPurgeCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.purge");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("purge").literal("expired")
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            int prunedCooldowns = 0;
                            for (var ps : BoardCooldownDAO.pruneExpiredCooldowns(connection)) {
                                prunedCooldowns += ps.executeUpdate();
                                ps.close();
                            }
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_PURGE_SUCCESS,
                                    Map.of(DELETED_COUNT.getPlaceholder(), String.valueOf(prunedCooldowns))));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_PURGE_ERROR));
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
