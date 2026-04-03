package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
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
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.StatisticParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg statistic view <player> <statistic>}
 * <p>
 * Views a specific statistic value for a player. Supports both online and offline targets.
 */
public class StatisticViewCommand extends StatisticCommandBase {

    private static final Permission VIEW_PERMISSION = Permission.of("mcrpg.statistic.view");

    /** Registers the {@code /mcrpg statistic view} command. */
    public static void registerCommand() {
        McRPG mcRPG = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .commandDescription(RichDescription.richDescription(
                        localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC)))
                .literal("view")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_VIEW_PLAYER)))
                .required("statistic", StatisticParser.statisticParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_VIEW_STATISTIC)))
                .permission(Permission.anyOf(ROOT_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, VIEW_PERMISSION))
                .handler(commandContext -> {
                    String playerName = commandContext.get(CloudKey.of("player", String.class));
                    Statistic statistic = commandContext.get(CloudKey.of("statistic", Statistic.class));
                    Audience sender = commandContext.sender().getSender();

                    @SuppressWarnings("deprecation")
                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                    // Online player: read directly
                    Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                            .getPlayer(target.getUniqueId());
                    if (onlineOptional.isPresent()) {
                        Object value = onlineOptional.get().getStatisticData()
                                .getValue(statistic.getStatisticKey()).orElse(statistic.getDefaultValue());
                        Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, value);
                        placeholders.put("target", playerName);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_VIEW_MESSAGE, placeholders));
                        return;
                    }

                    // Offline player: async DB query
                    Map<String, String> loadingPlaceholders = getStatisticPlaceholders(sender, sender, sender, statistic, null);
                    loadingPlaceholders.put("target", playerName);
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                            sender, LocalizationKey.STATISTIC_LOADING_MESSAGE, loadingPlaceholders));

                    Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase();
                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            Optional<StatisticEntry> entry = PlayerStatisticDAO.getPlayerStatistic(
                                    connection, target.getUniqueId(), statistic.getStatisticKey());
                            Object value = entry.map(StatisticEntry::value).orElse(statistic.getDefaultValue());

                            // Cache the result if cache manager is available
                            ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
                            if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
                                entry.ifPresent(e -> managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE)
                                        .getCache().put(target.getUniqueId(), statistic.getStatisticKey(), e));
                            }

                            Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, value);
                            placeholders.put("target", playerName);
                            new CoreTask(mcRPG) {
                                @Override
                                public void run() {
                                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                            sender, LocalizationKey.STATISTIC_VIEW_MESSAGE, placeholders));
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
                }));
    }
}
