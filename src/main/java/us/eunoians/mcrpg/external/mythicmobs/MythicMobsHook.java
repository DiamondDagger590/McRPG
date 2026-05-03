package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A hook for containing all code related to
 * <a href="https://mythiccraft.io/index.php?pages/official-mythicmobs/">MythicMobs</a>
 * that this plugin needs to support it.
 * <p>
 * MythicMobs is used by the fishing skill to spawn custom mobs when a player catches a fish.
 * McRPG registers a custom drop type ({@code mcrpg_skillbook}) via {@link MythicMobsListener}
 * and bridges MythicMobs spawn/death events into McRPG's own event system.
 * <p>
 * This hook also owns the {@link MechanicAttributeExtractor} registry. Third-party plugins
 * register extractors here so that the {@code mcrpg_ability} mechanic can read custom
 * config parameters and convert them into {@link us.eunoians.mcrpg.ability.attribute.AbilityAttribute}s.
 */
public class MythicMobsHook extends PluginHook<McRPG> {

    private final Map<String, MechanicAttributeExtractor> attributeExtractors = new LinkedHashMap<>();

    public MythicMobsHook(@NotNull McRPG plugin) {
        super(plugin);
        registerBuiltInExtractors();
    }

    /**
     * Registers a {@link MechanicAttributeExtractor} for a named config parameter.
     * When the {@code mcrpg_ability} mechanic is constructed or parsed at spawn time,
     * every registered extractor is invoked against the line config. If the extractor
     * returns a non-empty result, the attribute is attached to the ability data.
     * <p>
     * The built-in {@code tier} extractor is registered at construction and cannot be
     * overridden. Attempting to register a duplicate parameter name will overwrite the
     * previous extractor.
     *
     * @param configParamName The config parameter name (e.g., {@code "charge_time"})
     * @param extractor       The extractor that reads from the {@link io.lumine.mythic.api.config.MythicLineConfig}
     */
    public void registerMechanicAttributeExtractor(@NotNull String configParamName,
                                                    @NotNull MechanicAttributeExtractor extractor) {
        attributeExtractors.put(configParamName, extractor);
    }

    /**
     * Returns an unmodifiable view of all registered attribute extractors, keyed by
     * their config parameter name.
     *
     * @return An unmodifiable map of config parameter names to extractors
     */
    @NotNull
    public Map<String, MechanicAttributeExtractor> getMechanicAttributeExtractors() {
        return Collections.unmodifiableMap(attributeExtractors);
    }

    /**
     * Spawns a MythicMobs mob at the given location.
     *
     * @param mythicMobsId the MM internal type ID
     * @param location     the Bukkit location to spawn at
     * @param mobLevel     the MM mob level
     * @return the spawned entity, or empty if the type ID is not registered in MM
     */
    @NotNull
    public Optional<Entity> spawnMob(@NotNull String mythicMobsId,
                                      @NotNull Location location,
                                      double mobLevel) {
        Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId);
        if (mythicMob.isEmpty()) {
            plugin().getLogger().warning("MythicMob type '" + mythicMobsId
                    + "' not found in MythicMobs registry.");
            return Optional.empty();
        }

        ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), mobLevel);
        return Optional.of(activeMob.getEntity().getBukkitEntity());
    }

    /**
     * Checks whether a MythicMobs mob type ID is registered.
     *
     * @param mythicMobsId the MM internal type ID
     * @return true if the type exists in the MM registry
     */
    public boolean isMobTypeRegistered(@NotNull String mythicMobsId) {
        return MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId).isPresent();
    }

    private void registerBuiltInExtractors() {
        registerMechanicAttributeExtractor("tier", config -> {
            int tier = config.getInteger("tier", 0);
            if (tier > 0) {
                return Optional.of(new AbilityTierAttribute(tier));
            }
            return Optional.empty();
        });
    }
}
