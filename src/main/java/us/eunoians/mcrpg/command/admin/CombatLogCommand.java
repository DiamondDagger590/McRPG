package us.eunoians.mcrpg.command.admin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.diamonddagger590.mccore.registry.RegistryKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.log.CombatLogEntry;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.CombatLogDAO;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Admin command that displays a paginated history of combat log incidents for a player.
 * Each entry shows the timestamp, combat type, and a clickable location that teleports
 * staff to the incident location. Supports online and offline players via Paper's async
 * {@link PlayerProfile} API for name-to-UUID resolution.
 * <p>
 * Usage: {@code /mcrpg combatlog <player> [page]}
 */
public class CombatLogCommand extends McRPGCommandBase {

    public static final Permission COMBATLOG_PERMISSION = Permission.of("mcrpg.admin.combatlog");
    private static final int PAGE_SIZE = 10;
    private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final CloudKey<String> PLAYER_KEY = CloudKey.of("player", String.class);
    private static final CloudKey<Integer> PAGE_KEY = CloudKey.of("page", Integer.class);

    /**
     * Registers the combat log command with the Cloud command manager.
     */
    public static void registerCommand() {
        McRPG plugin = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("combatlog")
                .required("player", StringParser.stringParser(),
                        RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(
                                LocalizationKey.COMMAND_DESCRIPTION_COMBAT_LOG_PLAYER)))
                .optional("page", IntegerParser.integerParser(1),
                        RichDescription.richDescription(localizationManager.getLocalizedMessageAsComponent(
                                LocalizationKey.COMMAND_DESCRIPTION_COMBAT_LOG_PAGE)))
                .permission(Permission.anyOf(
                        ROOT_PERMISSION,
                        AdminBaseCommand.ADMIN_BASE_PERMISSION,
                        COMBATLOG_PERMISSION))
                .handler(commandContext -> {
                    CommandSender sender = commandContext.sender().getSender();
                    String playerName = commandContext.get(PLAYER_KEY);
                    int page = commandContext.getOrDefault(PAGE_KEY, 1);

                    PlayerProfile profile = Bukkit.getServer().createProfile(playerName);
                    profile.update().thenAcceptAsync(resolvedProfile -> {
                        UUID targetUUID = resolvedProfile.getId();
                        if (targetUUID == null) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                McRPGLocalizationManager lm = plugin.registryAccess()
                                        .registry(RegistryKey.MANAGER)
                                        .manager(McRPGManagerKey.LOCALIZATION);
                                Component notFound = lm.getLocalizedMessageAsComponent(sender,
                                        LocalizationKey.COMBAT_LOG_PLAYER_NOT_FOUND,
                                        Map.of("player", playerName));
                                sender.sendMessage(notFound);
                            });
                            return;
                        }

                        String resolvedName = resolvedProfile.getName() != null
                                ? resolvedProfile.getName() : playerName;

                        var database = plugin.registryAccess().registry(RegistryKey.MANAGER)
                                .manager(McRPGManagerKey.DATABASE).getDatabase();
                        database.getDatabaseExecutorService().submit(() -> {
                            try (Connection conn = database.getConnection()) {
                                int totalEntries = CombatLogDAO.getCombatLogCount(conn, targetUUID);
                                List<CombatLogEntry> entries =
                                        CombatLogDAO.getCombatLogHistory(conn, targetUUID, page, PAGE_SIZE);

                                Bukkit.getScheduler().runTask(plugin, () ->
                                        sendPaginatedResults(sender, resolvedName, entries, page, totalEntries));
                            }
                            catch (Exception e) {
                                plugin.getLogger().log(Level.WARNING,
                                        "Failed to query combat log for " + resolvedName, e);
                            }
                        });
                    }, Bukkit.getScheduler().getMainThreadExecutor(plugin));
                }));
    }

    /**
     * Formats and sends paginated combat log results to the command sender.
     *
     * @param sender       The command sender.
     * @param targetName   The target player's name.
     * @param entries      The combat log entries for this page.
     * @param page         The current page number.
     * @param totalEntries The total number of entries across all pages.
     */
    private static void sendPaginatedResults(@NotNull CommandSender sender,
                                             @NotNull String targetName,
                                             @NotNull List<CombatLogEntry> entries,
                                             int page, int totalEntries) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        DateTimeFormatter timestampFormatter = resolveTimestampFormatter(plugin);

        if (entries.isEmpty()) {
            Component noEntries = localizationManager.getLocalizedMessageAsComponent(sender,
                    LocalizationKey.COMBAT_LOG_HISTORY_NO_ENTRIES,
                    Map.of("player", targetName));
            sender.sendMessage(noEntries);
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / PAGE_SIZE));

        Component header = localizationManager.getLocalizedMessageAsComponent(sender,
                LocalizationKey.COMBAT_LOG_HISTORY_HEADER,
                Map.of("player", targetName,
                        "page", String.valueOf(page),
                        "total_pages", String.valueOf(totalPages)));
        sender.sendMessage(header);

        for (int i = 0; i < entries.size(); i++) {
            CombatLogEntry entry = entries.get(i);
            Component entryComponent = localizationManager.getLocalizedMessageAsComponent(sender,
                    LocalizationKey.COMBAT_LOG_HISTORY_ENTRY,
                    Map.of(
                            "index", String.valueOf((page - 1) * PAGE_SIZE + i + 1),
                            "timestamp", timestampFormatter.format(entry.timestamp()),
                            "combat_type", entry.combatType().name(),
                            "world", entry.world(),
                            "x", String.valueOf((int) entry.x()),
                            "y", String.valueOf((int) entry.y()),
                            "z", String.valueOf((int) entry.z()),
                            "punishments", entry.punishmentsApplied().stream()
                                    .map(type -> type.getKey().getKey())
                                    .reduce((a, b) -> a + ", " + b)
                                    .orElse("none")
                    ));
            sender.sendMessage(entryComponent);
        }

        Component footer = localizationManager.getLocalizedMessageAsComponent(sender,
                LocalizationKey.COMBAT_LOG_HISTORY_FOOTER,
                Map.of("page", String.valueOf(page),
                        "total_pages", String.valueOf(totalPages)));
        sender.sendMessage(footer);
    }

    /**
     * Resolves the configured {@code combat-log.history-timestamp-format} pattern into a
     * {@link DateTimeFormatter}, rendered in the server's local time zone. Read fresh on every
     * invocation rather than cached — this command runs infrequently enough that re-parsing the
     * pattern string each time costs nothing, and it means a config change takes effect on the
     * very next run with no explicit reload wiring. Falls back to {@link #DEFAULT_TIMESTAMP_PATTERN}
     * and logs a warning if the configured pattern is not a valid {@link DateTimeFormatter} pattern.
     *
     * @param plugin The plugin instance for config access and logging.
     * @return The resolved {@link DateTimeFormatter}.
     */
    @NotNull
    private static DateTimeFormatter resolveTimestampFormatter(@NotNull McRPG plugin) {
        String pattern = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.COMBAT_CONFIG)
                .getString(CombatConfigFile.COMBAT_LOG_HISTORY_TIMESTAMP_FORMAT, DEFAULT_TIMESTAMP_PATTERN);
        try {
            return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        }
        catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid combat-log.history-timestamp-format pattern '"
                    + pattern + "', falling back to '" + DEFAULT_TIMESTAMP_PATTERN + "'", e);
            return DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN).withZone(ZoneId.systemDefault());
        }
    }
}
