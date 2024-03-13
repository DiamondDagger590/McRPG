package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.VampireActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Objects;
import java.util.Optional;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can heal the user of the {@link Bleed} ability each time a bleeding
 * entity takes a tick of bleed damage
 */
public class Vampire extends TierableAbility {

    public static final NamespacedKey VAMPIRE_KEY = new NamespacedKey(McRPG.getInstance(), "vampire");

    public Vampire() {
        super(VAMPIRE_KEY);
        addActivatableComponent(VampireComponents.VAMPIRE_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Vampire");
    }

    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @Override
    public String getDisplayName() {
        return "Vampire";
    }

    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.GHAST_TEAR);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;
        VampireActivateEvent vampireActivateEvent = new VampireActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), 1);
        Bukkit.getPluginManager().callEvent(vampireActivateEvent);

        if(!vampireActivateEvent.isCancelled()) {
            LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(abilityHolder.getUUID()); //We assert this in the vampire components
            assert livingEntity != null;
            livingEntity.setHealth(Math.min(Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(),
                    livingEntity.getHealth() + vampireActivateEvent.getAmountToHeal()));
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
        return 3;
    }
}
