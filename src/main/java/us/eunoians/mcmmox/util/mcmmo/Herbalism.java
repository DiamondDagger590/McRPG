package us.eunoians.mcmmox.util.mcmmo;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import us.eunoians.mcmmox.Mcmmox;

import java.util.HashSet;

public class Herbalism {

  public static HashSet<Block> findChorusPlant(Block target){
	return findChorusPlant(target, new HashSet<Block>());
  }

  private static HashSet<Block> findChorusPlant(Block target, HashSet<Block> traversed){
	if(target.getType() != Material.CHORUS_PLANT){
	  return traversed;
	}
	// Prevent any infinite loops, who needs more than 64 chorus anyways
	if(traversed.size() > 64){
	  return traversed;
	}

	traversed.add(target);

	Block relative = target.getRelative(BlockFace.UP, 1);
	if(!traversed.contains(relative)){
	  if(relative.getType() == Material.CHORUS_PLANT){
		traversed.addAll(findChorusPlant(relative, traversed));
	  }
	}

	relative = target.getRelative(BlockFace.NORTH, 1);
	if(!traversed.contains(relative)){
	  if(relative.getType() == Material.CHORUS_PLANT){
		traversed.addAll(findChorusPlant(relative, traversed));
	  }
	}

	relative = target.getRelative(BlockFace.SOUTH, 1);
	if(!traversed.contains(relative)){
	  if(relative.getType() == Material.CHORUS_PLANT){
		traversed.addAll(findChorusPlant(relative, traversed));
	  }
	}

	relative = target.getRelative(BlockFace.EAST, 1);
	if(!traversed.contains(relative)){
	  if(relative.getType() == Material.CHORUS_PLANT){
		traversed.addAll(findChorusPlant(relative, traversed));
	  }
	}

	relative = target.getRelative(BlockFace.WEST, 1);
	if(!traversed.contains(relative)){
	  if(relative.getType() == Material.CHORUS_PLANT){
		traversed.addAll(findChorusPlant(relative, traversed));
	  }
	}

	return traversed;
  }

  /**
   * Calculate the drop amounts for multi block plants based on the blocks
   * relative to them.
   *
   * @param blockState The {@link BlockState} of the bottom block of the plant
   * @return the number of bonus drops to award from the blocks in this plant
   */
  protected static int calculateMultiBlockPlantDrops(BlockState blockState){
	Block block = blockState.getBlock();
	Material blockType = blockState.getType();
	int dropAmount = Mcmmox.getPlaceStore().isTrue(block) ? 0 : 1;

	if(blockType == Material.CHORUS_PLANT){
	  dropAmount = 1;

	  if(block.getRelative(BlockFace.DOWN, 1).getType() == Material.END_STONE){
		HashSet<Block> blocks = findChorusPlant(block);

		dropAmount = blocks.size();

		/*
		 * for(Block b : blocks) {
		 * b.breakNaturally();
		 * }
		 */
	  }
	}
	else{
	  // Handle the two blocks above it - cacti & sugar cane can only grow 3 high naturally
	  for(int y = 1; y < 3; y++){
		Block relativeBlock = block.getRelative(BlockFace.UP, y);

		if(relativeBlock.getType() != blockType){
		  break;
		}

		if(Mcmmox.getPlaceStore().isTrue(relativeBlock)){
		  Mcmmox.getPlaceStore().setFalse(relativeBlock);
		}
		else{
		  dropAmount++;
		}
	  }
	}

	return dropAmount;
  }
}
