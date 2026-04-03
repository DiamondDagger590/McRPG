package us.eunoians.mcrpg.listener.fishing;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.FishingMobSpawnConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnChanceUpdateEvent;
import us.eunoians.mcrpg.external.mythicmobs.FishingMobKeys;
import us.eunoians.mcrpg.external.mythicmobs.MythicMobsHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;
import us.eunoians.mcrpg.fishing.MobPoolEntry;
import us.eunoians.mcrpg.fishing.MobPoolSelector;
import us.eunoians.mcrpg.fishing.PlayerFishingState;
import us.eunoians.mcrpg.fishing.ReloadableMobPool;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listens to fishing events and triggers mob spawns via MythicMobs when the
 * player's accumulated spawn chance succeeds.
 * <p>
 * Per-player state is stored on {@link McRPGPlayer#getFishingState()}.
 * The mob pool is loaded via {@link ReloadableMobPool} and refreshes on
 * {@code /mcrpg admin reload}.
 * <p>
 * This listener is only registered when MythicMobs is present and the
 * fishing mob spawn system is enabled in config.
 */
public class FishingMobSpawnListener implements Listener {

    private final McRPG plugin;
    private final ReloadableMobPool reloadableMobPool;

    /**
     * Creates a new fishing mob spawn listener.
     *
     * @param plugin           the McRPG plugin instance
     * @param reloadableMobPool the reloadable mob pool configuration
     */
    public FishingMobSpawnListener(@NotNull McRPG plugin, @NotNull ReloadableMobPool reloadableMobPool) {
        this.plugin = plugin;
        this.reloadableMobPool = reloadableMobPool;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(@NotNull PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                && event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getHook() == null) {
            return;
        }

        Optional<McRPGPlayer> mcRPGPlayerOpt = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        Location hookLocation = event.getHook().getLocation();

        PlayerFishingState state = mcRPGPlayer.getOrCreateFishingState(getBaseChance());

        // Check active mob cap
        int maxActiveMobs = getConfig().getInt(FishingMobSpawnConfigFile.MAX_ACTIVE_MOBS_PER_PLAYER, 1);
        if (state.getActiveMobCount() >= maxActiveMobs) {
            return;
        }

        // Update chance based on proximity to last hook
        updateSpawnChance(player, state, hookLocation);
        state.setLastHookLocation(hookLocation);

        // Roll for spawn
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < state.getCurrentSpawnChance()) {
            attemptSpawnMob(player, state, hookLocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishingMobDeath(@NotNull FishingMobDeathEvent event) {
        UUID mobUUID = event.getMob().getUniqueId();
        double postKillChance = getConfig().getDouble(FishingMobSpawnConfigFile.POST_KILL_CHANCE, 0.0);

        // Find the player who owns this mob via the angler UUID PDC tag
        String anglerUuidString = event.getMob().getPersistentDataContainer()
                .get(FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING);

        if (anglerUuidString == null) {
            return;
        }

        UUID anglerUUID = UUID.fromString(anglerUuidString);
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(anglerUUID)
                .ifPresent(mcRPGPlayer -> mcRPGPlayer.getFishingState().ifPresent(state -> {
                    if (state.removeActiveMob(mobUUID)) {
                        state.setCurrentSpawnChance(postKillChance);
                    }
                }));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getPlayer().getUniqueId())
                .ifPresent(mcRPGPlayer -> mcRPGPlayer.getFishingState().ifPresent(state -> {
                    boolean resetOnWorldChange = getConfig().getBoolean(
                            FishingMobSpawnConfigFile.RESET_ON_WORLD_CHANGE, true);
                    if (resetOnWorldChange) {
                        state.setCurrentSpawnChance(getBaseChance());
                    }
                    state.clearLastHookLocation();
                }));
    }

    private void updateSpawnChance(@NotNull Player player,
                                    @NotNull PlayerFishingState state,
                                    @NotNull Location hookLocation) {
        double oldChance = state.getCurrentSpawnChance();
        double newChance;

        Optional<Location> lastHookOpt = state.getLastHookLocation();
        double sameAreaRange = getConfig().getDouble(FishingMobSpawnConfigFile.SAME_AREA_RANGE, 10.0);

        if (lastHookOpt.isEmpty() || !lastHookOpt.get().getWorld().equals(hookLocation.getWorld())) {
            newChance = oldChance;
        } else {
            double distance = lastHookOpt.get().distance(hookLocation);
            if (distance <= sameAreaRange) {
                double increment = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_INCREMENT_PER_CATCH, 0.02);
                double maxChance = getConfig().getDouble(FishingMobSpawnConfigFile.MAX_CHANCE, 0.35);
                newChance = Math.min(oldChance + increment, maxChance);
            } else {
                double decrement = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_DECREMENT_PER_CATCH, 0.05);
                newChance = Math.max(oldChance - decrement, getBaseChance());
            }
        }

        FishingMobSpawnChanceUpdateEvent updateEvent = new FishingMobSpawnChanceUpdateEvent(
                player, oldChance, newChance, hookLocation);
        Bukkit.getPluginManager().callEvent(updateEvent);

        if (!updateEvent.isCancelled()) {
            state.setCurrentSpawnChance(updateEvent.getNewChance());
        }
    }

    private void attemptSpawnMob(@NotNull Player player,
                                  @NotNull PlayerFishingState state,
                                  @NotNull Location hookLocation) {
        MobPoolSelector selector = reloadableMobPool.getContent();

        // Resolve WorldGuard hook (nullable — region checks are skipped if absent)
        WorldGuardHook worldGuardHook = plugin.registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.WORLDGUARD)
                .orElse(null);

        Optional<MobPoolEntry> selected = selector.select(
                state.getCurrentSpawnChance(), hookLocation, worldGuardHook);

        if (selected.isEmpty()) {
            return;
        }

        MobPoolEntry entry = selected.get();
        Location spawnLocation = calculateSpawnLocation(hookLocation);

        // Spawn via MythicMobsHook
        MythicMobsHook mmHook = plugin.registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.MYTHIC_MOBS)
                .orElse(null);

        if (mmHook == null) {
            return;
        }

        Optional<Entity> entityOpt = mmHook.spawnMob(entry.mythicMobsId(), spawnLocation, entry.mobLevel());
        if (entityOpt.isEmpty()) {
            return;
        }

        Entity entity = entityOpt.get();

        // Tag the entity with PDC keys so MythicMobsListener can identify it
        entity.getPersistentDataContainer().set(
                FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN, true);
        entity.getPersistentDataContainer().set(
                FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING, player.getUniqueId().toString());

        state.addActiveMob(entity.getUniqueId());
        state.setCurrentSpawnChance(getBaseChance());

        plugin.getLogger().fine("Spawned fishing mob '" + entry.mythicMobsId()
                + "' for player " + player.getName() + " at " + spawnLocation);
    }

    @NotNull
    private Location calculateSpawnLocation(@NotNull Location hookLocation) {
        double offset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_OFFSET_FROM_HOOK, 3.0);
        double yOffset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_Y_OFFSET, 1.0);

        double angle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
        double x = hookLocation.getX() + offset * Math.cos(angle);
        double z = hookLocation.getZ() + offset * Math.sin(angle);
        double y = hookLocation.getY() + yOffset;

        return new Location(hookLocation.getWorld(), x, y, z);
    }

    private double getBaseChance() {
        return getConfig().getDouble(FishingMobSpawnConfigFile.BASE_CHANCE, 0.0);
    }

    @NotNull
    private YamlDocument getConfig() {
        return plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.FISHING_MOB_SPAWN_CONFIG);
    }
}
