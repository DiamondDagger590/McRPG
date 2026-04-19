package us.eunoians.mcrpg.stat;

import com.diamonddagger590.mccore.registry.manager.Manager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;

/**
 * Manager responsible for the {@link CombatStatRegistry} and {@link PlayerCombatData}
 * construction. Registered via
 * {@link us.eunoians.mcrpg.registry.manager.McRPGManagerKey#STAT} and accessed through
 * {@code registryAccess()}.
 * <p>
 * The registry is populated during construction (before any players join). Per-player
 * {@link PlayerCombatData} instances live on {@link us.eunoians.mcrpg.entity.player.McRPGPlayer}
 * and are built via {@link #createPlayerCombatData(YamlDocument)} when the player
 * object is constructed.
 */
public class StatManager extends Manager<McRPG> {

    private final CombatStatRegistry registry;

    /**
     * @param plugin The McRPG plugin instance.
     */
    public StatManager(@NotNull McRPG plugin) {
        super(plugin);
        this.registry = new CombatStatRegistry();
        McRPGCombatStat.registerAll(registry);
    }

    /**
     * Gets the combat stat registry containing all registered stat definitions.
     *
     * @return The combat stat registry.
     */
    @NotNull
    public CombatStatRegistry getRegistry() {
        return registry;
    }

    /**
     * Creates and initializes a new {@link PlayerCombatData} from the registry definitions,
     * then applies config-driven overrides for base values and regen rates.
     *
     * @param comboConfig The combo configuration YAML document to read base values from.
     * @return A fully initialized {@link PlayerCombatData}.
     */
    @NotNull
    public PlayerCombatData createPlayerCombatData(@NotNull YamlDocument comboConfig) {
        PlayerCombatData data = new PlayerCombatData();
        data.initFromRegistry(registry);

        data.getInstance(McRPGCombatStat.HEALTH_KEY).ifPresent(instance -> {
            double baseMax = comboConfig.getDouble(ComboConfigFile.HEALTH_BASE_MAX, instance.getBaseValue());
            instance.setBaseValue(baseMax);
            instance.setCurrent(baseMax);
        });

        data.getInstance(McRPGCombatStat.MANA_KEY).ifPresent(instance -> {
            double baseMax = comboConfig.getDouble(ComboConfigFile.MANA_BASE_MAX, instance.getBaseValue());
            double regen = comboConfig.getDouble(ComboConfigFile.MANA_REGEN_PER_SECOND, instance.getRegenPerSecond());
            instance.setBaseValue(baseMax);
            instance.setCurrent(baseMax);
            instance.setRegenPerSecond(regen);
        });

        return data;
    }
}
