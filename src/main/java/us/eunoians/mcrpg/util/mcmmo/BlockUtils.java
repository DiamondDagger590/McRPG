package us.eunoians.mcrpg.util.mcmmo;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;

/**
 * This code is not mine. It is copyright from the original mcMMO allowed for use by their license.
 * Modified by  * This code has been modified from it source material
 * It was released under the GPLv3 license
 */
public final class BlockUtils {

  private BlockUtils(){
  }

  /**
   * Check if a given block should allow for the activation of abilities
   *
   * @param blockState The {@link BlockState} of the block to check
   * @return true if the block should allow ability activation, false
   * otherwise
   */
  public static boolean canActivateAbilities(BlockState blockState){
	switch(blockState.getType()){
	  case BLACK_BED:
	  case BLUE_BED:
	  case BROWN_BED:
	  case CYAN_BED:
	  case GRAY_BED:
	  case GREEN_BED:
	  case LIGHT_BLUE_BED:
	  case LIGHT_GRAY_BED:
	  case LIME_BED:
	  case MAGENTA_BED:
	  case ORANGE_BED:
	  case PINK_BED:
	  case PURPLE_BED:
	  case RED_BED:
	  case WHITE_BED:
	  case YELLOW_BED:
	  case BREWING_STAND:
	  case BOOKSHELF:
	  case CAKE:
	  case CHEST:
	  case DISPENSER:
	  case ENCHANTING_TABLE:
	  case ENDER_CHEST:
	  case OAK_FENCE_GATE:
	  case ACACIA_FENCE_GATE:
	  case DARK_OAK_FENCE_GATE:
	  case SPRUCE_FENCE_GATE:
	  case BIRCH_FENCE_GATE:
	  case JUNGLE_FENCE_GATE:
	  case FURNACE:
	  case JUKEBOX:
	  case LEVER:
	  case NOTE_BLOCK:
	  case STONE_BUTTON:
	  case OAK_BUTTON:
	  case BIRCH_BUTTON:
	  case ACACIA_BUTTON:
	  case DARK_OAK_BUTTON:
	  case JUNGLE_BUTTON:
	  case SPRUCE_BUTTON:
	  case ACACIA_TRAPDOOR:
	  case BIRCH_TRAPDOOR:
	  case DARK_OAK_TRAPDOOR:
	  case JUNGLE_TRAPDOOR:
	  case OAK_TRAPDOOR:
	  case SPRUCE_TRAPDOOR:
		case OAK_WALL_SIGN:
		case ACACIA_WALL_SIGN:
		case BIRCH_WALL_SIGN:
		case DARK_OAK_WALL_SIGN:
		case JUNGLE_WALL_SIGN:
		case SPRUCE_WALL_SIGN:
	  case CRAFTING_TABLE:
	  case BEACON:
	  case ANVIL:
	  case DROPPER:
	  case HOPPER:
	  case TRAPPED_CHEST:
	  case IRON_DOOR:
	  case IRON_TRAPDOOR:
	  case OAK_DOOR:
	  case ACACIA_DOOR:
	  case SPRUCE_DOOR:
	  case BIRCH_DOOR:
	  case JUNGLE_DOOR:
	  case DARK_OAK_DOOR:
	  case OAK_FENCE:
	  case ACACIA_FENCE:
	  case DARK_OAK_FENCE:
	  case BIRCH_FENCE:
	  case JUNGLE_FENCE:
	  case SPRUCE_FENCE:
	  case ARMOR_STAND:
	  case BLACK_SHULKER_BOX:
	  case BLUE_SHULKER_BOX:
	  case BROWN_SHULKER_BOX:
	  case CYAN_SHULKER_BOX:
	  case GRAY_SHULKER_BOX:
	  case GREEN_SHULKER_BOX:
	  case LIGHT_BLUE_SHULKER_BOX:
	  case LIME_SHULKER_BOX:
	  case MAGENTA_SHULKER_BOX:
	  case ORANGE_SHULKER_BOX:
	  case PINK_SHULKER_BOX:
	  case PURPLE_SHULKER_BOX:
	  case RED_SHULKER_BOX:
	  case LIGHT_GRAY_SHULKER_BOX:
	  case WHITE_SHULKER_BOX:
	  case YELLOW_SHULKER_BOX:
		return false;

	  default:
		return true;
	}
  }

  /**
   * Check if a given block is an ore
   *
   * @param blockState The {@link BlockState} of the block to check
   * @return true if the block is an ore, false otherwise
   */
  public static boolean isOre(BlockState blockState){
	return MaterialUtils.isOre(blockState.getType());
  }

  /**
   * Determine if a given block can be made mossy
   *
   * @param blockState The {@link BlockState} of the block to check
   * @return true if the block can be made mossy, false otherwise
   */
  public static boolean canMakeMossy(BlockState blockState){
	switch(blockState.getType()){
	  case COBBLESTONE:
	  case DIRT:
	  case GRASS_PATH:
		return true;
	  case STONE_BRICKS:
		return true;
	  case COBBLESTONE_WALL:
		return true;
	  default:
		return false;
	}
  }

  /**
   * Check if a given block is a leaf
   *
   * @param blockState The {@link BlockState} of the block to check
   * @return true if the block is a leaf, false otherwise
   */
  public static boolean isLeaves(BlockState blockState){
	switch(blockState.getType()){
	  case OAK_LEAVES:
	  case ACACIA_LEAVES:
	  case BIRCH_LEAVES:
	  case DARK_OAK_LEAVES:
	  case JUNGLE_LEAVES:
	  case SPRUCE_LEAVES:
		return true;

	  default:
		return false;
	}
  }

  /**
   * Determine if a given block can activate Herbalism abilities
   *
   * @param blockState The {@link BlockState} of the block to check
   * @return true if the block can be activate Herbalism abilities, false
   * otherwise
   */
  public static boolean canActivateHerbalism(BlockState blockState){
	switch(blockState.getType()){
	  case DIRT:
	  case GRASS:
	  case GRASS_PATH:
	  case FARMLAND:
		return false;

	  default:
		return true;
	}
  }

  public static boolean isPistonPiece(BlockState blockState){
	Material type = blockState.getType();

	return type == Material.MOVING_PISTON || type == Material.AIR;
  }

  /**
   * Get a HashSet containing every transparent block
   *
   * @return HashSet with the IDs of every transparent block
   */
  public static HashSet<Material> getTransparentBlocks(){
	HashSet<Material> transparentBlocks = new HashSet<Material>();

	for(Material material : Material.values()){
	  if(material.isTransparent()){
		transparentBlocks.add(material);
	  }
	}

	return transparentBlocks;
  }

  public static boolean isFullyGrown(BlockState blockState){
	BlockData data = blockState.getBlockData();
	if(data.getMaterial() == Material.CACTUS || data.getMaterial() == Material.SUGAR_CANE)
	  return true;
	if(data instanceof Ageable){
	  Ageable ageable = (Ageable) data;
	  return ageable.getAge() == ageable.getMaximumAge();
	}
	return true;
  }
}
