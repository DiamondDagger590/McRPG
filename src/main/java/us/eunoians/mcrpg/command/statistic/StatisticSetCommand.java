package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
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
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.StatisticParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg statistic set <player> <statistic> <value>}
 * <p>
 * Sets a statistic value for a player. Supports both online and offline targets.
 * Requires {@code /mcrpg confirm}.
 */
public class StatisticSetCommand extends StatisticCommandBase {

    private static final Permission SET_PERMISSION = Permission.of("mcrpg.statistic.set");

    /** Registers the {@code /mcrpg statistic set} command. */
    public static void registerCommand() {
        McRPG mcRPG = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .literal("set")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_SET_PLAYER)))
                .required("statistic", StatisticParser.statisticParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_SET_STATISTIC)))
                .required("value", StringParser.stringParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_SET_VALUE)))
                .permission(Permission.anyOf(ROOT_PERMISSION, ADMIN_BASE_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, SET_PERMISSION))
                .meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)
                .handler(commandContext -> {
                    String playerName = commandContext.get(CloudKey.of("player", String.class));
                    Statistic statistic = commandContext.get(CloudKey.of("statistic", Statistic.class));
                    String valueString = commandContext.get(CloudKey.of("value", String.class));
                    Audience sender = commandContext.sender().getSender();

                    // Parse value based on statistic type
                    Object parsedValue;
                    try {
                        parsedValue = parseValue(statistic, valueString);
                    } catch (IllegalArgumentException e) {
                        Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, null);
                        placeholders.put("target", playerName);
                        placeholders.put("statistic-value", valueString);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_INVALID_VALUE, placeholders));
                        return;
                    }

                    @SuppressWarnings("deprecation")
                    OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

                    // Online player: set directly
                    Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                            .getPlayer(target.getUniqueId());
                    if (onlineOptional.isPresent()) {
                        onlineOptional.get().getStatisticData().setValue(statistic.getStatisticKey(), parsedValue);
                        Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, parsedValue);
                        placeholders.put("target", playerName);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_SET_SUCCESS, placeholders));
                        return;
                    }

                    // Offline player: async DB write
                    Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase();
                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            StatisticEntry entry = new StatisticEntry(
                                    statistic.getStatisticKey(), statistic.getStatisticType(), parsedValue);
                            new FailSafeTransaction(connection,
                                    List.of(PlayerStatisticDAO.savePlayerStatistic(connection, target.getUniqueId(), entry)))
                                    .executeTransaction();

                            // Invalidate cache
                            ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
                            if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
                                managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE)
                                        .getCache().invalidate(target.getUniqueId(), statistic.getStatisticKey());
                            }

                            Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, parsedValue);
                            placeholders.put("target", playerName);
                            new CoreTask(mcRPG) {
                                @Override
                                public void run() {
                                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                            sender, LocalizationKey.STATISTIC_SET_SUCCESS, placeholders));
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
     * Parses a string value into the appropriate type for the given statistic.
     *
     * @param statistic   The statistic to parse the value for.
     * @param valueString The string value to parse.
     * @return The parsed value.
     * @throws NumberFormatException      If the value cannot be parsed as the expected numeric type.
     * @throws IllegalArgumentException If the statistic type does not support direct value setting.
     */
    @NotNull
    private static Object parseValue(@NotNull Statistic statistic, @NotNull String valueString) {
        return switch (statistic.getStatisticType()) {
            case INT -> Integer.parseInt(valueString);
            case LONG -> Long.parseLong(valueString);
            case DOUBLE -> Double.parseDouble(valueString);
            case STRING -> valueString;
            case TIMESTAMP -> throw new IllegalArgumentException("Timestamp statistics cannot be set via command");
            case SET_STRING -> throw new IllegalArgumentException("Set statistics cannot be set via command");
        };
    }
}
