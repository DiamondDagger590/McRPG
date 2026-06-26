package us.eunoians.mcrpg.ability;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal {@link SkillAbility} for unit testing ability-skill associations.
 */
class StubSkillAbility implements SkillAbility {

    private final Plugin plugin;
    private final NamespacedKey abilityKey;
    private final NamespacedKey skillKey;

    StubSkillAbility(@NotNull Plugin plugin, @NotNull NamespacedKey abilityKey, @NotNull NamespacedKey skillKey) {
        this.plugin = plugin;
        this.abilityKey = abilityKey;
        this.skillKey = skillKey;
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return skillKey;
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return Set.of();
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return abilityKey.getKey();
    }

    @NotNull
    @Override
    public String getName(@NotNull McRPGPlayer player) {
        return abilityKey.getKey();
    }

    @NotNull
    @Override
    public String getName() {
        return abilityKey.getKey();
    }

    @NotNull
    @Override
    public Component getDisplayName(@NotNull McRPGPlayer player) {
        return Component.text(abilityKey.getKey());
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.text(abilityKey.getKey());
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
    public boolean isPassive() {
        return true;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }
}
