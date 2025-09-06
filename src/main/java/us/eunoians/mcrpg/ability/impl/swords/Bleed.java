package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedComponents;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;

/**
 * Bleed is an ability that does a DOT for enemies whenever
 * an entity attacks with a sword.
 * <p>
 * This is designed to do armor piercing damage so that it can be viable even against opponents
 * with maxed out armor. As a trade-off, this will not damage an opponent past a certain
 * health threshold in order to remain balanced.
 * <p>
 * After an enemy is done bleeding, they are put on a short bleed immunity in order to
 * allow them a chance to regenerate health since constantly bleeding would cause fights to
 * easily swing in one direction.
 */
public final class Bleed extends McRPGAbility implements PassiveAbility, ConfigurableSkillAbility {

    public static final NamespacedKey BLEED_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bleed");

    public Bleed(@NotNull McRPG mcRPG) {
        super(mcRPG, BLEED_KEY);
        addActivatableComponent(SwordsComponents.HOLDING_SWORD_ACTIVATE_COMPONENT, EntityDamageByEntityEvent.class, 0);
        addActivatableComponent(BleedComponents.BLEED_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 1);
        addActivatableComponent(BleedComponents.BLEED_ON_TARGET_PLAYER_COMPONENT, EntityDamageByEntityEvent.class, 2);
        addActivatableComponent(new BleedComponents.BleedEligibleForTargetComponent(mcRPG), EntityDamageByEntityEvent.class, 3);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "bleed";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        //This is the only event that can activate this ability, so this should be a safe cast
        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
        LivingEntity livingEntity = (LivingEntity) entityDamageByEntityEvent.getEntity();
        YamlDocument swordsConfig = getYamlDocument();
        BleedActivateEvent bleedActivateEvent = new BleedActivateEvent(abilityHolder, livingEntity, swordsConfig.getInt(SwordsConfigFile.BLEED_BASE_CYCLES), swordsConfig.getDouble(SwordsConfigFile.BLEED_BASE_DAMAGE));
        Bukkit.getPluginManager().callEvent(bleedActivateEvent);

        if(!bleedActivateEvent.isCancelled()) {
            getPlugin().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.BLEED).startBleeding(abilityHolder, livingEntity, bleedActivateEvent.getBleedCycles(), bleedActivateEvent.getBleedDamage());
        }
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return SwordsConfigFile.BLEED_ENABLED;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.BLEED_DISPLAY_ITEM_HEADER;
    }

    public double getActivationChance(@NotNull AbilityHolder abilityHolder) {
        if (abilityHolder instanceof SkillHolder skillHolder) {
            var skillHolderDataOptional = skillHolder.getSkillHolderData(Swords.SWORDS_KEY);
            if (skillHolderDataOptional.isPresent()) {
                Parser parser = new Parser(getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG).getString(SwordsConfigFile.BLEED_ACTIVATION_EQUATION));
                parser.setVariable("swords_level", skillHolderDataOptional.get().getCurrentLevel());
                return parser.getValue();
            }
        }
        return 0.0;
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(AbilityItemPlaceholderKeys.ACTIVATION_CHANCE.getKey(),
                McRPGMethods.getChanceNumberFormat().format(getActivationChance(player.asSkillHolder())));
        return placeholders;
    }
}
