package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.axes.CripplingBlow;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class CripplingBlowEvent extends AbilityActivateEvent {

  @Getter @Setter private int duration;
  @Getter @Setter private int slownessDuration;
  @Getter @Setter private int slownessLevel;
  @Getter @Setter private int nauseaDuration;
  @Getter @Setter private int cooldown;

  public CripplingBlowEvent(McRPGPlayer player, CripplingBlow cripplingBlow, int duration, int slownessDuration, int slownessLevel, int nauseaDuration, int cooldown){
    super(cripplingBlow, player, AbilityEventType.COMBAT);
    this.duration = duration;
    this.slownessDuration = slownessDuration;
    this.slownessLevel = slownessLevel;
    this.nauseaDuration = nauseaDuration;
    this.cooldown = cooldown;
  }
}
