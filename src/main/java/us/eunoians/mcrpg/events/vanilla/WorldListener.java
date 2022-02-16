package us.eunoians.mcrpg.events.vanilla;

import com.gmail.nossr50.config.WorldBlacklist;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.events.fake.FakeBlockBreakEvent;
import com.gmail.nossr50.events.fake.FakeBlockDamageEvent;
import com.gmail.nossr50.skills.alchemy.Alchemy;
import com.gmail.nossr50.skills.excavation.ExcavationManager;
import com.gmail.nossr50.skills.herbalism.HerbalismManager;
import com.gmail.nossr50.skills.mining.MiningManager;
import com.gmail.nossr50.skills.repair.Repair;
import com.gmail.nossr50.skills.salvage.Salvage;
import com.gmail.nossr50.skills.woodcutting.WoodcuttingManager;
import com.gmail.nossr50.util.BlockUtils;
import com.gmail.nossr50.util.EventUtils;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.gmail.nossr50.util.sounds.SoundManager;
import com.gmail.nossr50.util.sounds.SoundType;
import com.gmail.nossr50.worldguard.WorldGuardManager;
import com.gmail.nossr50.worldguard.WorldGuardUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.HeavySwingTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.LargerSpadeTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.PansShrineTestEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.BlastTestEvent;
import us.eunoians.mcrpg.api.util.brewing.BrewingStandManager;
import us.eunoians.mcrpg.players.McRPGPlayer;

/**
 * This code is not mine. It is copyright from the original McRPG allowed for use by their license.
 * This code has been modified from it source material
 * It was released under the GPLv3 license
 */

public class WorldListener implements Listener {
    private final McRPG plugin;

    public WorldListener(McRPG plugin) {
        this.plugin = plugin;
    }

    /**
     * Monitor BlockPistonExtend events.
     *
     * @param event The event to monitor
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        World world = event.getBlock().getWorld();

        /* WORLD BLACKLIST CHECK */
        if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())) {
            return;
        }

//        if (!ExperienceConfig.getInstance().isPistonCheatingPrevented()) {
//            return;
//        }

        BlockFace direction = event.getDirection();
        Block movedBlock;
        for (Block block : event.getBlocks()) {
            movedBlock = block.getRelative(direction);

            if (isWithinWorldBounds(movedBlock)) {
                McRPG.getPlaceStore().setTrue(movedBlock);
            }
        }
    }

    /**
     * Monitor BlockPistonRetract events.
     *
     * @param event The event to watch
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {

        World world = event.getBlock().getWorld();

        /* WORLD BLACKLIST CHECK */
        if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())) {
            return;
        }

//        if (!ExperienceConfig.getInstance().isPistonCheatingPrevented()) {
//            return;
//        }

        // Get opposite direction so we get correct block
        BlockFace direction = event.getDirection();
        Block movedBlock = event.getBlock().getRelative(direction);

        //Spigot makes bad things happen in its API
        if (isWithinWorldBounds(movedBlock)) {
            McRPG.getPlaceStore().setTrue(movedBlock);
        }

        for (Block block : event.getBlocks()) {
            if (isWithinWorldBounds(block)) {
                McRPG.getPlaceStore().setTrue(block.getRelative(direction));
            }
        }
    }

    /**
     * Monitor blocks formed by entities (snowmen)
     * Does not seem to monitor stuff like a falling block creating a new block
     *
     * @param event The event to watch
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockFormEvent(EntityBlockFormEvent event) {

        World world = event.getBlock().getWorld();

        /* WORLD BLACKLIST CHECK */
        if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())) {
            return;
        }

        BlockState blockState = event.getNewState();

        if (true) { //TODO BlockUtils.shouldBeWatched(blockState)) {
            Block block = blockState.getBlock();

            if (isWithinWorldBounds(block)) {
                McRPG.getPlaceStore().setTrue(block);
            }
        }
    }

    /*
     * Does not monitor stuff like a falling block replacing a liquid
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFormEvent(BlockFormEvent event) {

        World world = event.getBlock().getWorld();

        /* WORLD BLACKLIST CHECK */
        if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
               McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())){
            return;
        }

        if (true) { //ExperienceConfig.getInstance().preventStoneLavaFarming()) {
            BlockState newState = event.getNewState();

            //if (newState.getType() != Material.OBSIDIAN && ExperienceConfig.getInstance().doesBlockGiveSkillXP(PrimarySkillType.MINING, newState.getBlockData())) {
                if (isWithinWorldBounds(newState.getBlock())) {
                    McRPG.getPlaceStore().setTrue(newState);
                }
            //}
        }
    }

    /**
     * Monitor BlockPlace events.
     *
     * @param event The event to watch
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        BlockState blockState = event.getBlock().getState();
        Block block = blockState.getBlock();
        World world = block.getWorld();

        /* Check if the blocks placed should be monitored so they do not give out XP in the future */
