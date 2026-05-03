package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.UUID;

/**
 * Bridges MythicMobs events into McRPG's event and entity systems.
 * <p>
 * This listener is only registered when the MythicMobs plugin is present on the server.
 * It performs the following:
 * <ol>
 *   <li>Registers the {@code mcrpg_skillbook} custom drop type via {@link MythicDropLoadEvent}</li>
 *   <li>Registers the {@code mcrpg_ability} custom mechanic via {@link MythicMechanicLoadEvent}</li>
 *   <li>Registers the {@code mcrpg_ability_unlocked} custom condition via {@link MythicConditionLoadEvent}</li>
 *   <li>Creates and tracks an {@link AbilityHolder} for each MythicMob at spawn time,
 *       eagerly populated with all {@code mcrpg_ability} mechanics found in the mob's skill tree
 *       via {@link MythicMobAbilityParser}</li>
 *   <li>Removes tracked {@link AbilityHolder} instances on mob death and despawn</li>
 *   <li>Clears the {@link MythicMobAbilityParser} cache on MythicMobs reload</li>
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

    private final MythicMobAbilityParser abilityParser;

    /**
     * Creates a new listener, instantiating the {@link MythicMobAbilityParser} it will use
     * to eagerly populate {@link AbilityHolder}s at spawn time. The parser pulls its cache
     * TTL from {@link FileType#MAIN_CONFIG} directly so callers can't hand it a foreign
     * {@link dev.dejvokep.boostedyaml.YamlDocument}, and the TTL reloadable is registered
     * with the {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}
     * here as well.
     */
    public MythicMobsListener() {
        this.abilityParser = new MythicMobAbilityParser(
                McRPG.getInstance().registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.FILE)
                        .getFile(FileType.MAIN_CONFIG));
        McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(abilityParser.getCacheTtlReloadable());
    }

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
     * Creates and tracks an {@link AbilityHolder} for each MythicMob that spawns,
     * eagerly populated with all {@code mcrpg_ability} mechanics found in the mob's
     * skill tree via {@link MythicMobAbilityParser}.
     * <p>
     * This ensures that any system querying the {@link EntityManager} for a mob's abilities
     * gets a complete picture immediately, without waiting for each ability to fire once.
     * The holder is removed on death ({@link #onMythicMobDeathCleanup}) or despawn
     * ({@link #onMythicMobDespawnCleanup}).
     *
     * @param event The MythicMob spawn event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobSpawnTrackHolder(@NotNull MythicMobSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        EntityManager entityManager = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY);
        if (entityManager.isAbilityHolderTracked(entity.getUniqueId())) {
            return;
        }

        AbilityHolder holder = new AbilityHolder(McRPG.getInstance(), entity.getUniqueId());
        AbilityRegistry abilityRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        List<MythicMobAbilityParser.ParsedAbilityInfo> parsedAbilities =
                abilityParser.parseAbilities(event.getMobType());

        for (MythicMobAbilityParser.ParsedAbilityInfo info : parsedAbilities) {
            NamespacedKey abilityKey = info.abilityKey();
            if (!abilityRegistry.registered(abilityKey)) {
                continue;
            }
            holder.addAvailableAbility(abilityKey);
            AbilityData abilityData = new AbilityData(abilityKey, info.attributes());
            holder.addAbilityData(abilityData);
        }

        entityManager.trackAbilityHolder(holder);
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
     * Removes the tracked {@link AbilityHolder} for a MythicMob on death.
     *
     * @param event The MythicMob death event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMythicMobDeathCleanup(@NotNull MythicMobDeathEvent event) {
        McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .<EntityManager>manager(McRPGManagerKey.ENTITY)
                .removeAbilityHolder(event.getEntity().getUniqueId());
    }

    /**
     * Removes the tracked {@link AbilityHolder} for a MythicMob on despawn
     * (e.g., max lifetime, combat dropout, chunk unload).
     *
     * @param event The MythicMob despawn event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMythicMobDespawnCleanup(@NotNull MythicMobDespawnEvent event) {
        McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .<EntityManager>manager(McRPGManagerKey.ENTITY)
                .removeAbilityHolder(event.getEntity().getUniqueId());
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

    /**
     * Clears the {@link MythicMobAbilityParser} cache when MythicMobs reloads its configuration.
     * This ensures that any skill tree changes made by server owners are picked up on the next
     * mob spawn.
     *
     * @param event The MythicMobs reload event
     */
    @EventHandler
    public void onMythicMobsReload(@NotNull MythicReloadedEvent event) {
        abilityParser.clearCache();
    }
}
