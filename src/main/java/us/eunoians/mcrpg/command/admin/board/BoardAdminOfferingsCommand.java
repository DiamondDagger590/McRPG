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
import us.eunoians.mcrpg.command.parser.quest.QuestOfferingIdParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Commands: {@code /mcrpg board admin offerings list}
 * and {@code /mcrpg board admin offerings expire <offering-id>}
 */
public class BoardAdminOfferingsCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.offerings");
    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("offerings").literal("list")
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    QuestBoardManager boardManager = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.QUEST_BOARD);

                    List<BoardOffering> offerings = boardManager.getSharedOfferingsForBoard(DEFAULT_BOARD_KEY).stream()
                            .filter(o -> o.getState() == BoardOffering.State.VISIBLE)
                            .filter(o -> o.getScopeTargetId().isEmpty())
                            .toList();

                    if (offerings.isEmpty()) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.BOARD_ADMIN_OFFERINGS_LIST_EMPTY));
                        return;
                    }

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_OFFERINGS_LIST_HEADER,
                            Map.of(COUNT.getPlaceholder(), String.valueOf(offerings.size()))));

                    for (BoardOffering o : offerings) {
                        String id = o.getOfferingId().toString().substring(0, 8);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.BOARD_ADMIN_OFFERINGS_LIST_ENTRY,
                                Map.of(OFFERING_ID.getPlaceholder(), id,
                                        QUEST_DEF.getPlaceholder(), o.getQuestDefinitionKey().getKey(),
                                        RARITY.getPlaceholder(), o.getRarityKey().getKey(),
                                        CATEGORY.getPlaceholder(), o.getCategoryKey().getKey())));
                    }
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("offerings").literal("expire")
                .required("offering-id", QuestOfferingIdParser.offeringIdParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    UUID offeringId = commandContext.get("offering-id");
                    String offeringIdStr = offeringId.toString();

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            Optional<BoardOffering> opt = BoardOfferingDAO.loadOfferingById(connection, offeringId);
                            if (opt.isEmpty()) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_OFFERINGS_EXPIRE_NOT_FOUND,
                                        Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                                return;
                            }
                            BoardOffering offering = opt.get();
                            if (!offering.canTransitionTo(BoardOffering.State.EXPIRED)) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_OFFERINGS_EXPIRE_INVALID_STATE,
                                        Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr,
                                                OFFERING_STATE.getPlaceholder(), offering.getState().name())));
                                return;
                            }
                            for (var ps : BoardOfferingDAO.updateOfferingState(connection, offeringId,
                                    BoardOffering.State.EXPIRED, null, null)) {
                                ps.executeUpdate();
                                ps.close();
                            }
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_OFFERINGS_EXPIRE_SUCCESS,
                                    Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_OFFERINGS_EXPIRE_ERROR,
                                    Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
