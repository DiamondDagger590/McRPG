package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableSet;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.api.event.ability.woodcutting.DryadsGiftActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DryadsGift extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility, ReloadableContentAbility {

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
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Woodcutting.WOODCUTTING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("dryads_gift");
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Has a chance to drop experience when breaking wood",
                "<gray>Activation Chance: <gold>" + getActivationChance(currentTier));
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Dryad's Gift";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.OAK_SAPLING, 1);
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

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(WoodcuttingConfigFile.DRYADS_GIFT_ENABLED);
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
        return getPlugin().getFileManager().getFile(FileType.WOODCUTTING_CONFIG);
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
}
