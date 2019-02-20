package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.GainReason;

public class McRPGPlayerExpGainEvent extends PlayerModifiedEvent {

  @Getter
  private int expGained;
  @Getter
  private Skill skillGained;
  @Getter
  private GainReason gainType;

  public McRPGPlayerExpGainEvent(McRPGPlayer mcRPGPlayer, int expGained, Skill skillGained, GainReason gainType){
    super(mcRPGPlayer);
    this.expGained = expGained;
    this.skillGained = skillGained;
    this.gainType = gainType;
  }

  public McRPGPlayer getMcMMOPlayer(){
    return skillGained.getPlayer();
  }
}