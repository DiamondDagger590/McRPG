package us.eunoians.mcrpg.party;

import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TeleportRequest{
  
  /**
   * The player who sent the request
   */
  @Getter
  private UUID sender;
  
  /**
   * The player who received this request
   */
  @Getter
  private UUID recipient;
  
  /**
   * The time that this request was sent
   */
  @Getter
  private long timeSent;
  
  /**
   * This represents if it is a here or to request. If it is a /mcparty tp %player%, this should be true, otherwise it will be false
   */
  @Getter
  private boolean toRecipient;
  
  public TeleportRequest(UUID recipient, UUID sender, boolean toRecipient){
    this.recipient = recipient;
    this.sender = sender;
    this.toRecipient = toRecipient;
    this.timeSent = Calendar.getInstance().getTimeInMillis();
  }
  
  public boolean accept(){
    OfflinePlayer offlineSender = Bukkit.getOfflinePlayer(sender);
    OfflinePlayer offlineRecipient = Bukkit.getOfflinePlayer(recipient);
    if(offlineRecipient.isOnline() && offlineSender.isOnline()){
      Player onlineSender = (Player) offlineSender;
      Player onlineRecipient = (Player) offlineRecipient;
      TeleportFunction teleportFunction;
      if(toRecipient){
        teleportFunction = (Player recipient, Player sender) -> {
          if(recipient.isOnline() && sender.isOnline()){
            sender.teleport(recipient);
            return true;
          }
          return false;
        };
      }
      else{
        teleportFunction = (Player recipient, Player sender) -> {
          if(recipient.isOnline() && sender.isOnline()){
            recipient.teleport(sender);
            return true;
          }
          return false;
        };
      }
      AtomicInteger iterations = new AtomicInteger(0);
      BukkitTask waitTask = new BukkitRunnable(){
        @Override
        public void run(){
          if(iterations.get() == 0){
            if(!toRecipient){
              onlineSender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TpWasAccepted").replace("%Player%", onlineRecipient.getName())));
              onlineRecipient.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TpAcceptedAndStandStill")));
            }
            else{
              onlineRecipient.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TpAccepted")));
              onlineSender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TpWasAcceptedAndStandStill").replace("%Player%", onlineRecipient.getName())));
            }
          }
          else{
            if(!onlineRecipient.isOnline() || !onlineSender.isOnline()){
              if(!onlineRecipient.isOnline()){
                if(onlineSender.isOnline()){
                  onlineSender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TeleportationCanceled")));
                  cancel();
                  return;
                }
              }
              else{
                if(onlineRecipient.isOnline()){
                  onlineRecipient.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TeleportationCanceled")));
                  cancel();
                  return;
                }
              }
            }
            if(iterations.get() < 6){
              if(toRecipient){
                onlineSender.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Methods.color("&c" + (5 - iterations.get()))));
              }
              else{
                onlineRecipient.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Methods.color("&c" + (5 - iterations.get()))));
              }
            }
            else{
              teleportFunction.teleportPlayer(onlineRecipient, onlineSender);
              cancel();
              try{
                McRPGPlayer mp = toRecipient ? PlayerManager.getPlayer(sender) : PlayerManager.getPlayer(recipient);
                mp.setAcceptedTeleportRequest(null);
              }catch(McRPGPlayerNotFoundException e){
              }
            }
          }
          iterations.incrementAndGet();
        }
      }.runTaskTimer(McRPG.getInstance(), 10, 20);
      
      AcceptedTeleportRequest acceptedTeleportRequest = new AcceptedTeleportRequest(waitTask, this);
      try{
        McRPGPlayer mp = toRecipient ? PlayerManager.getPlayer(sender) : PlayerManager.getPlayer(recipient);
        mp.setAcceptedTeleportRequest(acceptedTeleportRequest);
        return true;
      }catch(McRPGPlayerNotFoundException e){
        return false;
      }
    }
    return false;
  }
  
  @Override
  public boolean equals(Object o){
    if(o instanceof TeleportRequest){
      TeleportRequest teleportRequest = (TeleportRequest) o;
      if(teleportRequest.getRecipient().equals(this.recipient) && teleportRequest.getSender().equals(this.sender) && teleportRequest.getTimeSent() == this.getTimeSent()){
        return true;
      }
    }
    return false;
  }
}
