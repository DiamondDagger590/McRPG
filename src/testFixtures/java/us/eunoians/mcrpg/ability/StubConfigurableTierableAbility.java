package us.eunoians.mcrpg.ability;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal {@link ConfigurableTierableAbility} implementation for unit tests.
 * <p>
 * Delegates {@link #getUpgradeQuestKey(int)} to the interface default so that YAML-based
 * upgrade-quest resolution (tier-specific key, all-tiers fallback, inference) works in tests.
 * Methods that would otherwise call into the live registry are overridden with simple stubs.
 * <p>
 * Use {@link #withMaxTier(int)} to change the max tier (default&nbsp;5).
 */
public class StubConfigurableTierableAbility implements ConfigurableTierableAbility {

    private final Plugin plugin;
    private final NamespacedKey key;
    private final YamlDocument doc;
    private int maxTier = 5;

    public StubConfigurableTierableAbility(@NotNull Plugin plugin,
                                           @NotNull NamespacedKey key,
                                           @NotNull YamlDocument doc) {
        this.plugin = plugin;
        this.key = key;
        this.doc = doc;
    }

    /**
     * Sets the maximum tier returned by {@link #getMaxTier()}.
     *
     * @param maxTier the max tier
     * @return this instance for chaining
     */
    @NotNull
    public StubConfigurableTierableAbility withMaxTier(int maxTier) {
        this.maxTier = maxTier;
        return this;
    }

    @Override
    public int getMaxTier() {
        return maxTier;
    }

    @Override
    public int getUnlockLevelForTier(int tier) {
        return 1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getUpgradeCostForTier(int tier) {
        return 0;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return doc;
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return Route.fromString("dummy");
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return Route.fromString("dummy.enabled");
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return Route.fromString("ability.tier-configuration");
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return key;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of();
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return key.getKey();
    }

    @NotNull
    @Override
    public String getName(@NotNull McRPGPlayer player) {
        return key.getKey();
    }

    @NotNull
    @Override
    public String getName() {
        return key.getKey();
    }

    @NotNull
    @Override
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return Component.text(key.getKey());
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.text(key.getKey());
    }

    @NotNull
    @Override
    public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return true;
    }

    @Override
    public boolean isAbilityEnabled() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return true;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
