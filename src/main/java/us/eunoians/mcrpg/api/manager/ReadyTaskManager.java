package us.eunoians.mcrpg.api.manager;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.ReadyableAbility;
import us.eunoians.mcrpg.api.AbilityHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class handles storing all of a {@link us.eunoians.mcrpg.player.McRPGPlayer}s tasks where they are
 * in a "ready" state and can activate the {@link us.eunoians.mcrpg.ability.ReadyableAbility} by some sort action.
 * <p>
 * This manager also assumes a single-ready state where a player can only be readying one ability at a time and can not ready
 * another until the existing "ready" status expires.
 *
 * @author DiamondDagger590
 */
public class ReadyTaskManager {

    @NotNull
    private Map<UUID, ReadyWrapper> readyTasks;

    public ReadyTaskManager() {
        readyTasks = new HashMap<>();
    }

    /**
     * Starts a task that will mark the provided {@link ReadyableAbility} as "ready" for activation and will auto cancel after the provided duration
     * <p>
     * It is up to implementor to ensure that {@link #hasReadyStatus(UUID)} is {@code false} before calling this method.
     *
     * @param abilityHolder          The {@link AbilityHolder} that is having the ready status activated
     * @param readyableAbility     The {@link ReadyableAbility} that is being marked as ready
     * @param readyDurationSeconds The amount of time in seconds that the {@link ReadyableAbility} should be marked as ready for
     */
    public void startReadyTask(@NotNull AbilityHolder abilityHolder, @NotNull ReadyableAbility readyableAbility, long readyDurationSeconds) {

        LivingEntity livingEntity = abilityHolder.getEntity();

        if(livingEntity == null){
            return;
        }

        boolean isPlayer = livingEntity instanceof Player;

        if (!readyableAbility.isReady()) {

            readyableAbility.setReady(true);

            if(isPlayer) {
                McRPG.getInstance().getMessageSender().sendMessage((Player) livingEntity, ChatColor.YELLOW + "Readying...", false);
            }
            BukkitTask bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {

                    //Check to validate that they are still ready
                    if (readyableAbility.isReady()) {
                        readyableAbility.setReady(false);

                        if(isPlayer) {
                            McRPG.getInstance().getMessageSender().sendMessage((Player) livingEntity, ChatColor.RED + "You are no longer ready...", false);
                        }
                    }

                    readyTasks.remove(livingEntity.getUniqueId());
                }
            }.runTaskLater(McRPG.getInstance(), readyDurationSeconds * 20);//TODO configure

            readyTasks.put(livingEntity.getUniqueId(), new ReadyWrapper(bukkitTask, readyableAbility));
        }
    }

    /**
     * Checks to see if the provided {@link UUID} has an ongoing "ready" state
     *
     * @param uuid The {@link UUID} to validate
     * @return {@code true} if the provided {@link UUID} has an ongoing "ready" state
     */
    public boolean hasReadyStatus(@NotNull UUID uuid) {
        return readyTasks.containsKey(uuid);
    }

    /**
     * Gets the {@link ReadyWrapper} that is currently being used by the provided {@link UUID}
     *
     * @param uuid The {@link UUID} to get the {@link ReadyWrapper} for
     * @return {@code null} if there is no {@link ReadyWrapper} associated with the provided {@link UUID} or the
     * {@link ReadyWrapper} if there is one.
     */
    @Nullable
    public ReadyWrapper getReadyWrapper(@NotNull UUID uuid) {
        return readyTasks.get(uuid);
    }

    /**
     * Ends and cleans up the {@link ReadyWrapper} for the provided {@link UUID}
     *
     * @param uuid The {@link UUID} to clean up the related {@link ReadyWrapper} for
     */
    public void endReadyTask(@NotNull UUID uuid) {

        if (readyTasks.containsKey(uuid)) {

            ReadyWrapper readyWrapper = readyTasks.remove(uuid);

            readyWrapper.getReadyTask().cancel();
            readyWrapper.getReadyableAbility().setReady(false);
        }
    }

    /**
     * This class acts as a wrapper containing a {@link BukkitTask} and {@link ReadyableAbility} that are needed
     * for preemptive cancellation via {@link #endReadyTask(UUID)}
     *
     * @author DiamondDagger590
     */
    private class ReadyWrapper {

        @NotNull
        private final BukkitTask readyTask;

        @NotNull
        private final ReadyableAbility readyableAbility;

        public ReadyWrapper(@NotNull BukkitTask readyTask, @NotNull ReadyableAbility readyableAbility) {
            this.readyTask = readyTask;
            this.readyableAbility = readyableAbility;
        }

        /**
         * Gets the {@link BukkitTask} that is set to auto unready the {@link #getReadyableAbility()}
         *
         * @return The {@link BukkitTask} that is set to auto unready the {@link #getReadyableAbility()}
         */
        public BukkitTask getReadyTask() {
            return readyTask;
        }

        /**
         * Gets the {@link ReadyableAbility} that is set to be auto unready'd by {@link #getReadyableAbility()}
         *
         * @return The {@link ReadyableAbility} that is set to be auto unready'd by {@link #getReadyableAbility()}
         */
        public ReadyableAbility getReadyableAbility() {
            return readyableAbility;
        }
    }
}
