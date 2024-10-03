package us.eunoians.mcrpg.mock.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;
import java.util.Optional;

public class MockAbility extends BaseAbility {

    private NamespacedKey skillKey;

    public MockAbility(@NotNull Plugin plugin, @NotNull NamespacedKey abilityKey) {
        super(plugin, abilityKey);
    }

    public MockAbility(@NotNull Plugin plugin, @NotNull NamespacedKey abilityKey, @NotNull NamespacedKey skillKey) {
        super(plugin, abilityKey);
        this.skillKey = skillKey;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.ofNullable(skillKey);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("mocked");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Mocked Ability";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of("mocked");
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return null;
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }

    @Override
    public boolean isAbilityEnabled() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }
}
