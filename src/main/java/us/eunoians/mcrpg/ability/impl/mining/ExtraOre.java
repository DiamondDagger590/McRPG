package us.eunoians.mcrpg.ability.impl.mining;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Optional;

public final class ExtraOre extends BaseAbility {

    public static final NamespacedKey EXTRA_ORE_KEY = new NamespacedKey(McRPG.getInstance(), "extra_ore");

    public ExtraOre() {
        super(EXTRA_ORE_KEY);
        addActivatableComponent(MiningComponents.HOLDING_PICKAXE_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        addActivatableComponent(ExtraOreComponents.EXTRA_ORE_ON_BREAK_COMPONENT, BlockBreakEvent.class, 1);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("extra_ore");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Extra Ore";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND, 2);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }

    @Override
    public boolean isAbilityEnabled() {
        return false;
    }

    @Override
    public boolean isPassive() {
        return false;
    }
}
