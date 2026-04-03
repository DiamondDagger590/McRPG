package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import com.diamonddagger590.mccore.task.core.CoreTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg statistic list <player>}
 * <p>
 * Lists all statistic values for a player. Supports both online and offline targets.
 */
public class StatisticListCommand extends StatisticCommandBase {

    private static final Permission LIST_PERMISSION = Permission.of("mcrpg.statistic.list");

    /** Registers the {@code /mcrpg statistic list} command. */
    public static void registerCommand() {
        McRPG mcRPG = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .literal("list")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_LIST_PLAYER)))
                .permission(Permission.anyOf(ROOT_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, LIST_PERMISSION))
                .handler(commandContext -> {
                    String playerName = commandContext.get(CloudKey.of("player", String.class));
                    Audience sender = commandContext.sender().getSender();
                    StatisticRegistry statisticRegistry = mcRPG.registryAccess().registry(RegistryKey.STATISTIC);

                    @SuppressWarnings("deprecation")
                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                    // Online player: read directly
                    Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                            .getPlayer(target.getUniqueId());
                    if (onlineOptional.isPresent()) {
                        McRPGPlayer mcRPGPlayer = onlineOptional.get();
                        sendStatisticList(sender, playerName, statisticRegistry, statistic -> {
                            return mcRPGPlayer.getStatisticData().getValue(statistic.getStatisticKey())
                                    .orElse(statistic.getDefaultValue());
                        }, localizationManager);
                        return;
                    }

                    // Offline player: async DB query
                    Map<String, String> loadingPlaceholders = getStatisticPlaceholders(sender, sender, sender, null, null);
                    loadingPlaceholders.put("target", playerName);
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                            sender, LocalizationKey.STATISTIC_LOADING_MESSAGE, loadingPlaceholders));

                    Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase();
                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            Map<NamespacedKey, StatisticEntry> allEntries =
                                    PlayerStatisticDAO.getAllPlayerStatistics(connection, target.getUniqueId());

                            new CoreTask(mcRPG) {
                                @Override
                                public void run() {
                                    sendStatisticList(sender, playerName, statisticRegistry, statistic -> {
                                        StatisticEntry entry = allEntries.get(statistic.getStatisticKey());
                                        return entry != null ? entry.value() : statistic.getDefaultValue();
                                    }, localizationManager);
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

    /**
     * Sends the formatted statistic list to the sender.
     *
     * @param sender              The audience to send messages to.
     * @param playerName          The target player's name.
     * @param statisticRegistry   The statistic registry.
     * @param valueResolver       A function that resolves a statistic's value.
     * @param localizationManager The localization manager.
     */
    private static void sendStatisticList(
            @NotNull Audience sender,
            @NotNull String playerName,
            @NotNull StatisticRegistry statisticRegistry,
            @NotNull StatisticValueResolver valueResolver,
            @NotNull McRPGLocalizationManager localizationManager) {

        Map<String, String> headerPlaceholders = getStatisticPlaceholders(sender, sender, sender, null, null);
        headerPlaceholders.put("target", playerName);
        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                sender, LocalizationKey.STATISTIC_LIST_HEADER, headerPlaceholders));

        for (Statistic statistic : statisticRegistry.getRegisteredStatistics()) {
            Object value = valueResolver.resolve(statistic);
            Map<String, String> entryPlaceholders = getStatisticPlaceholders(sender, sender, sender, statistic, value);
            entryPlaceholders.put("target", playerName);
            sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    sender, LocalizationKey.STATISTIC_LIST_ENTRY, entryPlaceholders));
        }
    }

    @FunctionalInterface
    private interface StatisticValueResolver {
        Object resolve(@NotNull Statistic statistic);
    }
}
