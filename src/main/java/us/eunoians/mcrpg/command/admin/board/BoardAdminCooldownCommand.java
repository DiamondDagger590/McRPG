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
import us.eunoians.mcrpg.command.parser.quest.QuestScopeTypeParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Commands: {@code /mcrpg board admin cooldown list|reset <scope-type> <scope-id>}
 */
public class BoardAdminCooldownCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.cooldown");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("cooldown").literal("list")
                .required("scope-type", QuestScopeTypeParser.scopeTypeParser())
                .required("scope-id", stringParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    String scopeType = commandContext.get("scope-type");
                    String scopeId = commandContext.get("scope-id");

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            List<BoardCooldownDAO.CooldownRecord> records =
                                    BoardCooldownDAO.listCooldowns(connection, scopeType, scopeId);

                            if (records.isEmpty()) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_COOLDOWN_LIST_EMPTY,
                                        Map.of(SCOPE_TYPE_PH.getPlaceholder(), scopeType,
                                                SCOPE_ID.getPlaceholder(), scopeId)));
                                return;
                            }

                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_COOLDOWN_LIST_HEADER_MSG,
                                    Map.of(SCOPE_TYPE_PH.getPlaceholder(), scopeType,
                                            SCOPE_ID.getPlaceholder(), scopeId,
                                            COUNT.getPlaceholder(), String.valueOf(records.size()))));

                            for (var r : records) {
                                String expires = TIME_FORMAT.format(Instant.ofEpochMilli(r.expiresAt()));
                                String category = r.categoryKey() != null ? r.categoryKey() : "n/a";
                                String quest = r.questDefinitionKey() != null ? r.questDefinitionKey() : "n/a";
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_COOLDOWN_LIST_ENTRY,
                                        Map.of(COOLDOWN_TYPE.getPlaceholder(), r.cooldownType(),
                                                CATEGORY.getPlaceholder(), category,
                                                QUEST_DEF.getPlaceholder(), quest,
                                                EXPIRES.getPlaceholder(), expires)));
                            }
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_COOLDOWN_RESET_ERROR,
                                    Map.of(SCOPE_TYPE_PH.getPlaceholder(), scopeType,
                                            SCOPE_ID.getPlaceholder(), scopeId)));
                            e.printStackTrace();
                        }
                    });
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("cooldown").literal("reset")
                .required("scope-type", QuestScopeTypeParser.scopeTypeParser())
                .required("scope-id", stringParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    String scopeType = commandContext.get("scope-type");
                    String scopeId = commandContext.get("scope-id");

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            int deleted = BoardCooldownDAO.deleteCooldowns(connection, scopeType, scopeId, null);
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_COOLDOWN_RESET_SUCCESS,
                                    Map.of(DELETED_COUNT.getPlaceholder(), String.valueOf(deleted),
                                            SCOPE_TYPE_PH.getPlaceholder(), scopeType,
                                            SCOPE_ID.getPlaceholder(), scopeId)));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_COOLDOWN_RESET_ERROR,
                                    Map.of(SCOPE_TYPE_PH.getPlaceholder(), scopeType,
                                            SCOPE_ID.getPlaceholder(), scopeId)));
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
