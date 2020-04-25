package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.RedeemType;

public class ChatEvent implements Listener {

  @EventHandler
  public void chatEvent(AsyncPlayerChatEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId()) && !PlayerManager.isPlayerStored(e.getPlayer().getUniqueId())){
      return;
    }
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    }
    catch(McRPGPlayerNotFoundException exception){
      return;
    }
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
              final int fAmount = amount;
              new BukkitRunnable(){
                @Override
                public void run(){
                  mp.getSkill(redeemBit.getSkill()).giveExp(mp, fAmount, GainReason.REDEEM);
                  mp.setRedeemableExp(mp.getRedeemableExp() - fAmount);
                  e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedExp")
                          .replace("%Skill%", redeemBit.getSkill().getName()).replace("%Amount%", Integer.toString(fAmount))));
                }
              }.runTaskLater(McRPG.getInstance(), 5);
              return;
            }
          }
          else{
            if(mp.getRedeemableLevels() < amount){
              e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.NotEnoughLevels")));
              return;
            }
            else{
              if(amount + mp.getSkill(redeemBit.getSkill()).getCurrentLevel() > mp.getSkill(redeemBit.getSkill()).getType().getMaxLevel()){
                amount = mp.getSkill(redeemBit.getSkill()).getType().getMaxLevel() - mp.getSkill(redeemBit.getSkill()).getCurrentLevel();
              }
              final int fAmount = amount;
              new BukkitRunnable(){
                @Override
                public void run(){
                  mp.getSkill(redeemBit.getSkill()).giveLevels(mp, fAmount, McRPG.getInstance().getConfig().getBoolean("Configuration.RedeemLevelsResetExp"));
                  mp.setRedeemableLevels(mp.getRedeemableLevels() - fAmount);
                  e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedLevels")
                          .replace("%Skill%", redeemBit.getSkill().getName()).replace("%Amount%", Integer.toString(fAmount))));
                }
              }.runTaskLater(McRPG.getInstance(), 5);
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
    else if(mp.isUsePartyChat()){
      if(mp.getPartyID() != null){
        Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
        String message = Methods.color(McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getString("PartyChatPrefix").replace("%Player_Name%", e.getPlayer().getName()))+ e.getMessage();
        if(party != null){
          Bukkit.getConsoleSender().sendMessage(message);
          for(Player player : Bukkit.getOnlinePlayers()){
            if(party.isPlayerInParty(player.getUniqueId()) || mp.isSpyPartyChat()){
              player.sendMessage(message);
            }
          }
          e.setCancelled(true);
        }
      }
      else{
        mp.setUsePartyChat(false);
        e.setCancelled(true);
        e.getPlayer().sendMessage(Methods.color(e.getPlayer(), McRPG.getInstance().getPluginPrefix() + "&cYou are no longer in a party so party chat is now disabled."));
        return;
      }
    }
  }
}
