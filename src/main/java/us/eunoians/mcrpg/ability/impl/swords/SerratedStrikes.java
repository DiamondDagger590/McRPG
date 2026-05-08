package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ParserConfigKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.swords.SerratedStrikesActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.ABILITY_DURATION;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.ACTIVATION_CHANCE_INCREASE;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.COOLDOWN;
import static us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys.MANA_COST;

/**
 * Serrated Strikes is an active ability activated via a click-combo sequence that increases
 * the activation rate of {@link Bleed} while active.
 */
@ParserConfigKeys({"cooldown", "mana-cost", "duration", "bleed-activation-boost"})
public final class SerratedStrikes extends McRPGAbility implements ConfigurableActiveAbility, ConfigurableSkillAbility, ComboActivatable {

    public static final NamespacedKey SERRATED_STRIKES_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "serrated_strikes");

    public SerratedStrikes(@NotNull McRPG plugin) {
        super(plugin, SERRATED_STRIKES_KEY);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.SWORDS_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.SERRATED_STRIKES_DISPLAY_ITEM_HEADER;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(SwordsConfigFile.SERRATED_STRIKES_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Swords.SWORDS_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "serrated_strikes";
    }

    /**
     * Activates Serrated Strikes via the combo system. Fires
     * {@link SerratedStrikesActivateEvent} and, if not cancelled,
     * marks the ability as active for its tier-dependent duration.
     *
     * @param abilityHolder The holder activating this ability.
     * @return {@code true} if the ability executed, {@code false} if the
     *         event was cancelled by a third-party listener.
     */
    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        int duration = getDuration(getCurrentAbilityTier(abilityHolder));
        SerratedStrikesActivateEvent serratedStrikesActivateEvent =
                new SerratedStrikesActivateEvent(abilityHolder, duration);
        Bukkit.getPluginManager().callEvent(serratedStrikesActivateEvent);

        if (serratedStrikesActivateEvent.isCancelled()) {
            return false;
        }
        abilityHolder.addActiveAbility(this, serratedStrikesActivateEvent.getDuration());
        return true;
    }

    /**
     * Delegates to {@link #comboActivate(AbilityHolder)}. This ability has no activation
     * components registered, so this method is never called via the event-listener path;
     * it remains implemented to satisfy the {@link us.eunoians.mcrpg.ability.Ability} interface.
     *
     * @param abilityHolder The holder activating this ability.
     * @param event         The triggering event (unused).
     * @return The result of {@link #comboActivate(AbilityHolder)}.
     */
    @Override
    public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        return comboActivate(abilityHolder);
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return SwordsConfigFile.SERRATED_STRIKES_ENABLED;
    }

    /**
     * Gets the duration of this ability for the given tier.
     *
     * @param tier The tier to get the duration for.
     * @return The duration of this ability.
     */
    public int getDuration(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "duration");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "duration");
        Parser parser;
        if (swordsConfig.contains(tierRoute)) {
            parser = new Parser(swordsConfig.getString(tierRoute));
        } else {
            parser = new Parser(swordsConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return (int) parser.getValue();
    }

    /**
     * Gets the amount to boost {@link Bleed}'s activation chance by.
     *
     * @param tier The tier to get the activation chance boost for.
     * @return The amount to boost {@link Bleed}'s activation chance for.
     */
    public double getBoostToBleedActivation(int tier) {
        YamlDocument swordsConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "bleed-activation-boost");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "bleed-activation-boost");
        Parser parser;
        if (swordsConfig.contains(tierRoute)) {
            parser = new Parser(swordsConfig.getString(tierRoute));
        } else {
            parser = new Parser(swordsConfig.getString(allTiersRoute));
        }
        parser.setVariable("tier", tier);
        return parser.getValue();
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        int tier = getCurrentAbilityTier(player.asSkillHolder());
        placeholders.put(ABILITY_DURATION.getKey(), Integer.toString(getDuration(tier)));
        placeholders.put(COOLDOWN.getKey(), Long.toString(getCooldown(player.asSkillHolder())));
        placeholders.put(ACTIVATION_CHANCE_INCREASE.getKey(), McRPGMethods.getChanceNumberFormat().format(getBoostToBleedActivation(tier)));
        placeholders.put(MANA_COST.getKey(), Integer.toString(getManaCost(player.asSkillHolder())));
        return placeholders;
    }
}
