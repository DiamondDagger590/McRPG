package us.eunoians.mcrpg.abilities;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This class offers some basic construction for an {@link Ability} and should
 * be extended by all abilities
 *
 * @author DiamondDagger590
 */
public abstract class AbilityConstructor implements Ability{
  
  /**
   * The {@link McRPGPlayer} who this {@link Ability} belongs to
   */
  private final McRPGPlayer mcRPGPlayer;
  
  /**
   * A boolean representing if this {@link Ability} needs saving
   */
  protected boolean isDirty;
  
  /**
   * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
   */
  public AbilityConstructor(McRPGPlayer mcRPGPlayer){
    this.mcRPGPlayer = mcRPGPlayer;
  }
  
  /**
   * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
   *
   * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
   */
  @Override
  public @NotNull McRPGPlayer getPlayer(){
    return this.mcRPGPlayer;
  }
}
