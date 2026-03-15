package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.statistic.Statistic;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.external.papi.placeholder.statistic.StatisticPlaceholder;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.STATISTIC_NAME;
import static us.eunoians.mcrpg.command.CommandPlaceholders.STATISTIC_VALUE;

/**
 * Base command for all {@code /mcrpg statistic} commands.
 */
public class StatisticCommandBase extends AdminBaseCommand {

    protected static final Permission STATISTIC_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.statistic.*");

    /**
     * Builds a placeholder map that includes the standard sender/target placeholders
     * plus the statistic name and formatted value.
     *
     * @param messageAudience  The audience the message is being built for.
     * @param senderAudience   The command sender.
     * @param receiverAudience The command target.
     * @param statistic        The statistic, or {@code null} if not applicable.
     * @param value            The statistic value, or {@code null} for default.
     * @return A mutable placeholder map.
     */
    @NotNull
    protected static Map<String, String> getStatisticPlaceholders(
            @NotNull Audience messageAudience,
            @NotNull Audience senderAudience,
            @NotNull Audience receiverAudience,
            @Nullable Statistic statistic,
            @Nullable Object value) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(STATISTIC_NAME.getPlaceholder(), statistic != null ? statistic.getDisplayName() : "Unknown");
        placeholders.put(STATISTIC_VALUE.getPlaceholder(), StatisticPlaceholder.formatValue(value));
        return placeholders;
    }
}
