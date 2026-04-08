package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnEvent;

import java.util.UUID;

/**
 * Bridges MythicMobs events into McRPG's fishing mob event system.
 * <p>
 * This listener is only registered when the MythicMobs plugin is present on the server.
 * It performs three functions:
 * <ol>
 *   <li>Registers the {@code mcrpg_skillbook} custom drop type via {@link MythicDropLoadEvent}</li>
 *   <li>Registers the {@code mcrpg_ability} custom mechanic via {@link MythicMechanicLoadEvent}</li>
 *   <li>Registers the {@code mcrpg_ability_unlocked} custom condition via {@link MythicConditionLoadEvent}</li>
 *   <li>Fires {@link FishingMobSpawnEvent} when a MythicMob tagged as a fishing mob spawns</li>
 *   <li>Fires {@link FishingMobDeathEvent} when a MythicMob tagged as a fishing mob dies</li>
 * </ol>
 * <p>
 * A MythicMob is considered a "fishing mob" if it has the
 * {@link FishingMobKeys#FISHING_MOB_KEY} persistent data container key set to {@code true},
 * which is applied by the fishing skill when it spawns the mob via MythicMobs API.
 *
 * @see FishingMobKeys
 */
public class MythicMobsListener implements Listener {

    /**
     * Registers the {@code mcrpg_skillbook} custom drop type when MythicMobs loads its drop table.
     *
     * @param event The drop load event fired by MythicMobs
     */
    @EventHandler
    public void onMythicDropLoad(@NotNull MythicDropLoadEvent event) {
        if (event.getDropName().equalsIgnoreCase("mcrpg_skillbook")) {
            event.register(new McRPGSkillBookDrop(event.getConfig(), event.getArgument()));
        }
    }

    /**
     * Registers the {@code mcrpg_ability} custom mechanic when MythicMobs loads its mechanics.
     * This mechanic delegates ability execution from MythicMobs AI to McRPG's ability system.
     *
     * @param event The mechanic load event fired by MythicMobs
     */
    @EventHandler
    public void onMythicMechanicLoad(@NotNull MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("mcrpg_ability")) {
            event.register(new McRPGAbilityMechanic(event.getConfig()));
        }
    }

    /**
     * Registers the {@code mcrpg_ability_unlocked} custom condition when MythicMobs loads
     * its conditions. This condition checks whether the killing player has unlocked a
     * specific McRPG ability, used in DropTable TriggerConditions for unlock-aware drop rates.
     *
     * @param event The condition load event fired by MythicMobs
     */
    @EventHandler
    public void onMythicConditionLoad(@NotNull MythicConditionLoadEvent event) {
        if (event.getConditionName().equalsIgnoreCase("mcrpg_ability_unlocked")) {
            event.register(new McRPGAbilityUnlockedCondition(event.getConfig()));
        }
    }

    /**
     * Listens for MythicMob spawns and fires a {@link FishingMobSpawnEvent} if the mob
     * is tagged as a fishing mob.
     *
     * @param event The MythicMob spawn event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobSpawn(@NotNull MythicMobSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN)) {
            return;
        }

        String anglerUuidString = entity.getPersistentDataContainer().get(FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING);
        if (anglerUuidString == null) {
            return;
        }

        Player angler = Bukkit.getPlayer(UUID.fromString(anglerUuidString));
        if (angler == null) {
            return;
        }

        String mobType = event.getMobType().getInternalName();
        FishingMobSpawnEvent fishingEvent = new FishingMobSpawnEvent(angler, entity, mobType, entity.getLocation());
        Bukkit.getPluginManager().callEvent(fishingEvent);

        if (fishingEvent.isCancelled()) {
            entity.remove();
        }
    }

    /**
     * Listens for MythicMob deaths and fires a {@link FishingMobDeathEvent} if the mob
     * is tagged as a fishing mob.
     *
     * @param event The MythicMob death event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(@NotNull MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN)) {
            return;
        }

        Player killer = null;
        if (event.getKiller() instanceof Player player) {
            killer = player;
        }

        String mobType = event.getMobType().getInternalName();
        FishingMobDeathEvent fishingEvent = new FishingMobDeathEvent(entity, killer, mobType);
        Bukkit.getPluginManager().callEvent(fishingEvent);
    }
}
