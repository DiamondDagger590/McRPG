package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.RedeemType;

public class ChatEvent implements Listener {

  @EventHandler
  public void chatEvent(AsyncPlayerChatEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      e.setCancelled(true);
      return;
    }
    McRPGPlayer mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    if(mp.isListenForCustomExpInput()){
      e.setCancelled(true);
      mp.setListenForCustomExpInput(false);
      String[] args = e.getMessage().split(" ");
      if(args.length != 1){
        e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.NotAnInt")));
        return;
      }
      else{
        if(Methods.isInt(args[0])){
          if(mp.getRedeemBit() == null){
            e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.UnexpectedError")));
            return;
          }
          RedeemBit redeemBit = mp.getRedeemBit();
          int amount = Integer.parseInt(args[0]);
          if(redeemBit.getRedeemType() == RedeemType.EXP){
            if(mp.getRedeemableExp() < amount){
              e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.NotEnoughExp")));
              return;
            }
            else{
              mp.getSkill(redeemBit.getSkill()).giveExp(mp, amount, GainReason.REDEEM);
              mp.setRedeemableExp(mp.getRedeemableExp() - amount);
              e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedExp")
                      .replace("%Skill%", redeemBit.getSkill().getName()).replace("%Amount%", Integer.toString(amount))));
              return;
            }
          }
          else{
            if(mp.getRedeemableLevels() < amount){
              e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.NotEnoughLevels")));
              return;            }
            else{
              mp.getSkill(redeemBit.getSkill()).giveLevels(mp, amount, McRPG.getInstance().getConfig().getBoolean("Configuration.RedeemLevelsResetExp"));
              mp.setRedeemableLevels(mp.getRedeemableLevels() - amount);
              e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedLevels")
                      .replace("%Skill%", redeemBit.getSkill().getName()).replace("%Amount%", Integer.toString(amount))));
              return;
            }
          }
        }
        else{
          e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getConfig().getString("Messages.Commands.Utility.NotAnInt")));
          return;
        }
      }
    }
  }
}
