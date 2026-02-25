package us.eunoians.mcrpg.command.admin.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.parser.quest.QuestOfferingIdParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoardManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Commands: {@code /mcrpg board admin player offerings|accepted|abandon <player> [offering-id]}
 */
public class BoardAdminPlayerCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.player");

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("player").literal("offerings")
                .required("player", PlayerParser.playerParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    Player target = commandContext.get(CloudKey.of("player", Player.class));
                    UUID playerUUID = target.getUniqueId();

                    QuestBoardManager boardManager = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.QUEST_BOARD);

                    var scopedOfferings = boardManager.getScopedOfferingsForPlayer(playerUUID);
                    int scopedCount = scopedOfferings.values().stream().mapToInt(List::size).sum();

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_PLAYER_OFFERINGS_HEADER_MSG,
                            Map.of(TARGET.getPlaceholder(), target.getName())));
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_PLAYER_OFFERINGS_SCOPED_COUNT,
                            Map.of(COUNT.getPlaceholder(), String.valueOf(scopedCount))));

                    for (var entry : scopedOfferings.entrySet()) {
                        for (BoardOffering o : entry.getValue()) {
                            String id = o.getOfferingId().toString().substring(0, 8);
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_PLAYER_OFFERINGS_ENTRY,
                                    Map.of(ENTITY_ID.getPlaceholder(), entry.getKey(),
                                            OFFERING_ID.getPlaceholder(), id,
                                            QUEST_DEF.getPlaceholder(), o.getQuestDefinitionKey().getKey(),
                                            RARITY.getPlaceholder(), o.getRarityKey().getKey())));
                        }
                    }
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("player").literal("accepted")
                .required("player", PlayerParser.playerParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    Player target = commandContext.get(CloudKey.of("player", Player.class));
                    UUID playerUUID = target.getUniqueId();

                    QuestManager questManager = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.QUEST);

                    List<QuestInstance> boardQuests = questManager.getActiveQuestsForPlayer(playerUUID).stream()
                            .filter(q -> q.getBoardRarityKey().isPresent())
                            .toList();

                    if (boardQuests.isEmpty()) {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.BOARD_ADMIN_PLAYER_ACCEPTED_EMPTY,
                                Map.of(TARGET.getPlaceholder(), target.getName())));
                        return;
                    }

                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                            LocalizationKey.BOARD_ADMIN_PLAYER_ACCEPTED_HEADER_MSG,
                            Map.of(TARGET.getPlaceholder(), target.getName(),
                                    COUNT.getPlaceholder(), String.valueOf(boardQuests.size()))));

                    for (QuestInstance q : boardQuests) {
                        String uuid = q.getQuestUUID().toString().substring(0, 8);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                LocalizationKey.BOARD_ADMIN_PLAYER_ACCEPTED_ENTRY,
                                Map.of(QUEST_UUID.getPlaceholder(), uuid,
                                        QUEST_KEY.getPlaceholder(), q.getQuestKey().getKey(),
                                        QUEST_STATE.getPlaceholder(), q.getQuestState().name(),
                                        RARITY.getPlaceholder(), q.getBoardRarityKey().map(k -> k.getKey()).orElse("?"))));
                    }
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("player").literal("abandon")
                .required("player", PlayerParser.playerParser())
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
                                        LocalizationKey.BOARD_ADMIN_PLAYER_ABANDON_NOT_FOUND,
                                        Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                                return;
                            }
                            BoardOffering offering = opt.get();
                            if (offering.getState() != BoardOffering.State.ACCEPTED) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_PLAYER_ABANDON_INVALID_STATE,
                                        Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr,
                                                OFFERING_STATE.getPlaceholder(), offering.getState().name())));
                                return;
                            }
                            for (var ps : BoardOfferingDAO.updateOfferingState(connection, offeringId,
                                    BoardOffering.State.ABANDONED, null, null)) {
                                ps.executeUpdate();
                                ps.close();
                            }
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_PLAYER_ABANDON_SUCCESS,
                                    Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_PLAYER_ABANDON_ERROR,
                                    Map.of(OFFERING_ID.getPlaceholder(), offeringIdStr)));
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
