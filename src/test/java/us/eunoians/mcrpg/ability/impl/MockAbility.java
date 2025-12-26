package us.eunoians.mcrpg.ability.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * A mock instance of an ability used for unit testing
 * abilities abstractly without caring about a specific implementation
 * of an ability.
 */
public class MockAbility extends BaseAbility {

    public MockAbility(@NotNull McRPG plugin) {
        super(plugin, new NamespacedKey(plugin, "mock-ability"));
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "mock-ability";
    }

    @NotNull
    @Override
    public String getName(@NotNull McRPGPlayer player) {
        return "Mock Ability";
    }

    @NotNull
    @Override
    public String getName() {
        return "Mock Ability";
    }

    @NotNull
    @Override
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return Component.text("Mock Ability");
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.text("Mock Ability");
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        // No-op for testing
    }

    @Override
    public boolean isAbilityEnabled() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @NotNull
    @Override
    public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        return mock(AbilityItemBuilder.class);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
