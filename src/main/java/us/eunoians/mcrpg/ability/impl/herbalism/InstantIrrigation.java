package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.CooldownableAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.herbalism.InstantIrrigationActivateEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.herbalism.Herbalism;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;

public final class InstantIrrigation extends McRPGAbility implements PassiveAbility, ConfigurableSkillAbility, CooldownableAbility {

    public static final NamespacedKey INSTANT_IRRIGATION_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "instant_irrigation");

    public InstantIrrigation(@NotNull McRPG mcRPG) {
        super(mcRPG, INSTANT_IRRIGATION_KEY);
        addActivatableComponent(InstantIrrigationComponents.HOLDING_HOE_BREAK_BLOCK_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
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
        return LocalizationKey.INSTANT_IRRIGATION_DISPLAY_ITEM_HEADER;
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
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        blockBreakEvent.setCancelled(true);
        Block block = blockBreakEvent.getBlock();
        InstantIrrigationActivateEvent instantIrrigationActivateEvent = new InstantIrrigationActivateEvent(abilityHolder, block);
        Bukkit.getPluginManager().callEvent(instantIrrigationActivateEvent);
        if (instantIrrigationActivateEvent.isCancelled()) {
            return;
        }
        block.setType(Material.WATER);
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation(), 3, 0.5, 0.5, 0.5);
        long cooldown = putHolderOnCooldown(abilityHolder);
        McRPGPlayerManager mcRPGPlayerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        mcRPGPlayerManager.getPlayer(abilityHolder.getUUID()).ifPresent(mcRPGPlayer -> {
            Player player = mcRPGPlayer.getAsBukkitPlayer().orElseThrow(IllegalStateException::new);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.INSTANT_IRRIGATION_ACTIVATION_NOTIFICATION, Map.of("cooldown", Long.toString(cooldown))));
        });
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        YamlDocument yamlDocument = getYamlDocument();
        Parser parser = new Parser(yamlDocument.getString(HerbalismConfigFile.INSTANT_IRRIGATION_COOLDOWN));
        if (abilityHolder instanceof SkillHolder skillHolder) {
            parser.setVariable("herbalism_level", skillHolder.getSkillHolderData(Herbalism.HERBALISM_KEY).orElseThrow(IllegalStateException::new).getCurrentLevel());
        } else {
            parser.setVariable("herbalism_level", 0);
        }
        return (long) parser.getValue();
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        return placeholders;
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return CooldownableAbility.super.getApplicableAttributes();
    }
}
