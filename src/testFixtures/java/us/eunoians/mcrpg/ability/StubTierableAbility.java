package us.eunoians.mcrpg.ability;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal {@link TierableAbility} implementation for unit tests.
 * <p>
 * All boilerplate methods return sensible defaults derived from the ability key.
 * Use the {@code with*()} builder methods to customise the axes that vary across tests:
 * {@link #withMaxTier(int)}, {@link #withCurrentTier(int)}, and
 * {@link #withUpgradeQuestKey(NamespacedKey)}.
 */
public class StubTierableAbility implements TierableAbility {

    private final Plugin plugin;
    private final NamespacedKey key;
    private int maxTier = 5;
    private int currentTier = 1;
    private Optional<NamespacedKey> upgradeQuestKey = Optional.empty();

    public StubTierableAbility(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        this.plugin = plugin;
        this.key = key;
    }

    /**
     * Sets the maximum tier returned by {@link #getMaxTier()}.
     *
     * @param maxTier the max tier
     * @return this instance for chaining
     */
    @NotNull
    public StubTierableAbility withMaxTier(int maxTier) {
        this.maxTier = maxTier;
        return this;
    }

    /**
     * Sets the current tier returned by {@link #getCurrentAbilityTier(AbilityHolder)}.
     *
     * @param currentTier the current tier
     * @return this instance for chaining
     */
    @NotNull
    public StubTierableAbility withCurrentTier(int currentTier) {
        this.currentTier = currentTier;
        return this;
    }

    /**
     * Sets the upgrade quest key returned by {@link #getUpgradeQuestKey(int)}.
     *
     * @param questKey the quest definition key, or {@code null} for {@link Optional#empty()}
     * @return this instance for chaining
     */
    @NotNull
    public StubTierableAbility withUpgradeQuestKey(@NotNull NamespacedKey questKey) {
        this.upgradeQuestKey = Optional.of(questKey);
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

    @Override
    public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
        return currentTier;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getUpgradeQuestKey(int tier) {
        return upgradeQuestKey;
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
