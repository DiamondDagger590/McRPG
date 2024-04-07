package us.eunoians.mcrpg.mock.ability;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Optional;

public class MockAbility extends Ability {

    private NamespacedKey skillKey;

    public MockAbility(@NotNull NamespacedKey abilityKey) {
        super(abilityKey);
    }

    public MockAbility(@NotNull NamespacedKey abilityKey, @NotNull NamespacedKey skillKey) {
        super(abilityKey);
        this.skillKey = skillKey;
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.ofNullable(skillKey);
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("mocked");
    }

    @Override
    public String getDisplayName() {
        return "Mocked Ability";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return null;
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }
}
