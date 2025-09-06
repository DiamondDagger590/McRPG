package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableSkillAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.woodcutting.DryadsGiftActivateEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dryads Gift is a {@link Woodcutting} ability that activates whenever a player breaks
 * wood. Whenever the ability activates, vanilla experience is dropped.
 */
public class DryadsGift extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility,
        ReloadableContentAbility, ConfigurableSkillAbility {

    public static final NamespacedKey DRYADS_GIFT_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "dryads_gift");

    private final ReloadableSet<Material> VALID_BLOCK_TYPES;

    public DryadsGift(@NotNull McRPG mcRPG) {
        super(mcRPG, DRYADS_GIFT_KEY);
        addActivatableComponent(DryadsGiftComponents.DRYADS_GIFT_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        VALID_BLOCK_TYPES = getValidBlockTypes();
    }

    /**
     * Get a {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     */
    private ReloadableSet<Material> getValidBlockTypes() {
        return new ReloadableSet<>(getYamlDocument(), WoodcuttingConfigFile.DRYADS_GIFT_VALID_BLOCKS, strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return DRYADS_GIFT_KEY;
    }

    @NotNull
    @Override
    public NamespacedKey getSkillKey() {
        return Woodcutting.WOODCUTTING_KEY;
    }

    @NotNull
    @Override
    public String getDatabaseName() {
        return "dryads_gift";
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        Location locationToDrop = blockBreakEvent.getBlock().getLocation();
        DryadsGiftActivateEvent dryadsGiftActivateEvent = new DryadsGiftActivateEvent(abilityHolder, getExperienceToDrop(getCurrentAbilityTier(abilityHolder)));
        if (!dryadsGiftActivateEvent.isCancelled()) {
            (locationToDrop.getWorld().spawn(locationToDrop, ExperienceOrb.class)).setExperience(dryadsGiftActivateEvent.getExperienceToDrop());
        }
    }

    @NotNull
    @Override
    public Route getAbilityEnabledRoute() {
        return WoodcuttingConfigFile.DRYADS_GIFT_ENABLED;
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.WOODCUTTING_CONFIG);
    }

    @NotNull
    @Override
    public Route getDisplayItemRoute() {
        return LocalizationKey.DRYADS_GIFT_DISPLAY_ITEM_HEADER;
    }

    /**
     * Gets the chance of this ability activating for the given tier.
     *
     * @param tier The tier to get the activation chance for.
     * @return The chance of this ability activating for the given tier.
     */
    public double getActivationChance(int tier) {
        return getYamlDocument().getDouble(Route.addTo(getRouteForTier(tier), "activation-chance"));
    }

    /**
     * Gets the amount of experience to drop whenever this ability activates for the
     * given tier.
     *
     * @param tier The tier to get the amount of experience for.
     * @return The amount of experience to drop whenever this ability activates
     * for the given tier.
     */
    public int getExperienceToDrop(int tier) {
        return getYamlDocument().getInt(Route.addTo(getRouteForTier(tier), "experience-to-drop"));

    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableTierableAbility.super.getApplicableAttributes();
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BLOCK_TYPES);
    }

    /**
     * Checks to see if the provided {@link Block} can be used to activate this ability.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can be used to activate this ability.
     */
    public boolean isBlockValid(@NotNull Block block) {
        return VALID_BLOCK_TYPES.getContent().contains(block.getType());
    }

    @NotNull
    @Override
    public Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(AbilityItemPlaceholderKeys.EXPERIENCE_DROPPED.getKey(),
                Integer.toString(getExperienceToDrop(getCurrentAbilityTier(player.asSkillHolder()))));
        placeholders.put(AbilityItemPlaceholderKeys.ACTIVATION_CHANCE.getKey(),
                McRPGMethods.getChanceNumberFormat().format(getActivationChance(getCurrentAbilityTier(player.asSkillHolder()))));
        return placeholders;
    }
}
