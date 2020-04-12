package us.eunoians.mcrpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.exceptions.PartyNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.gui.GUITracker;
import us.eunoians.mcrpg.gui.PartyMainGUI;
import us.eunoians.mcrpg.gui.PartyPrivateBankGUI;
import us.eunoians.mcrpg.gui.PartyRoleGUI;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyInvite;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.party.TeleportRequest;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;
import us.eunoians.mcrpg.types.PartyUpgrades;

import java.util.UUID;

public class McParty implements CommandExecutor{
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    String pluginPrefix = McRPG.getInstance().getPluginPrefix();
    if(!McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getBoolean("PartiesEnabled", false)){
      sender.sendMessage(Methods.color(pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartiesDisabled")));
      return true;
    }
    if(sender instanceof Player){
      Player p = (Player) sender;
      if(PlayerManager.isPlayerFrozen(p.getUniqueId())){
        return true;
      }
      //Disabled Worlds
      String world = p.getWorld().getName();
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
           McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)){
        return true;
      }
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(p.getUniqueId());
      }catch(McRPGPlayerNotFoundException e){
        e.printStackTrace();
        return true;
      }
      if(args.length == 0){
        if(mp.getPartyID() == null){
          if(mp.getPartyInvites().isEmpty()){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoPendingInvites")));
            return true;
          }
          else{
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.WaitingPartyInvites")));
            return true;
          }
        }
        Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
        PartyMainGUI partyMainGUI = new PartyMainGUI(mp);
        p.openInventory(partyMainGUI.getGui().getInv());
        return true;
      }
      else{
        //TODO rework this and move to a gui
        if(args[0].equalsIgnoreCase("invites")){
          if(args.length == 1){
            if(mp.getPartyInvites().isEmpty()){
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoPendingInvites")));
              return true;
            }
            else{
              PartyInvite partyInvite = mp.getPartyInvites().elements().nextElement();
              Party party = McRPG.getInstance().getPartyManager().getParty(partyInvite.getPartyID());
              if(party == null){
                try{
                  mp.getPartyInvites().dequeue();
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullPartyInviteRemoved")));
                }catch(InterruptedException e){
                  e.printStackTrace();
                }
                return true;
              }
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PendingPartyInvite").replace("%Party%", party.getName())));
              return true;
            }
          }
          else{
            if(args[1].equalsIgnoreCase("accept")){
              if(mp.getPartyID() != null){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.AlreadyInParty")));
                return true;
              }
              if(mp.getPartyInvites().isEmpty()){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoPendingInvites")));
                return true;
              }
              else{
                PartyInvite partyInvite = mp.getPartyInvites().elements().nextElement();
                Party party = McRPG.getInstance().getPartyManager().getParty(partyInvite.getPartyID());
                if(party == null){
                  try{
                    mp.getPartyInvites().dequeue();
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullPartyInviteRemoved")));
                  }catch(InterruptedException e){
                    e.printStackTrace();
                  }
                  return true;
                }
                else{
                  if(party.getAllMemberUUIDs().size() >= PartyUpgrades.getMemberCountAtTier(party.getUpgradeTier(PartyUpgrades.MEMBER_COUNT))){
                    try{
                      mp.getPartyInvites().dequeue();
                      p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.FullPartyInvite")));
                    }catch(InterruptedException e){
                      e.printStackTrace();
                    }
                    return true;
                  }
                  else{
                    party.addPlayer(p.getUniqueId());
                    party.saveParty();
                    try{
                      mp.getPartyInvites().dequeue();
                    }catch(InterruptedException e){
                      e.printStackTrace();
                    }
                    mp.setPartyID(party.getPartyID());
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyJoined").replace("%Party%", party.getName())));
                    for(UUID uuid : party.getAllMemberUUIDs()){
                      if(uuid.equals(p.getUniqueId())){
                        continue;
                      }
                      OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                      if(offlinePlayer.isOnline()){
                        ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerHasJoinedParty").replace("%Player%", p.getName())));
                      }
                    }
                    return true;
                  }
                }
              }
            }
            else if(args[1].equalsIgnoreCase("decline")){
              if(mp.getPartyInvites().isEmpty()){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoPendingInvites")));
                return true;
              }
              try{
                Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyInvites().dequeue().getPartyID());
                if(party == null){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullPartyInviteRemoved")));
                }
                else{
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.InviteDeclined").replace("%Party%", party.getName())));
                }
              }catch(InterruptedException e){
                e.printStackTrace();
              }
              return true;
            }
          }
        }
        // /mcparty invite player name
        else if(args[0].equalsIgnoreCase("invite")){
          if(args.length < 2){
            sendHelpMessage(p);
            return true;
          }
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            else{
              PartyMember partyPlayer = party.getPartyMember(p.getUniqueId());
              if(partyPlayer.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.INVITE_PLAYERS).getId()){
                if(party.getAllMemberUUIDs().size() >= PartyUpgrades.getMemberCountAtTier(party.getUpgradeTier(PartyUpgrades.MEMBER_COUNT))){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.FullParty")));
                  return true;
                }
                else{
                  if(Methods.hasPlayerLoggedInBefore(args[1])){
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if(offlinePlayer.isOnline()){
                      try{
                        PlayerManager.getPlayer(offlinePlayer.getUniqueId()).getPartyInvites().enqueue(new PartyInvite(party.getPartyID(), offlinePlayer.getUniqueId()));
                        p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.InvitedPlayer")));
                        ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.BeenInvited").replace("%Player%", p.getName())));
                      }catch(McRPGPlayerNotFoundException e){
                        p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.IssueInviting")));
                      }
                      return true;
                    }
                    else{
                      p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                      return true;
                    }
                  }
                  else{
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                    return true;
                  }
                }
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoInvitePermission")));
                return true;
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("create")){
          if(mp.getPartyID() != null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.AlreadyInParty")));
            return true;
          }
          else{
            if(args.length < 2){
              sendHelpMessage(p);
              return true;
            }
            if(McRPG.getInstance().getPartyManager().isPartyNameUsed(args[1])){
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyNameInUse")));
              return true;
            }
            String lowerCaseName = args[1].toLowerCase();
            for(String s : McRPG.getInstance().getFileManager().getFile(FileManager.Files.FILTER).getStringList("BannedPartyStrings")){
              if(lowerCaseName.contains(s.toLowerCase())){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.InvalidPartyName")));
                return true;
              }
            }
            Party party = McRPG.getInstance().getPartyManager().addParty(args[1], p.getUniqueId());
            mp.setPartyID(party.getPartyID());
            mp.saveData();
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyCreated").replace("%Party%", party.getName())));
            return true;
          }
        }
        else if(args[0].equalsIgnoreCase("kick")){
          if(args.length < 2){
            sendHelpMessage(p);
            return true;
          }
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
          if(party == null){
            mp.setPartyID(null);
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
            return true;
          }
          else{
            PartyMember partyPlayer = party.getPartyMember(p.getUniqueId());
            if(partyPlayer.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.KICK_PLAYERS).getId()){
              if(Methods.hasPlayerLoggedInBefore(args[1])){
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(offlinePlayer.getUniqueId().equals(p.getUniqueId())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantKickSelf")));
                  return true;
                }
                if(!party.isPlayerInParty(offlinePlayer.getUniqueId())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
                  return true;
                }
                PartyMember playerToKick = party.getPartyMember(offlinePlayer.getUniqueId());
                //This lets members kick other members if for some reason people decide to let members have kicking power. I don't control the people
                if(playerToKick.getPartyRole().getId() <= partyPlayer.getPartyRole().getId() && party.getRoleForPermission(PartyPermissions.KICK_PLAYERS) != PartyRoles.MEMBER){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantKickPlayer")));
                  return true;
                }
                boolean kicked = party.kickPlayer(offlinePlayer.getUniqueId());
                if(!kicked){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.IssueKicking")));
                  return true;
                }
                party.saveParty();
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.KickedPlayer").replace("%Player%", offlinePlayer.getName())));
                try{
                  McRPGPlayer target = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                  target.setPartyID(null);
                  target.emptyTeleportRequests();
                  target.saveData();
                }catch(McRPGPlayerNotFoundException e){
                  McRPGPlayer target = new McRPGPlayer(offlinePlayer.getUniqueId());
                  target.setPartyID(null);
                  target.saveData();
                }
                if(offlinePlayer.isOnline()){
                  ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.KickedFromParty").replace("%Party%", party.getName())));
                }
                return true;
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                return true;
              }
            }
            else{
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoKickPermissions")));
              return true;
            }
          }
        }
        else if(args[0].equalsIgnoreCase("disband")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            else{
              PartyMember partyMember = party.getPartyMember(p.getUniqueId());
              if(partyMember.getPartyRole() != PartyRoles.OWNER){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantDisband")));
                return true;
              }
              else{
                McRPG.getInstance().getPartyManager().removeParty(party.getPartyID());
                for(UUID uuid : party.getAllMemberUUIDs()){
                  if(!uuid.equals(p.getUniqueId())){
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    if(offlinePlayer.isOnline()){
                      ((Player) offlinePlayer).sendMessage(Methods.color(pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyWasDisbanded")));
                    }
                  }
                }
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.DisbandedParty")));
                return true;
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("leave")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            else{
              if(party.getAllMemberUUIDs().size() == 1){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.DisbandedParty")));
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.LeftParty")));
              }
              mp.setPartyID(null);
              mp.emptyTeleportRequests();
              party.kickPlayer(p.getUniqueId());
              for(UUID uuid : party.getAllMemberUUIDs()){
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if(offlinePlayer.isOnline()){
                  ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerLeftParty").replace("%Player%", p.getName())));
                }
              }
            }
          }
          return true;
        }
        else if(args[0].equalsIgnoreCase("promote")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length < 2){
              sendHelpMessage(p);
              return true;
            }
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole() == PartyRoles.OWNER){
              if(Methods.hasPlayerLoggedInBefore(args[1])){
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(offlinePlayer.getUniqueId().equals(p.getUniqueId())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantSelfPromote")));
                  return true;
                }
                if(party.isPlayerInParty(offlinePlayer.getUniqueId())){
                  PartyMember target = party.getPartyMember(offlinePlayer.getUniqueId());
                  //Handle possible edge case where there are two owners
                  if(target.getPartyRole() == PartyRoles.OWNER){
                    target.setPartyRole(PartyRoles.MOD);
                  }
                  if(target.getPartyRole() == PartyRoles.MOD){
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantPromoteMod")));
                  }
                  else{
                    target.setPartyRole(PartyRoles.MOD);
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PromotedPlayer").replace("%Player%", offlinePlayer.getName())));
                    if(offlinePlayer.isOnline()){
                      ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.BeenPromoted")));
                    }
                  }
                  return true;
                }
                else{
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotInParty")));
                  return true;
                }
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                return true;
              }
            }
            else{
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantPromote")));
              return true;
            }
          }
        }
        else if(args[0].equalsIgnoreCase("demote")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length < 2){
              sendHelpMessage(p);
              return true;
            }
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole() == PartyRoles.OWNER){
              if(Methods.hasPlayerLoggedInBefore(args[1])){
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(offlinePlayer.getUniqueId().equals(p.getUniqueId())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantSelfDemote")));
                  return true;
                }
                if(party.isPlayerInParty(offlinePlayer.getUniqueId())){
                  PartyMember target = party.getPartyMember(offlinePlayer.getUniqueId());
                  //Handle possible edge case where there are two owners
                  if(target.getPartyRole() == PartyRoles.OWNER){
                    target.setPartyRole(PartyRoles.MOD);
                  }
                  if(target.getPartyRole() == PartyRoles.MEMBER){
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantDemoteMember")));
                  }
                  else{
                    target.setPartyRole(PartyRoles.MEMBER);
                    party.saveParty();
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantDemoteMember").replace("%Player%", offlinePlayer.getName())));
                    if(offlinePlayer.isOnline()){
                      ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix +  McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.BeenDemoted")));
                    }
                  }
                  return true;
                }
                else{
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotInParty")));
                  return true;
                }
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                return true;
              }
            }
            else{
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantDemote")));
              return true;
            }
          }
        }
        else if(args[0].equalsIgnoreCase("setowner")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
            return true;
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length < 2){
              sendHelpMessage(p);
              return true;
            }
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole() == PartyRoles.OWNER){
              if(Methods.hasPlayerLoggedInBefore(args[1])){
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if(offlinePlayer.getUniqueId().equals(p.getUniqueId())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantSelfPromote")));
                  return true;
                }
                if(party.isPlayerInParty(offlinePlayer.getUniqueId())){
                  PartyMember target = party.getPartyMember(offlinePlayer.getUniqueId());
                  //Handle possible edge case where there are two owners
                  if(target.getPartyRole() == PartyRoles.OWNER){
                    target.setPartyRole(PartyRoles.MOD);
                  }
                  partyMember.setPartyRole(PartyRoles.MOD);
                  target.setPartyRole(PartyRoles.OWNER);
                  party.saveParty();
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.SetPlayerOwner").replace("%Player%", offlinePlayer.getName())));
                  if(offlinePlayer.isOnline()){
                    ((Player) offlinePlayer).sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.BeenSetOwner")));
                  }
                }
                else{
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotInParty")));
                }
                return true;
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                return true;
              }
            }
            else{
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantPromote")));
              return true;
            }
          }
        }
        else if(args[0].equalsIgnoreCase("storage")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.PRIVATE_BANK).getId()){
              try{
                PartyPrivateBankGUI partyPrivateBankGUI = new PartyPrivateBankGUI(mp);
                p.openInventory(partyPrivateBankGUI.getGui().getInv());
                GUITracker.trackPlayer(p, partyPrivateBankGUI);
              }catch(PartyNotFoundException e){
                mp.setPartyID(null);
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              }
            }
            else{
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantAccessBank").replace("%Bank_Role%", party.getRoleForPermission(PartyPermissions.PRIVATE_BANK).getName())));
            }
          }
          return true;
        }
        else if(args[0].equalsIgnoreCase("roles")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole() != PartyRoles.OWNER){
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantEditRoles")));
              return true;
            }
            else{
              PartyRoleGUI partyRoleGUI = new PartyRoleGUI(mp, party);
              p.openInventory(partyRoleGUI.getGui().getInv());
              GUITracker.trackPlayer(p, partyRoleGUI);
              return true;
            }
          }
        }
        else if(args[0].equalsIgnoreCase("chat")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length == 1){
              mp.setUsePartyChat(!mp.isUsePartyChat());
              if(mp.isUsePartyChat()){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyChatEnabled")));
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyChatDisabled")));
              }
            }
            else{
              StringBuilder message = new StringBuilder(Methods.color(p, McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getString("PartyChatPrefix").replace("%Player_Name%", p.getName())));
              Bukkit.getConsoleSender().sendMessage(message.toString());
              for(int i = 1; i < args.length; i++){
                message.append(args[i]);
              }
              for(Player player : Bukkit.getOnlinePlayers()){
                if(party.isPlayerInParty(player.getUniqueId()) || player.hasPermission("mcrpg.*") || player.hasPermission("mcparty.*") || player.hasPermission("mcadmin.*") || player.hasPermission("mcparty.spy")){
                  player.sendMessage(message.toString());
                }
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("tpahere")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length == 1){
              sendHelpMessage(p);
              return true;
            }
            else{
              OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
              if(!party.isPlayerInParty(offlinePlayer.getUniqueId())){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotInParty")));
                return true;
              }
              else{
                if(!offlinePlayer.isOnline()){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                  return true;
                }
                else{
                  try{
                    McRPGPlayer target = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                    TeleportRequest teleportRequest = new TeleportRequest(offlinePlayer.getUniqueId(), p.getUniqueId(), false);
                    target.addTeleportRequest(teleportRequest);
                    target.getPlayer().sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ReceivedTpahereRequest").replace("%Player%", p.getName())));
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.RequestedTpahere").replace("%Player%", offlinePlayer.getName())));
                    return true;
                  }catch(McRPGPlayerNotFoundException e){
                    return true;
                  }
                }
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("tpa")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(args.length == 1){
              sendHelpMessage(p);
              return true;
            }
            else{
              OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
              if(!party.isPlayerInParty(offlinePlayer.getUniqueId())){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotInParty")));
                return true;
              }
              else{
                if(!offlinePlayer.isOnline()){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                  return true;
                }
                else{
                  try{
                    McRPGPlayer target = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                    TeleportRequest teleportRequest = new TeleportRequest(offlinePlayer.getUniqueId(), p.getUniqueId(), true);
                    target.addTeleportRequest(teleportRequest);
                    target.getPlayer().sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ReceivedTpaRequest").replace("%Player%", p.getName())));
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.RequestedTpa").replace("%Player%", offlinePlayer.getName())));
                    return true;
                  }catch(McRPGPlayerNotFoundException e){
                    return true;
                  }
                }
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("tpaccept")){
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              mp.emptyTeleportRequests();
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            if(mp.getTeleportRequests().size() == 0){
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NoPendingTeleportRequests")));
              return true;
            }
            if(args.length == 1){
              TeleportRequest teleportRequest = mp.getTeleportRequests().get(0);
              if(!teleportRequest.accept()){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.IssueAcceptingRequest")));
                return true;
              }
            }
            else{
              OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
              if(offlinePlayer.isOnline()){
                if(mp.getTeleportRequestMap().containsKey(offlinePlayer.getUniqueId())){
                  TeleportRequest teleportRequest = mp.getTeleportRequestMap().remove(offlinePlayer.getUniqueId());
                  mp.getTeleportRequests().remove(teleportRequest);
                  if(!teleportRequest.accept()){
                    p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.IssueAcceptingRequest")));
                    return true;
                  }
                }
                else{
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerDidNotRequest")));
                  return true;
                }
              }
              else{
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerOffline")));
                return true;
              }
            }
          }
        }
        else if(args[0].equalsIgnoreCase("rename")){
          if(args.length < 2){
            sendHelpMessage(p);
            return true;
          }
          if(mp.getPartyID() == null){
            p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NotInParty")));
          }
          else{
            Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
            if(party == null){
              mp.setPartyID(null);
              mp.emptyTeleportRequests();
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.NullParty")));
              return true;
            }
            else{
              if(party.getPartyMember(p.getUniqueId()).getPartyRole() != PartyRoles.OWNER){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.CantRename")));
                return true;
              }
              if(McRPG.getInstance().getPartyManager().isPartyNameUsed(args[1])){
                p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyNameInUse")));
                return true;
              }
              String lowerCaseName = args[1].toLowerCase();
              for(String s : McRPG.getInstance().getFileManager().getFile(FileManager.Files.FILTER).getStringList("BannedPartyStrings")){
                if(lowerCaseName.contains(s.toLowerCase())){
                  p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.InvalidPartyName")));
                  return true;
                }
              }
              party.setName(lowerCaseName);
              p.sendMessage(Methods.color(p, pluginPrefix + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.RenamedParty")));
              return true;
            }
          }
        }
      }
      return true;
    }
    else{
      sender.sendMessage(Methods.color("&cYou can not send a party command via console"));
      return true;
    }
  }
  
  private void sendHelpMessage(CommandSender p) {
    McRPG plugin = McRPG.getInstance();
    FileConfiguration config = plugin.getLangFile();
    p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcparty")));
  }
}