//      if (!Tag.LOGS.isTagged(event.getBlockReplacedState().getType()) || !Tag.LOGS.isTagged(event.getBlockPlaced().getType()))

        /* WORLD BLACKLIST CHECK */
        if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
               McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())){
            return;
        }

        if (isWithinWorldBounds(block)) {
            //NOTE: BlockMultiPlace has its own logic so don't handle anything that would overlap
            if (!(event instanceof BlockMultiPlaceEvent)) {
                McRPG.getPlaceStore().setTrue(blockState);
            }
        }
    }

    /**
     * Monitor BlockMultiPlace events.
     *
     * @param event The event to watch
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
        for (BlockState replacedBlockState : event.getReplacedBlockStates()) {
            BlockState blockState = replacedBlockState.getBlock().getState();
            Block block = blockState.getBlock();

            /* Check if the blocks placed should be monitored so they do not give out XP in the future */
            if (isWithinWorldBounds(block)) {
                //Updated: 10/5/2021
                //Note: For some reason Azalea trees trigger this event but no other tree does (as of 10/5/2021) but if this changes in the future we may need to update this
//                if (BlockUtils.isPartOfTree(event.getBlockPlaced())) {
//                    return;
//                }
                //TODO update this in rewrite

                //Track unnatural blocks
                for (BlockState replacedStates : event.getReplacedBlockStates()) {
                    McRPG.getPlaceStore().setTrue(replacedStates);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();

        /* WORLD BLACKLIST CHECK */
        if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
               McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())){
            return;
        }

        // Minecraft is dumb, the events still throw when a plant "grows" higher than the max block height.  Even though no new block is created
        if (isWithinWorldBounds(block)) {
            McRPG.getPlaceStore().setFalse(block);
        }
    }

    /**
     * Monitor BlockBreak events.
     *
     * @param event The event to monitor
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        /* WORLD BLACKLIST CHECK */
        Block block = event.getBlock();
        World world = block.getWorld();

        if (event instanceof HeavySwingTestEvent || event instanceof LargerSpadeTestEvent || event instanceof BlastTestEvent || event instanceof PansShrineTestEvent) {
            return;
        }

        if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
               McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world.getName())){
            return;
        }

        BlockState blockState = block.getState();
        Location location = blockState.getLocation();

//        if (!BlockUtils.shouldBeWatched(blockState)) {
//            return;
//        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBloodPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() == Material.REDSTONE) {
            NBTItem nbtItem = new NBTItem(event.getItemInHand());
            if (nbtItem.hasKey("McRPGBlood")) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Monitor ChunkUnload events.
     *
     * @param event The event to watch
     */
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {

        Chunk chunk = event.getChunk();
        BrewingStandManager brewingStandManager = McRPG.getInstance().getBrewingStandManager();
        //Some edge case was happening here
        if (chunk == null || event.getWorld() == null) {
            return;
        }

        McRPG.getPlaceStore().chunkUnloaded(chunk.getX(), chunk.getZ(), event.getWorld());
        brewingStandManager.unloadChunk(chunk);
    }

    /**
     * Checks to see if a Block is within the world bounds
     * Prevent processing blocks from other plugins (or perhaps odd spigot anomalies) from sending blocks that can't exist within the world
     *
     * @param block
     * @return
     */
    public static boolean isWithinWorldBounds(@NotNull Block block) {
        World world = block.getWorld();

        //World min height = inclusive | World max height = exclusive
        return block.getY() >= world.getMinHeight() && block.getY() < world.getMaxHeight();
    }
}
