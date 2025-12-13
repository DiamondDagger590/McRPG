package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.util.McRPGMethods;

public class InstantIrrigation extends McRPGAbility implements PassiveAbility, ConfigurableSkillAbility, CooldownableAbility {

    public static final NamespacedKey INSTANT_IRRIGATION_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "instant_irrigation");

    public InstantIrrigation(@NotNull McRPG mcRPG) {
        super(mcRPG, INSTANT_IRRIGATION_KEY);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Herbalism.HERBALISM_KEY;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.HERBALISM_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return null;
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return HerbalismConfigFile.INSTANT_IRRIGATION_ENABLED;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "instant_irrigation";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        YamlDocument yamlDocument = getYamlDocument();
        Parser parser = new Parser(yamlDocument.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN));
        if (abilityHolder instanceof SkillHolder skillHolder) {
            parser.setVariable("level", skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY).orElseThrow(IllegalStateException::new).getCurrentLevel());
        } else {
            parser.setVariable("level", 0);
        }
        return (long) parser.getValue();
    }
}
