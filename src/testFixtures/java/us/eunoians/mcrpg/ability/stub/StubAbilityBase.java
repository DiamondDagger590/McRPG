package us.eunoians.mcrpg.ability.stub;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal {@link Ability} base for unit test stubs. Subclasses implement
 * the interface mix-ins that match the ability type under test
 * (e.g., {@link us.eunoians.mcrpg.ability.impl.type.ActiveAbility},
 * {@link us.eunoians.mcrpg.ability.impl.type.PassiveAbility}).
 */
public abstract class StubAbilityBase implements Ability {

    private final NamespacedKey key;

    protected StubAbilityBase(@NotNull McRPG plugin, @NotNull String name) {
        this.key = new NamespacedKey(plugin, name);
    }

    @NotNull
    @Override
    public org.bukkit.plugin.Plugin getPlugin() {
        return McRPG.getInstance();
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

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
