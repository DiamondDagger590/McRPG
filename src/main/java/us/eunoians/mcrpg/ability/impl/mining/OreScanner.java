package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.util.Methods;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.ability.impl.ConfigurableActiveAbility;
import us.eunoians.mcrpg.ability.impl.ReloadableContentAbility;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.ReloadableOreScannerBlocks;
import us.eunoians.mcrpg.ability.ready.MiningReadyData;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.api.event.ability.mining.OreScannerActivateEvent;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.mining.Mining;
import us.eunoians.mcrpg.task.glow.BlockRemoveGlowTask;
import us.eunoians.mcrpg.task.glow.BlockStartGlowTask;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Ore Scanner is an active ability that will scan the blocks around the player, informing the player of
 * all the different kinds of blocks around them while pointing them to the nearest, most valuable block.
 */
public final class OreScanner extends BaseAbility implements ConfigurableActiveAbility, ReloadableContentAbility {

    public static final NamespacedKey ORE_SCANNER_KEY = new NamespacedKey(McRPG.getInstance(), "ore_scanner");
    private final ReloadableOreScannerBlocks ORE_SCANNER_BLOCK_TYPES = new ReloadableOreScannerBlocks(getYamlDocument(), MiningConfigFile.ORE_SCANNER_BLOCK_TYPES);

