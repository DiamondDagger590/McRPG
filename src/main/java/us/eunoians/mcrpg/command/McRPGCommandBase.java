package us.eunoians.mcrpg.command;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.SENDER;
import static us.eunoians.mcrpg.command.CommandPlaceholders.TARGET;

/**
 * The base for any McRPG commands which provides some general functionality shared
 * across commands.
 */
public abstract class McRPGCommandBase {

    protected static final Permission ROOT_PERMISSION = Permission.of("mcrpg.*");

    /**
     * Gets the name of console to use if the sender of a command is ever console and the {@code <sender>}
     * placeholder is ever used.
     *
     * @param audience The {@link Audience} to localize for.
     * @return The localized name of console to use.
     */
    @NotNull
    public static String getConsoleName(@NotNull Audience audience) {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(audience, LocalizationKey.CONSOLE_NAME);
    }

    /**
     * Gets a {@link Map} of common placeholders for a command based on the sender/receiver provided and localized
     * based on the provided {@link Audience}.
     *
     * @param audience         The audience to localize for. It is probable that this is the same as the sender or receiver.
     * @param senderAudience   The audience who executed the command. There's a chance the sender audience is the same as
     *                         the receiver if a sender targets themselves when running a command.
     * @param receiverAudience The audience who was the target of the command. There's a chance the receiver audience is the
     *                         same as the sender if a sender targets themselves when running a command.
     * @return A {@link Map} of common placeholders for a command based on the sender/receiver provided and localized
     * based on the provided {@link Audience}.
     */
    @NotNull
    public static Map<String, String> getPlaceholders(@NotNull Audience audience, @NotNull Audience senderAudience, @NotNull Audience receiverAudience) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(SENDER.getPlaceholder(), senderAudience instanceof Player player ? player.getName() : getConsoleName(audience));
        placeholders.put(TARGET.getPlaceholder(), receiverAudience instanceof Player player ? player.getName() : getConsoleName(audience));
        return placeholders;
    }
}
