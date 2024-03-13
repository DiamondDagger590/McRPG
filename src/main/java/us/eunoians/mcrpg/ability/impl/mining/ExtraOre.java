package us.eunoians.mcrpg.ability.impl.mining;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Optional;

public class ExtraOre extends Ability {

    public static final NamespacedKey EXTRA_ORE_KEY = new NamespacedKey(McRPG.getInstance(), "extra_ore");

    public ExtraOre() {
        super(EXTRA_ORE_KEY);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return "Extra Ore";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND, 2);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }
}
