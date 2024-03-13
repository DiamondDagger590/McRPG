package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.DeeperWoundActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Optional;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can increase the duration of the {@link Bleed} ability
 */
public class DeeperWound extends TierableAbility {

    public static final NamespacedKey DEEPER_WOUND_KEY = new NamespacedKey(McRPG.getInstance(), "deeper_wound");

    public DeeperWound() {
        super(DEEPER_WOUND_KEY);
        addActivatableComponent(DeeperWoundComponents.DEEPER_WOUND_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Deeper Wound");
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return "Deeper Wound";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.RED_DYE);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;

        DeeperWoundActivateEvent deeperWoundActivateEvent = new DeeperWoundActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), 2);
        Bukkit.getPluginManager().callEvent(deeperWoundActivateEvent);

        if (!deeperWoundActivateEvent.isCancelled()) {
            bleedActivateEvent.setBleedCycles(bleedActivateEvent.getBleedCycles() + deeperWoundActivateEvent.getAdditionalBleedCycles());
        }
    }

    @Override
    public int getMaxTier() {
        return 5;
    }

    @Override
    public int getUnlockLevelForTier(int tier) {
        return 10;
    }

    @Override
    public int getUpgradeCostForTier(int tier) {
        return 1;
    }

    @Override
    public int getUnlockLevel() {
        return 1;
    }
}
