package us.eunoians.mcrpg.api.events.mcrpg.excavation;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import us.eunoians.mcrpg.abilities.excavation.PansShrine;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

import java.util.List;
import java.util.Set;

public class PansShrineEvent extends AbilityActivateEvent {

  @Getter
  private Material itemSacrificed;

  @Getter
  private Set<Material> affectableBlocks;

  @Getter
  private List<String> replaceableBlocks;

  @Getter
  @Setter
  private int cooldown;

  public PansShrineEvent(McRPGPlayer player, PansShrine pansShrine, Material itemSacrificed, Set<Material> affectableBlocks, List<String> replaceableBlocks, int cooldown){
    super(pansShrine, player, AbilityEventType.RECREATIONAL);
    this.itemSacrificed = itemSacrificed;
    this.affectableBlocks = affectableBlocks;
    this.replaceableBlocks = replaceableBlocks;
    this.cooldown = cooldown;
  }
}
