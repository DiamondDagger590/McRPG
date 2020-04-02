package us.eunoians.mcrpg.party;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface TeleportFunction{
  
  boolean teleportPlayer(Player recipient, Player sender);
}
