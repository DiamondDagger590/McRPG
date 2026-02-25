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
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.*;

/**
 * Commands: {@code /mcrpg board admin rewards pending|clear <player>}
 */
public class BoardAdminRewardsCommand extends McRPGCommandBase {

    private static final Permission PERMISSION = Permission.of("mcrpg.admin.board.rewards");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @SuppressWarnings("UnstableApiUsage")
    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("admin").literal("board").literal("rewards").literal("pending")
                .required("player", PlayerParser.playerParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    Player target = commandContext.get(CloudKey.of("player", Player.class));

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            List<PendingReward> rewards = PendingRewardDAO.listPendingRewards(connection, target.getUniqueId());

                            if (rewards.isEmpty()) {
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_REWARDS_PENDING_EMPTY,
                                        Map.of(TARGET.getPlaceholder(), target.getName())));
                                return;
                            }

                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_REWARDS_PENDING_HEADER_MSG,
                                    Map.of(TARGET.getPlaceholder(), target.getName(),
                                            COUNT.getPlaceholder(), String.valueOf(rewards.size()))));

                            for (PendingReward r : rewards) {
                                String expires = TIME_FORMAT.format(Instant.ofEpochMilli(r.getExpiresAt()));
                                sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.BOARD_ADMIN_REWARDS_PENDING_ENTRY,
                                        Map.of(REWARD_TYPE.getPlaceholder(), r.getRewardTypeKey().getKey(),
                                                QUEST_KEY.getPlaceholder(), r.getQuestKey().getKey(),
                                                EXPIRES.getPlaceholder(), expires)));
                            }
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_REWARDS_CLEAR_ERROR,
                                    Map.of(TARGET.getPlaceholder(), target.getName())));
                            e.printStackTrace();
                        }
                    });
                }));

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("board").literal("admin").literal("rewards").literal("clear")
                .required("player", PlayerParser.playerParser())
                .permission(Permission.anyOf(ROOT_PERMISSION, PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    Audience sender = commandContext.sender().getSender();
                    Player target = commandContext.get(CloudKey.of("player", Player.class));

                    Database database = McRPG.getInstance().registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            int deleted = PendingRewardDAO.deleteAllForPlayer(connection, target.getUniqueId());
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_REWARDS_CLEAR_SUCCESS,
                                    Map.of(DELETED_COUNT.getPlaceholder(), String.valueOf(deleted),
                                            TARGET.getPlaceholder(), target.getName())));
                        } catch (Exception e) {
                            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(sender,
                                    LocalizationKey.BOARD_ADMIN_REWARDS_CLEAR_ERROR,
                                    Map.of(TARGET.getPlaceholder(), target.getName())));
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
