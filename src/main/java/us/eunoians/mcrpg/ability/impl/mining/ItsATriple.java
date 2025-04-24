package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKeys;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.mining.ExtraOreActivateEvent;
import us.eunoians.mcrpg.event.ability.mining.ItsATripleActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;

/**
 * This is a passive ability that has a chance to activate when {@link ExtraOre} activates,
 * turning the double drop into a triple drop.
 */
public final class ItsATriple extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility {

    public static final NamespacedKey ITS_A_TRIPLE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "its_a_triple");

    public ItsATriple(@NotNull McRPG mcRPG) {
        super(mcRPG, ITS_A_TRIPLE_KEY);
        addActivatableComponent(ItsATripleComponents.ITS_A_TRIPLE_ACTIVATE_ON_EXTRA_DROP_COMPONENT, ExtraOreActivateEvent.class, 0);
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return ITS_A_TRIPLE_KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "its_a_triple";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        ExtraOreActivateEvent extraOreActivateEvent = (ExtraOreActivateEvent) event;
        ItsATripleActivateEvent itsATripleActivateEvent = new ItsATripleActivateEvent(abilityHolder);
        Bukkit.getPluginManager().callEvent(itsATripleActivateEvent);
        if (!itsATripleActivateEvent.isCancelled()) {
            extraOreActivateEvent.setDropMultiplier(3);
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(MiningConfigFile.ITS_A_TRIPLE_ENABLED);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(MiningConfigFile.ITS_A_TRIPLE_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKeys.ITS_A_TRIPLE_DISPLAY_ITEM_HEADER;
    }

    public double getActivationChance(int tier) {
        return getYamlDocument().getDouble(Route.addTo(getRouteForTier(tier), "activation-chance"));
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableTierableAbility.super.getApplicableAttributes();
    }
}
