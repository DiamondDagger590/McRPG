package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedComponents;
import us.eunoians.mcrpg.event.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Optional;

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
public final class Bleed extends McRPGAbility implements PassiveAbility, ConfigurableAbility {

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
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Swords.SWORDS_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getLegacyName() {
        return Optional.of("Bleed");
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("bleed");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Bleed";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        ItemStack guiItem = new ItemStack(Material.REDSTONE);
        return guiItem;
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
            getPlugin().getBleedManager().startBleeding(abilityHolder, livingEntity, bleedActivateEvent.getBleedCycles(), bleedActivateEvent.getBleedDamage());
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(SwordsConfigFile.BLEED_ENABLED);
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG);
    }

    @Override
    @NotNull
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of("<gray>Causes the opponent to bleed when attacking with sword, dealing damage over time.", "<gray>Activation chance: <gold>" + getActivationChance(mcRPGPlayer.asSkillHolder()));
    }

    public double getActivationChance(@NotNull SkillHolder skillHolder) {
        var skillHolderDataOptional = skillHolder.getSkillHolderData(Swords.SWORDS_KEY);
        if (skillHolderDataOptional.isPresent()) {
            Parser parser = new Parser(getPlugin().getFileManager().getFile(FileType.SWORDS_CONFIG).getString(SwordsConfigFile.BLEED_ACTIVATION_EQUATION));
            parser.setVariable("swords_level", skillHolderDataOptional.get().getCurrentLevel());
            return parser.getValue();
        }

        return 0.0;
    }

}
