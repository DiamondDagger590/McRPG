package us.eunoians.mcrpg.api.events.mcrpg.archery;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.archery.CurseOfHades;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class CurseOfHadesEvent extends AbilityActivateEvent {

  @Getter
  @Setter
  private int witherDuration;

  @Getter
  @Setter
  private int witherLevel;

  @Getter
  @Setter
  private int slownessDuration;

  @Getter
  @Setter
  private int slownessLevel;

  @Getter
  @Setter
  private int blindnessDuration;

  @Getter
  @Setter
  private int cooldown;

  public CurseOfHadesEvent(McRPGPlayer mcRPGPlayer, CurseOfHades curseOfHades, int cooldown, int witherDuration, int witherLevel, int slownessDuration, int slownessLevel, int blindnessDuration){
    super(curseOfHades, mcRPGPlayer, AbilityEventType.COMBAT);
    this.cooldown = cooldown;
    this.witherDuration = witherDuration;
    this.witherLevel = witherLevel;
    this.slownessDuration = slownessDuration;
    this.slownessLevel = slownessLevel;
    this.blindnessDuration = blindnessDuration;
  }
}
