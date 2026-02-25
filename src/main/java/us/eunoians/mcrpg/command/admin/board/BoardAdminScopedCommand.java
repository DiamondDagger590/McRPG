package us.eunoians.mcrpg.command.admin.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.parser.quest.QuestScopeEntityIdParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.ScopedBoardStateDAO;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Commands: {@code /mcrpg board admin scoped list|reset|generate <entity-id>}
 */
public class BoardAdminScopedCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.scoped");
    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("scoped").literal("list")
                .required("entity-id", QuestScopeEntityIdParser.entityIdParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    String entityId = commandContext.get("entity-id");

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            List<ScopedBoardStateDAO.ScopedBoardStateRecord> records =
                                    ScopedBoardStateDAO.loadStatesForEntity(connection, entityId, DEFAULT_BOARD_KEY);

                            if (records.isEmpty()) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_SCOPED_LIST_EMPTY,
                                        Map.of(ENTITY_ID.getPlaceholder(), entityId)));
                                return;
                            }

                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_SCOPED_LIST_HEADER,
                                    Map.of(ENTITY_ID.getPlaceholder(), entityId,
                                            COUNT.getPlaceholder(), String.valueOf(records.size()))));

                            for (var r : records) {
                                String offId = r.offeringId().toString().substring(0, 8);
                                String acceptedBy = r.acceptedBy() != null ? r.acceptedBy().toString().substring(0, 8) : "none";
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_SCOPED_LIST_ENTRY,
                                        Map.of(OFFERING_ID.getPlaceholder(), offId,
                                                OFFERING_STATE.getPlaceholder(), r.state(),
                                                ACCEPTED_BY.getPlaceholder(), acceptedBy)));
                            }
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_SCOPED_RESET_ERROR,
                                    Map.of(ENTITY_ID.getPlaceholder(), entityId)));
                            e.printStackTrace();
                        }
                    });
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("scoped").literal("reset")
                .required("entity-id", QuestScopeEntityIdParser.entityIdParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    String entityId = commandContext.get("entity-id");

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            List<ScopedBoardStateDAO.ScopedBoardStateRecord> accepted =
                                    ScopedBoardStateDAO.loadAcceptedStatesForEntity(connection, entityId);
                            int cancelledQuests = 0;
                            for (var r : accepted) {
                                if (r.questInstanceUUID() != null) {
                                    for (var ps : QuestInstanceDAO.deleteQuestInstance(connection, r.questInstanceUUID())) {
                                        ps.executeUpdate();
                                        ps.close();
                                    }
                                    cancelledQuests++;
                                }
                            }
                            for (var ps : ScopedBoardStateDAO.deleteStatesForEntity(connection, entityId)) {
                                ps.executeUpdate();
                                ps.close();
                            }
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_SCOPED_RESET_SUCCESS,
                                    Map.of(ENTITY_ID.getPlaceholder(), entityId,
                                            CANCELLED_COUNT.getPlaceholder(), String.valueOf(cancelledQuests))));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_SCOPED_RESET_ERROR,
                                    Map.of(ENTITY_ID.getPlaceholder(), entityId)));
                            e.printStackTrace();
                        }
                    });
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("scoped").literal("generate")
                .required("entity-id", QuestScopeEntityIdParser.entityIdParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    String entityId = commandContext.get("entity-id");
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_SCOPED_GENERATE_SUCCESS,
                            Map.of(ENTITY_ID.getPlaceholder(), entityId)));
                }));
    }
}
