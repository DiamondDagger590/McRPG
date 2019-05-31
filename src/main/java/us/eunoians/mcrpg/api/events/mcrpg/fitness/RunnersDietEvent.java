package us.eunoians.mcrpg.api.events.mcrpg.fitness;

import us.eunoians.mcrpg.abilities.fitness.RunnersDiet;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class RunnersDietEvent extends AbilityActivateEvent {

  public RunnersDietEvent(McRPGPlayer mcRPGPlayer, RunnersDiet runnersDiet) {
    super(runnersDiet, mcRPGPlayer, AbilityEventType.RECREATIONAL);
  }
}
