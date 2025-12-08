package us.eunoians.mcrpg.ability.impl.herbalism;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.util.McRPGMethods;

public class VerdantSurge extends McRPGAbility implements ConfigurableActiveAbility, ConfigurableSkillAbility {

    public static final NamespacedKey VERDANT_SURGE_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "verdant_surge");

    public VerdantSurge(@NotNull McRPG mcRPG) {
        super(mcRPG, VERDANT_SURGE_KEY);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return VERDANT_SURGE_KEY;
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return null;
    }

    @Override
    public int getMaxTier() {
        return 0;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return null;
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return null;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return null;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "verdant_surge";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }
}
