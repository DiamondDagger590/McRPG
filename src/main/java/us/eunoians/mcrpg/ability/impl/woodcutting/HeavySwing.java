package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableSet;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.event.event.ability.woodcutting.HeavySwingActivateEvent;
import us.eunoians.mcrpg.event.event.ability.woodcutting.HeavySwingFakeBlockBreakEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting;
import us.eunoians.mcrpg.util.McRPGMethods;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Heavy Swing is an ability that has a chance to activate when a player breaks wood using an axe. This ability will
 * break wood in an area around the broken block.
 */
public class HeavySwing extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility, ReloadableContentAbility {

    public static final NamespacedKey HEAVY_SWING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "heavy_swing");

    private final ReloadableSet<Material> VALID_BLOCK_TYPES;

    public HeavySwing(@NotNull McRPG mcRPG) {
        super(mcRPG, HEAVY_SWING_KEY);
        addActivatableComponent(HeavySwingComponents.HEAVY_SWING_ACTIVATE_COMPONENT, BlockBreakEvent.class, 0);
        VALID_BLOCK_TYPES = getValidBlockTypes();
    }

    /**
     * Get a {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link Material}s that can trigger this ability.
     */
    private ReloadableSet<Material> getValidBlockTypes() {
        return new ReloadableSet<>(getYamlDocument(), WoodcuttingConfigFile.HEAVY_SWING_VALID_BLOCKS, strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return HEAVY_SWING_KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Woodcutting.WOODCUTTING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("heavy_swing");
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        int currentTier = getCurrentAbilityTier(mcRPGPlayer.asSkillHolder());
        return List.of("<gray>Has a chance to break wood in an area of where you mined",
                "<gray>Activation Chance: <gold>" + getActivationChance(currentTier));
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Heavy Swing";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.NETHERITE_AXE, 1);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
        Player player = blockBreakEvent.getPlayer();
        Location origin = blockBreakEvent.getBlock().getLocation();
        int radius = getRadius(getCurrentAbilityTier(abilityHolder));
        Set<Location> toBreakLocations = new HashSet<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location possibleBlockLocation = new Location(origin.getWorld(), origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    Block possibleBlock = possibleBlockLocation.getBlock();
                    // Only trigger on natural blocks and whenever the block is valid
                    if (!origin.equals(possibleBlockLocation) && WorldManager.isBlockNatural(possibleBlock) && isBlockValid(possibleBlock)) {
                        // Throw a fake block break event to check for protection checks
                        HeavySwingFakeBlockBreakEvent heavySwingFakeBlockBreakEvent = new HeavySwingFakeBlockBreakEvent(player, possibleBlock);
                        Bukkit.getPluginManager().callEvent(heavySwingFakeBlockBreakEvent);
                        if (!heavySwingFakeBlockBreakEvent.isCancelled()) {
                            toBreakLocations.add(possibleBlockLocation);
                        }
                    }
                }
            }
        }

        HeavySwingActivateEvent heavySwingActivateEvent = new HeavySwingActivateEvent(abilityHolder, toBreakLocations);
        Bukkit.getPluginManager().callEvent(heavySwingActivateEvent);
        if (!heavySwingActivateEvent.isCancelled()) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            for (Location possibleBlockLocation : toBreakLocations) {
                Block possibleBlock = possibleBlockLocation.getBlock();
                possibleBlock.breakNaturally(heldItem);
            }
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(WoodcuttingConfigFile.HEAVY_SWING_ENABLED);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(WoodcuttingConfigFile.HEAVY_SWING_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER;
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
     * Gets the radius of this ability for the given tier.
     *
     * @param tier The tier to get the radius for.
     * @return The radius of this ability for the given tier.
     */
    public int getRadius(int tier) {
        return getYamlDocument().getInt(Route.addTo(getRouteForTier(tier), "radius"));
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
