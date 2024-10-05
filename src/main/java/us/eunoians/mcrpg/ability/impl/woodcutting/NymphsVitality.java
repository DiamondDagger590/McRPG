package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableSet;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.McRPGAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableTierableAbility;
import us.eunoians.mcrpg.ability.impl.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.event.event.ability.woodcutting.NymphsVitalityActivateEvent;
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

/**
 * Nymphs Vitality is an ability that prevents players from losing hunger in a wooded biome
 * and lets players regain hunger up to a certain point as they move in a wooded biome.
 */
public class NymphsVitality extends McRPGAbility implements PassiveAbility, ConfigurableTierableAbility, ReloadableContentAbility {

    public static final NamespacedKey NYMPHS_VITALITY_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "nymphs_vitality");

    private final ReloadableSet<Biome> VALID_BIOMES;

    public NymphsVitality(@NotNull McRPG mcRPG) {
        super(mcRPG, NYMPHS_VITALITY_KEY);
        addActivatableComponent(NymphsVitalityComponents.NYMPHS_VITALITY_ACTIVATE_ON_HUNGER_DROP_COMPONENT, FoodLevelChangeEvent.class, 0);
        addActivatableComponent(NymphsVitalityComponents.NYMPHS_VITALITY_ACTIVATE_ON_MOVE_DROP_COMPONENT, PlayerMoveEvent.class, 0);
        VALID_BIOMES = getValidBiomes();
    }

    /**
     * Get a {@link ReloadableSet} of {@link Biome}s that can trigger this ability.
     *
     * @return A {@link ReloadableSet} of {@link Biome}s that can trigger this ability.
     */
    private ReloadableSet<Biome> getValidBiomes() {
        return new ReloadableSet<>(getYamlDocument(), WoodcuttingConfigFile.NYMPHS_VITALITY_VALID_BIOMES, strings -> strings.stream().map(Biome::valueOf).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public NamespacedKey getAbilityKey() {
        return NYMPHS_VITALITY_KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Woodcutting.WOODCUTTING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("nymphs_vitality");
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of("<gray>Will prevent your hunger from dropping past a certain point when in wooded biomes.");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Nymph's Vitality";
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.ACACIA_SAPLING, 1);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        if (event instanceof FoodLevelChangeEvent foodLevelChangeEvent) {
            NymphsVitalityActivateEvent nymphsVitalityActivateEvent = new NymphsVitalityActivateEvent(abilityHolder);
            Bukkit.getPluginManager().callEvent(nymphsVitalityActivateEvent);
            if (!nymphsVitalityActivateEvent.isCancelled()) {
                foodLevelChangeEvent.setCancelled(true);
            }
        } else if (event instanceof PlayerMoveEvent playerMoveEvent) {
            NymphsVitalityActivateEvent nymphsVitalityActivateEvent = new NymphsVitalityActivateEvent(abilityHolder);
            Bukkit.getPluginManager().callEvent(nymphsVitalityActivateEvent);
            if (!nymphsVitalityActivateEvent.isCancelled()) {
                Player player = playerMoveEvent.getPlayer();
                int minimumHunger = getMinimumHunger(getCurrentAbilityTier(abilityHolder));
                if (player.getFoodLevel() < minimumHunger) {
                    // Increase their food level by 1 if their hunger is below the minimum
                    player.setFoodLevel(player.getFoodLevel() + 1);
                }
            }
        }
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(WoodcuttingConfigFile.NYMPHS_VITALITY_ENABLED);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(WoodcuttingConfigFile.NYMPHS_VITALITY_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return WoodcuttingConfigFile.NYMPHS_VITALITY_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return getPlugin().getFileManager().getFile(FileType.WOODCUTTING_CONFIG);
    }

    /**
     * Gets the minimum amount of hunger for the given tier a player can be at while this ability is active.
     *
     * @param tier The tier to get the minimum amount of hunger for.
     * @return The minimum amount of hunger for the given tier a player can be at while this ability is active.
     */
    public int getMinimumHunger(int tier) {
        return getYamlDocument().getInt(Route.addTo(getRouteForTier(tier), "minimum-hunger"));
    }

    /**
     * Checks to see if the provided {@link Biome} is valid for activating this ability.
     *
     * @param biome The {@link Biome} to check.
     * @return {@code true} if the provided {@link Biome} is valid for activating this ability.
     */
    public boolean isBiomeValid(@NotNull Biome biome) {
        return VALID_BIOMES.getContent().contains(biome);
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableTierableAbility.super.getApplicableAttributes();
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(VALID_BIOMES);
    }
}
