package us.eunoians.mcrpg.ability.impl.swords;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.VampireActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.util.Objects;
import java.util.Optional;

/**
 * This ability is an unlockable ability for {@link Swords} that
 * can heal the user of the {@link Bleed} ability each time a bleeding
 * entity takes a tick of bleed damage
 */
public class Vampire extends BaseAbility implements ConfigurableTierableAbility, PassiveAbility {

    public static final NamespacedKey VAMPIRE_KEY = new NamespacedKey(McRPG.getInstance(), "vampire");

    public Vampire() {
        super(VAMPIRE_KEY);
        addActivatableComponent(VampireComponents.VAMPIRE_ACTIVATE_COMPONENT, BleedActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Vampire");
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Vampire";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.GHAST_TEAR);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

        BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) event;
        VampireActivateEvent vampireActivateEvent = new VampireActivateEvent(abilityHolder, bleedActivateEvent.getBleedingEntity(), getAmountToHeal(getCurrentAbilityTier(abilityHolder)));
        Bukkit.getPluginManager().callEvent(vampireActivateEvent);

        if(!vampireActivateEvent.isCancelled()) {
            LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(abilityHolder.getUUID()); //We assert this in the vampire components
            assert livingEntity != null;
            livingEntity.setHealth(Math.min(Objects.requireNonNull(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(),
                    livingEntity.getHealth() + vampireActivateEvent.getAmountToHeal()));
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.VAMPIRE_ENABLED);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.VAMPIRE_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.SWORDS_CONFIG);
    }

    public double getActivationChance(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "vampire-activation-chance");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "vampire-activation-chance");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getDouble(tierRoute);
        }
        else {
            return swordsConfig.getDouble(allTiersRoute);
        }
    }

    public int getAmountToHeal(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "amount-to-heal");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "amount-to-heal");
        if (swordsConfig.contains(tierRoute)) {
            return swordsConfig.getInt(tierRoute);
        }
        else {
            return swordsConfig.getInt(allTiersRoute);
        }
    }
}
