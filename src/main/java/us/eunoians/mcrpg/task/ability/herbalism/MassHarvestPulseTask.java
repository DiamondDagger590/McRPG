package us.eunoians.mcrpg.task.ability.herbalism;

import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.MassHarvestPullItemsAttribute;
import us.eunoians.mcrpg.ability.impl.herbalism.MassHarvest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * This task is fired whenever {@link MassHarvest} is activated.
 * It will harvest blocks in a spherical shell around the player, triggering
 * a pull effect via {@link MassHarvestItemPullTask} to pull the items closer to the player.
 */
public class MassHarvestPulseTask extends ExpireableCoreTask {

    private static final double TASK_FREQUENCY = .05;

    private final Player player;
    private final double maxRadius;
    private final MassHarvest massHarvest;
    @Nullable
    private MassHarvestItemPullTask pullTask;
    private double lastHarvestRadius;
    private Location center;

    public MassHarvestPulseTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer, @NotNull MassHarvest massHarvest, double maxRadius) {
        super(plugin, 0, TASK_FREQUENCY, 60L);
        this.player = mcRPGPlayer.getAsBukkitPlayer().orElseThrow(IllegalStateException::new);
        this.massHarvest = massHarvest;
        this.maxRadius = maxRadius;
        this.lastHarvestRadius = 0;
        mcRPGPlayer.asSkillHolder()
                .getAbilityData(massHarvest)
                .flatMap(abilityData -> abilityData.getAbilityAttribute(AbilityAttributeRegistry.MASS_HARVEST_PULL_ITEMS_ATTRIBUTE))
                .ifPresent(abilityAttribute -> {
                    if (abilityAttribute instanceof MassHarvestPullItemsAttribute massHarvestPullItemsAttribute && massHarvestPullItemsAttribute.getContent()) {
                        pullTask = new MassHarvestItemPullTask(plugin, player, 0, maxRadius);
                        pullTask.runTask();
                    }
                });
    }

    @Override
    protected void onTaskExpire() {
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onDelayComplete() {

    }

    @Override
    protected void onIntervalStart() {
        if (!player.isOnline()) {
            this.cancelTask();
            return;
        }

        if (center == null) {
            center = player.getLocation().clone();
        }

        double progress = (this.getCurrentInterval() * TASK_FREQUENCY) / 2;
        // If our progress goes over the maximum radius, cancel the task
        if (progress > 1) {
            this.cancelTask();
            return;
        }
        double currentRadius = Math.min(maxRadius, progress * maxRadius);
        playRingParticles(center, currentRadius);

        if (this.getCurrentInterval() % 3 == 0) {
            harvestAndReplantShell(center, lastHarvestRadius, currentRadius);
            lastHarvestRadius = currentRadius;
        }
    }

    @Override
    protected void onIntervalComplete() {

    }

    @Override
    protected void onIntervalPause() {

    }

    @Override
    protected void onIntervalResume() {

    }

    /**
     * Plays a circular particle effect around the given center location with the specified radius.
     * This method uses the {@link Particle#SWEEP_ATTACK} particle type to create a slicing effect.
     *
     * @param center The central location where the particle effect originates; must not be null
     * @param radius The current radius of the circle in which the particles are displayed; must be greater than 0
     */
    private void playRingParticles(@NotNull Location center, double radius) {
        World world = center.getWorld();
        if (world == null || radius <= 0) return;

        for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16.0) {
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            double y = center.getY() + 0.1;

            world.spawnParticle(
                    Particle.SWEEP_ATTACK,
                    x, y, z,
                    3,
                    0.1, 0.1, 0.1,
                    0.01
            );
        }
    }

    private int harvestAndReplantShell(Location center, double innerRadius, double outerRadius) {
        World world = center.getWorld();
        if (world == null) return 0;

        // Add a small thickness to ensure blocks get hit even if tick steps are coarse
        double shellPadding = 0.75;
        double innerSq = Math.max(0, (innerRadius - shellPadding) * (innerRadius - shellPadding));
        double outerSq = (outerRadius + shellPadding) * (outerRadius + shellPadding);

        int blockRadius = (int) Math.ceil(outerRadius + shellPadding);
        Block centerBlock = center.getBlock();

        int harvested = 0;

        // iterate a cube, filter to spherical shell band
        for (int dx = -blockRadius; dx <= blockRadius; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -blockRadius; dz <= blockRadius; dz++) {

                    Block block = centerBlock.getRelative(dx, dy, dz);
                    Location bCenter = block.getLocation().add(0.5, 0.5, 0.5);

                    double distSq = center.distanceSquared(bCenter);
                    if (distSq < innerSq || distSq > outerSq) continue;

                    BlockData data = block.getBlockData();
                    CustomBlockWrapper customBlockWrapper = new CustomBlockWrapper(block);
                    if (!massHarvest.getValidBlockTypes().getContent().contains(customBlockWrapper)) {
                        continue;
                    }

                    // Not all valid blocks may have ageable data
                    if (data instanceof Ageable ageable) {
                        if (ageable.getAge() < ageable.getMaximumAge()) continue;
                    }

                    // Call block break event to do permission checks
                    BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
                    Bukkit.getPluginManager().callEvent(blockBreakEvent);
                    if (blockBreakEvent.isCancelled()) continue;

                    List<ItemStack> itemsToDrop = CustomBlockWrapper.drops(block, player.getInventory().getItemInMainHand(), player);
                    CustomBlockWrapper.playBlockDropEffects(block);
                    CustomBlockWrapper.removeBlock(block);

                    // Spawn the items to drop
                    Location locationToDrop = block.getLocation().clone().add(0.5, 0.5, 0.5);
                    List<Item> items = new ArrayList<>();
                    for (ItemStack drop : itemsToDrop) {
                        if (drop == null || drop.getType().isAir() || drop.getAmount() <= 0) continue;
                        items.add(world.dropItemNaturally(locationToDrop, drop));
                    }
                    // Call event to allow duplication abilities to fire
                    BlockDropItemEvent blockDropItemEvent = new BlockDropItemEvent(block, block.getState(), player, List.copyOf(items));
                    blockDropItemEvent.callEvent();
                    // If event if cancelled, then despawn all related items
                    if (blockDropItemEvent.isCancelled()) {
                        items.forEach(Item::remove);
                        blockDropItemEvent.getItems().forEach(Item::remove);
                    } else {
                        // Remove items removed from drop item event
                        for (Item item : items) {
                            if (!blockDropItemEvent.getItems().contains(item)) {
                                item.remove();
                            }
                        }
                    }
                    // Add items to be pulled
                    if (pullTask != null) {
                        blockDropItemEvent.getItems().forEach(pullTask::addItemToPull);
                    }

                    // Now we need to check what the original state of the block was
                    if (customBlockWrapper.customBlock().isPresent()) {
                        // We can assume if there is a custom block then there has to be a custom block hook
                        CustomBlockHook customBlockHook = RegistryAccess.registryAccess().registry(McRPGRegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class).get(0);
                        customBlockHook.placeCustomBlock(block.getLocation(), customBlockWrapper.customBlock().get());
                        Block updatedBlock = block.getWorld().getBlockAt(block.getLocation());
                        if (updatedBlock.getBlockData() instanceof Ageable updatedAgeable) {
                            updatedAgeable.setAge(0);
                            updatedBlock.setBlockData(updatedAgeable, true);
                        }
                    } else {
                        block.setType(customBlockWrapper.material().get(), true);
                        if (data instanceof Ageable ageable) {
                            Ageable replanted = (Ageable) ageable.clone();
                            replanted.setAge(0);
                            block.setBlockData(replanted, true);
                        }
                    }
                    harvested++;
                }
            }
        }
        return harvested;
    }
}
