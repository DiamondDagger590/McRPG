package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

public class RampingFrenzy extends McRPGAbility implements PassiveAbility, ConfigurableSkillAbility {
    
    public static final NamespacedKey RAMPING_FRENZY_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "ramping_frenzy");
    
    public RampingFrenzy(@NotNull McRPG mcRPG) {
        super(mcRPG, RAMPING_FRENZY_KEY);
    }

    @Override
    public @NotNull McRPG getPlugin() {
        return super.getPlugin();
    }

    @Override
    public @NotNull NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @Override
    public @NotNull Route getDisplayItemRoute() {
        return null;
    }

    @Override
    public @NotNull Route getAbilityEnabledRoute() {
        return null;
    }

    @Override
    public @NotNull String getDatabaseName() {
        return "ramping_frenzy";
    }

    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        /*
        How do stacks work?
        How do they fall off?
        1-5 stacks = 5 seconds of haste I
        6-8 stacks = 5 seconds of haste II
        9-11 stacks = 5 seconds of haste III

        fall off? does it go back down the lowest stack until you reset it? how do you shed stacks quickly?

        you get 0.75 seconds of haste whenever it falls off.
        Combat tracker manages the state so you dont need to use PDC? Optimizes the scan of online players to only be players
        in combat.

        do we count pve as in combat? can you combat log when fighting mobs?
         */
        /*
        - attach stack count as pdc
        - attach stack gained time
        - attach stack expire time
        - when player attacks check if they have stack
        - if so, then check gain time
        - if stacks havent fallen off (time is configurable), then add one
        - if they have fallen off, check how many would fall off (fall off time is configurable)
         */
        return false;
    }

    public boolean hasRampingFrenzyStacks() {

    }
}
