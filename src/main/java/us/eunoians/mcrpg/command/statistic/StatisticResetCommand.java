package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import com.diamonddagger590.mccore.task.core.CoreTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.StatisticParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg statistic reset <player> [statistic]}
 * <p>
 * Resets a specific statistic (or all statistics) for a player. Supports both online
 * and offline targets. Requires {@code /mcrpg confirm}.
 */
public class StatisticResetCommand extends StatisticCommandBase {

    private static final Permission RESET_PERMISSION = Permission.of("mcrpg.statistic.reset");

    /** Registers the {@code /mcrpg statistic reset} command. */
    public static void registerCommand() {
        McRPG mcRPG = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        // Reset a specific statistic
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .literal("reset")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_RESET_PLAYER)))
                .required("statistic", StatisticParser.statisticParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_RESET_STATISTIC)))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, RESET_PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    String playerName = commandContext.get(CloudKey.of("player", String.class));
                    Statistic statistic = commandContext.get(CloudKey.of("statistic", Statistic.class));
                    Audience sender = commandContext.sender().getSender();
                    handleSingleReset(mcRPG, localizationManager, sender, playerName, statistic);
                }));

        // Reset all statistics
        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .literal("reset")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_RESET_PLAYER)))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, RESET_PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    String playerName = commandContext.get(CloudKey.of("player", String.class));
                    Audience sender = commandContext.sender().getSender();
                    handleResetAll(mcRPG, localizationManager, sender, playerName);
                }));
    }

    private static void handleSingleReset(
            @NotNull McRPG mcRPG,
            @NotNull McRPGLocalizationManager localizationManager,
            @NotNull Audience sender,
            @NotNull String playerName,
            @NotNull Statistic statistic) {

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        // Online player: set to default directly
        Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(target.getUniqueId());
        if (onlineOptional.isPresent()) {
            onlineOptional.get().getStatisticData().setValue(statistic.getStatisticKey(), statistic.getDefaultValue());
            Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, statistic.getDefaultValue());
            placeholders.put("target", playerName);
            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    sender, LocalizationKey.STATISTIC_RESET_SUCCESS, placeholders));
            return;
        }

        // Offline player: async DB delete + cache invalidation
        Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                new FailSafeTransaction(connection,
                        List.of(PlayerStatisticDAO.deletePlayerStatistic(connection, target.getUniqueId(), statistic.getStatisticKey())))
                        .executeTransaction();

                ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
                if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
                    managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE)
                            .getCache().invalidate(target.getUniqueId(), statistic.getStatisticKey());
                }

                Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, statistic.getDefaultValue());
                placeholders.put("target", playerName);
                new CoreTask(mcRPG) {
                    @Override
                    public void run() {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_RESET_SUCCESS, placeholders));
                    }
                }.runTask();
            } catch (SQLException e) {
                new CoreTask(mcRPG) {
                    @Override
                    public void run() {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_LOOKUP_ERROR_MESSAGE));
                    }
                }.runTask();
                mcRPG.getLogger().log(java.util.logging.Level.WARNING, "Failed to lookup statistic for offline player", e);
            }
        });
    }

    private static void handleResetAll(
            @NotNull McRPG mcRPG,
            @NotNull McRPGLocalizationManager localizationManager,
            @NotNull Audience sender,
            @NotNull String playerName) {

        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        StatisticRegistry statisticRegistry = mcRPG.registryAccess().registry(RegistryKey.STATISTIC);

        // Online player: reset all to defaults
        Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(target.getUniqueId());
        if (onlineOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = onlineOptional.get();
            for (Statistic stat : statisticRegistry.getRegisteredStatistics()) {
                mcRPGPlayer.getStatisticData().setValue(stat.getStatisticKey(), stat.getDefaultValue());
            }
            Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, null, null);
            placeholders.put("target", playerName);
            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    sender, LocalizationKey.STATISTIC_RESET_ALL_SUCCESS, placeholders));
            return;
        }

        // Offline player: async DB delete all + cache invalidation
        Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = new ArrayList<>();
                for (Statistic stat : statisticRegistry.getRegisteredStatistics()) {
                    statements.add(PlayerStatisticDAO.deletePlayerStatistic(
                            connection, target.getUniqueId(), stat.getStatisticKey()));
                }
                new FailSafeTransaction(connection, statements).executeTransaction();

                ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
                if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
                    managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE)
                            .getCache().invalidate(target.getUniqueId());
                }

                Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, null, null);
                placeholders.put("target", playerName);
                new CoreTask(mcRPG) {
                    @Override
                    public void run() {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_RESET_ALL_SUCCESS, placeholders));
                    }
                }.runTask();
            } catch (SQLException e) {
                new CoreTask(mcRPG) {
                    @Override
                    public void run() {
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_LOOKUP_ERROR_MESSAGE));
                    }
                }.runTask();
                mcRPG.getLogger().log(java.util.logging.Level.WARNING, "Failed to lookup statistic for offline player", e);
            }
        });
    }
}
