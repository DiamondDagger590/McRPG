package us.eunoians.mcrpg.ability;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal {@link UnlockableAbility} + {@link PassiveAbility} implementation for unit tests.
 */
public class StubUnlockablePassiveAbility implements UnlockableAbility, PassiveAbility {

    private final Plugin plugin;
    private final NamespacedKey key;

    public StubUnlockablePassiveAbility(@NotNull Plugin plugin, @NotNull NamespacedKey key) {
        this.plugin = plugin;
        this.key = key;
    }

    @Override
    public int getUnlockLevel() {
        return 1;
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
        return UnlockableAbility.super.getApplicableAttributes();
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

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
