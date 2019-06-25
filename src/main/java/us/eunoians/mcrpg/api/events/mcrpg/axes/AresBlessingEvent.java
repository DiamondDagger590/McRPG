package us.eunoians.mcrpg.api.events.mcrpg.axes;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.axes.AresBlessing;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class AresBlessingEvent extends AbilityActivateEvent {

  @Getter @Setter private int strengthDuration;
  @Getter @Setter private int strengthLevel;
  @Getter @Setter private int resistanceDuration;
  @Getter @Setter private int resistanceLevel;
  @Getter @Setter private int weaknessDuration;
  @Getter @Setter private int weaknessLevel;
  @Getter @Setter private int miningFatigueDuration;
  @Getter @Setter private int miningFatigueLevel;
  @Getter @Setter private int cooldown;

  public AresBlessingEvent(McRPGPlayer player, AresBlessing aresBlessing, int strengthDuration, int strengthLevel, int resistanceDuration,
                           int resistanceLevel, int weaknessDuration, int weaknessLevel, int miningFatigueDuration, int miningFatigueLevel,
                           int cooldown){
    super(aresBlessing, player, AbilityEventType.COMBAT);
    this.strengthDuration = strengthDuration;
    this.strengthLevel = strengthLevel;
    this.resistanceDuration = resistanceDuration;
    this.resistanceLevel = resistanceLevel;
    this.weaknessDuration = weaknessDuration;
    this.weaknessLevel = weaknessLevel;
    this.miningFatigueDuration = miningFatigueDuration;
    this.miningFatigueLevel = miningFatigueLevel;
    this.cooldown = cooldown;
  }
}