    public OreScanner() {
        super(ORE_SCANNER_KEY);
        addReadyingComponent(MiningComponents.MINING_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addReadyingComponent(MiningComponents.MINING_READY_COMPONENT, PlayerInteractEntityEvent.class, 0);

        addActivatableComponent(MiningComponents.MINING_ACTIVATE_ON_READY_COMPONENT, PlayerInteractEvent.class, 0);
        addActivatableComponent(MiningComponents.HOLDING_PICKAXE_INTERACT_ACTIVATE_COMPONENT, PlayerInteractEvent.class, 1);
    }

    @NotNull
    @Override
    public Route getAbilityTierConfigurationRoute() {
        return MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER;
    }

    @NotNull
    @Override
    public YamlDocument getYamlDocument() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MINING_CONFIG);
    }

    @Override
    public int getMaxTier() {
        return getYamlDocument().getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getSkill() {
        return Optional.of(Mining.MINING_KEY);
    }

    @NotNull
    @Override
    public Optional<String> getDatabaseName() {
        return Optional.of("ore_scanner");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Ore Scanner";
    }

    @NotNull
    @Override
    public List<String> getDescription(@NotNull McRPGPlayer mcRPGPlayer) {
        return List.of();
    }

    @NotNull
    @Override
    public ItemStack getGuiItem(@NotNull AbilityHolder abilityHolder) {
        return new ItemStack(Material.DIAMOND_ORE);
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        PlayerInteractEvent playerInteractEvent = (PlayerInteractEvent) event;
        Player player = playerInteractEvent.getPlayer();
        Location playerLocation = player.getLocation();
        abilityHolder.unreadyHolder();
        abilityHolder.addActiveAbility(this);
        int radius = getRange(getCurrentAbilityTier(abilityHolder));

        Map<OreScannerBlockType, Set<Location>> instancesOfBlocks = new HashMap<>();
        Map<OreScannerBlockType, Location> closestBlock = new HashMap<>();
        for (int x = -1 * radius; x < radius; x++) {
            for (int z = -1 * radius; z < radius; z++) {
                for (int y = -1 * radius; y < radius; y++) {
                    Block block = player.getLocation().add(x, y, z).getBlock();
                    Location location = block.getLocation();
                    Optional<OreScannerBlockType> optionalOreScannerBlockType = getScannerTypeForBlock(block);
                    if (optionalOreScannerBlockType.isPresent()) {
                        OreScannerBlockType oreScannerBlockType = optionalOreScannerBlockType.get();
                        Set<Location> locations = instancesOfBlocks.getOrDefault(oreScannerBlockType, new HashSet<>());
                        locations.add(location);
                        instancesOfBlocks.put(oreScannerBlockType, locations);

                        if (location.distance(playerLocation) <= closestBlock.getOrDefault(oreScannerBlockType, location).distance(playerLocation)) {
                            closestBlock.put(oreScannerBlockType, location);
                        }
                    }
                }
            }
        }
        abilityHolder.removeActiveAbility(this);
        OreScannerActivateEvent oreScannerActivateEvent = new OreScannerActivateEvent(abilityHolder, instancesOfBlocks);
        Bukkit.getPluginManager().callEvent(oreScannerActivateEvent);
        if (oreScannerActivateEvent.isCancelled()) {
            return;
        }
        putHolderOnCooldown(abilityHolder);

        var highestWeightedScanType = getHighestWeightedScanType(instancesOfBlocks.keySet());
        highestWeightedScanType.ifPresent(oreScannerBlockType -> {
            Location toPoint = closestBlock.get(oreScannerBlockType);
            player.teleport(Methods.lookAt(playerLocation, toPoint));
        });

        Audience audience = McRPG.getInstance().getAdventure().player(player);
        instancesOfBlocks.keySet().forEach(oreScannerBlockType -> {
            Set<Location> locations = instancesOfBlocks.get(oreScannerBlockType);
            BlockStartGlowTask blockStartGlowTask = new BlockStartGlowTask(player, oreScannerBlockType, locations);
            blockStartGlowTask.runTask();
            BlockRemoveGlowTask blockRemoveGlowTask = new BlockRemoveGlowTask(player, locations);
            blockRemoveGlowTask.runTask();
            audience.sendMessage(McRPG.getInstance().getMiniMessage().deserialize("<gray>You've detected <gold>" + locations.size() + " " + oreScannerBlockType.typeName() + "</gold> near you."));
        });
    }

    @Override
    public boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(MiningConfigFile.ORE_SCANNER_ENABLED);
    }

    /**
     * Gets the range of ore scanner.
     *
     * @param tier The tier to get the range for.
     * @return The range of ore scanner for the provided tier.
     */
    public int getRange(int tier) {
        YamlDocument miningConfig = getYamlDocument();
        Route allTiersRoute = Route.addTo(getRouteForAllTiers(), "range");
        Route tierRoute = Route.addTo(getRouteForTier(tier), "range");
        if (miningConfig.contains(tierRoute)) {
            return miningConfig.getInt(tierRoute);
        } else {
            return miningConfig.getInt(allTiersRoute);
        }
    }

    /**
     * Gets the {@link OreScannerBlockType} that matches the provided {@link Block}, if any.
     *
     * @param block The {@link Block} to get a matching {@link OreScannerBlockType} for.
     * @return An {@link Optional} containing the matched {@link OreScannerBlockType}, or an empty optional
     * if no matches were found.
     */
    @NotNull
    public Optional<OreScannerBlockType> getScannerTypeForBlock(@NotNull Block block) {
        for (OreScannerBlockType scannerBlockType : ORE_SCANNER_BLOCK_TYPES.getContent()) {
            if (scannerBlockType.isBlockScannable(block)) {
                return Optional.of(scannerBlockType);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the highest weighted {@link OreScannerBlockType} out of all scanner types provided.
     *
     * @param scannerTypes The {@link OreScannerBlockType}s to get the highest weighted one of.
     * @return An {@link Optional} containing the highest weighted {@link OreScannerBlockType}, or empty
     * if an empty {@link Set} was provided.
     */
    @NotNull
    public Optional<OreScannerBlockType> getHighestWeightedScanType(@NotNull Set<OreScannerBlockType> scannerTypes) {
        return scannerTypes.stream().max(Comparator.comparingInt(OreScannerBlockType::weight));
    }

    @NotNull
    @Override
    public Optional<ReadyData> getReadyData() {
        return Optional.of(new MiningReadyData());
    }

    @Override
    public Set<ReloadableContent<?>> getReloadableContent() {
        return Set.of(ORE_SCANNER_BLOCK_TYPES);
    }

    @NotNull
    @Override
    public Set<NamespacedKey> getApplicableAttributes() {
        return ConfigurableActiveAbility.super.getApplicableAttributes();
    }
}
