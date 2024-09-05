package us.eunoians.mcrpg.util;

import com.lunarclient.apollo.Apollo;
import com.lunarclient.apollo.common.icon.ItemStackIcon;
import com.lunarclient.apollo.module.cooldown.Cooldown;
import com.lunarclient.apollo.module.cooldown.CooldownModule;
import com.lunarclient.apollo.player.ApolloPlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * A utility class for supporting LunarClient integration
 */
public class LunarUtils {

    /**
     * Checks to see if the provided {@link UUID} is running lunar client.
     * @param uuid The {@link UUID} to check.
     * @return {@code true} if the provided {@link UUID} is running lunar client.
     */
    public static boolean isPlayerRunningLunarClient(@NotNull UUID uuid) {
        return McRPG.getInstance().isLunarEnabled() && Apollo.getPlayerManager().hasSupport(uuid);
    }

    /**
     * Gets the {@link ApolloPlayer} that belongs to the provided {@link UUID}.
     * @param uuid The {@link UUID} to get the corresponding {@link ApolloPlayer} for.
     * @return An {@link Optional} containing the {@link ApolloPlayer} belonging to the provided {@link UUID},
     * or it will be empty if there is no match.
     */
    public static Optional<ApolloPlayer> getLunarPlayer(@NotNull UUID uuid) {
        return Apollo.getPlayerManager().getPlayer(uuid);
    }

    /**
     * Displays a cooldown on lunar client for the provided {@link UUID} if they are running LC.
     * @param uuid The player's {@link UUID}
     * @param itemStack The {@link ItemStack} to display
     * @param name The unique identifier of the cooldown
     * @param duration How long in seconds the cooldown should be displayed for
     */
    public static void displayCooldown(@NotNull UUID uuid, @NotNull ItemStack itemStack, @NotNull String name, long duration) {
        if (isPlayerRunningLunarClient(uuid)) {
            getLunarPlayer(uuid).ifPresent(apolloPlayer -> {
                CooldownModule cooldownModule = Apollo.getModuleManager().getModule(CooldownModule.class);
                cooldownModule.displayCooldown(apolloPlayer, Cooldown.builder()
                        .name(name)
                        .icon(ItemStackIcon.builder().itemName(itemStack.getType().name()).build())
                        .duration(Duration.ofSeconds(duration))
                        .build());
            });
        }
    }

    /**
     * Removes all lunar client cooldowns for the provided {@link UUID}.
     * @param uuid The {@link UUID} to remove cooldowns for.
     */
    public static void clearCooldowns(@NotNull UUID uuid) {
        if (isPlayerRunningLunarClient(uuid)) {
            getLunarPlayer(uuid).ifPresent(apolloPlayer -> {
                CooldownModule cooldownModule = Apollo.getModuleManager().getModule(CooldownModule.class);
                cooldownModule.resetCooldowns(apolloPlayer);
            });
        }
    }
}
