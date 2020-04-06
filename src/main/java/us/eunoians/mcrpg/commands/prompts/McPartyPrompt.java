package us.eunoians.mcrpg.commands.prompts;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class McPartyPrompt implements TabCompleter{
  
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String commandLable, String[] args){
    if(!McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getBoolean("PartiesEnabled", false)){
      return null;
    }
    List<String> completions = new ArrayList<>();
    Player p = (Player) sender;
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(p.getUniqueId());
    }catch(McRPGPlayerNotFoundException e){
      mp = new McRPGPlayer(p.getUniqueId());
    }
    Party party = mp.getPartyID() != null ? McRPG.getInstance().getPartyManager().getParty(mp.getPartyID()) : null;
    PartyMember partyMember = party != null ? party.getPartyMember(p.getUniqueId()) : null;
    boolean isInParty = party != null;
    if(args.length == 1){
      completions.add("invites");
      if(isInParty){
        completions.add("leave");
        completions.add("tpa");
        completions.add("tpaccept");
        completions.add("chat");
        PartyRoles partyRole = partyMember.getPartyRole();
        if(partyRole.getId() <= party.getRoleForPermission(PartyPermissions.INVITE_PLAYERS).getId()){
          completions.add("invite");
        }
        if(partyRole.getId() <= party.getRoleForPermission(PartyPermissions.KICK_PLAYERS).getId()){
          completions.add("kick");
        }
        if(partyRole.getId() <= party.getRoleForPermission(PartyPermissions.PRIVATE_BANK).getId()){
          completions.add("storage");
        }
        if(partyRole == PartyRoles.OWNER){
          completions.add("disband");
          completions.add("promote");
          completions.add("demote");
          completions.add("setowner");
          completions.add("roles");
          completions.add("rename");
        }
      }
      else{
        completions.add("create");
      }
      return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
    }
    else if(args.length == 2){
      if(isInParty){
        if(partyMember.getPartyRole() == PartyRoles.OWNER){
          switch(args[0].toLowerCase()){
            case "promote":
              for(PartyMember member : party.getAllMembers()){
                if(member.getPartyRole() == PartyRoles.MEMBER){
                  completions.add(Bukkit.getOfflinePlayer(member.getUuid()).getName());
                }
              }
              break;
            case "demote":
              for(PartyMember member : party.getAllMembers()){
                if(member.getPartyRole() == PartyRoles.MOD){
                  completions.add(Bukkit.getOfflinePlayer(member.getUuid()).getName());
                }
              }
              break;
            case "setowner":
              for(PartyMember member : party.getAllMembers()){
                if(!member.getUuid().equals(p.getUniqueId())){
                  completions.add(Bukkit.getOfflinePlayer(member.getUuid()).getName());
                }
              }
              break;
          }
        }
        if(partyMember.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.KICK_PLAYERS).getId()){
          switch(args[0].toLowerCase()){
            case "kick":
              for(UUID uuid : party.getAllMemberUUIDs()){
                if(!uuid.equals(p.getUniqueId())){
                  completions.add(Bukkit.getOfflinePlayer(uuid).getName());
                }
              }
              break;
          }
        }
        if(partyMember.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.INVITE_PLAYERS).getId()){
          switch(args[0].toLowerCase()){
            case "invite":
              for(Player player : Bukkit.getOnlinePlayers()){
                if(!party.isPlayerInParty(player.getUniqueId())){
                  try{
                    McRPGPlayer mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
                    if(mcRPGPlayer.getPartyID() == null){
                      completions.add(player.getName());
                    }
                  }catch(McRPGPlayerNotFoundException e){
                  }
                }
              }
              break;
          }
        }
        if(args[0].equalsIgnoreCase("tpa") || args[0].equalsIgnoreCase("tpahere")){
          for(UUID uuid : party.getAllMemberUUIDs()){
            if(!uuid.equals(p.getUniqueId())){
              completions.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
          }
        }
      }
      return StringUtil.copyPartialMatches(args[1], completions, new ArrayList<>());
    }
    return null;
  }
}