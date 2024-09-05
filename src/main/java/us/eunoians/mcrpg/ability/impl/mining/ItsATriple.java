package us.eunoians.mcrpg.ability.impl.mining;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.api.event.ability.mining.ExtraOreActivateEvent;
import us.eunoians.mcrpg.api.event.ability.mining.ItsATripleActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is a passive ability that has a chance to activate when {@link ExtraOre} activates,
 * turning the double drop into a triple drop.
 */
public final class ItsATriple extends BaseAbility implements PassiveAbility, ConfigurableTierableAbility {

    public static final NamespacedKey ITS_A_TRIPLE_KEY = new NamespacedKey(McRPG.getInstance(), "its_a_triple");

    public ItsATriple() {
        super(ITS_A_TRIPLE_KEY);
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
    public Optional<String> getDatabaseName() {
        return Optional.of("its_a_triple");
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Has a chance to change doubled drops from Extra Ore to triple drops",
                "<gray>Activation Chance: <gold>" + getActivationChance(currentTier));
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "It's A Triple";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND, 3);
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
        return McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG);
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
