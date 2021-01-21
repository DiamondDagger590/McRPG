package us.eunoians.mcrpg.ability;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This interface represents an {@link Ability} that requires some sort of activation
 * action to be done before activation can be performed.
 *
 * @author DiamondDagger590
 */
public interface ReadyableAbility extends Ability {

    Map<UUID, BukkitTask> readyTasks = new HashMap<>();

    /**
     * Checks to see if this ability is currently in a ready status
     *
     * @return {@code true} if this ability is currently in a ready status
     */
    public boolean isReady();

    /**
     * Sets if this ability is currently in a ready status or not
     *
     * @param ready If this ability should be in a ready state or note
     */
    public void setReady(boolean ready);

    /**
     * Handles parsing an {@link Event} to see if this ability should enter "ready" status.
     * <p>
     * This method should call {@link #startReady()} if the ready status should be enabled
     *
     * @param event The {@link Event} that needs to be parsed
     */
    public void handleReadyAttempt(Event event);

    /**
     * This method should only be called by {@link #handleReadyAttempt(Event)} which
     * handles parsing events to set the {@link us.eunoians.mcrpg.api.AbilityHolder} into
     * a ready state.
     */
    public default void startReady() {

        if (!isReady()) {

            LivingEntity livingEntity = getAbilityHolder().getEntity();

            if (livingEntity == null) {
                return;
            }

            setReady(true);

            if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;

                McRPG.getInstance().getMessageSender().sendMessage(player, ChatColor.YELLOW + "Readying...", false);
            }

            BukkitTask bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isReady()) {
                        setReady(false);

                        if (livingEntity instanceof Player) {
                            Player player = (Player) livingEntity;

                            McRPG.getInstance().getMessageSender().sendMessage(player, ChatColor.RED + "You are no longer ready...", false);
                        }
                    }
                }
            }.runTaskLater(McRPG.getInstance(), 5 * 20);//TODO configure

            readyTasks.put(livingEntity.getUniqueId(), bukkitTask);
        }
    }

    /**
     * Gets a {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     *
     * @return A {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     */
    public Set<Material> getActivatableMaterials();
}
