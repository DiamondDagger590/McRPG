package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.displays.LeaderboardScoreboard;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.LeaderboardType;
import us.eunoians.mcrpg.types.Skills;

public class McRank implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player player) {

            if (PlayerManager.isPlayerFrozen(player.getUniqueId())) {
                return true;
            }
            String world = player.getWorld().getName();
            //Disabled Worlds
            if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
                return true;
            }
            if (player.hasPermission("mcrpg.*") || player.hasPermission("mcrpg.rank")) {
                McRPGPlayer mcRPGPlayer;
                try {
                    mcRPGPlayer = PlayerManager.getPlayer(player.getUniqueId());
                }
                catch (McRPGPlayerNotFoundException exception) {
                    return true;
                }
                if (args.length == 0) {
                    /*if(McRPG.getInstance().getLeaderboardManager().isLoading(mcRPGPlayer)){
                        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                        return true;
                    }
                    if (McRPG.getInstance().getLeaderboardManager().updateRank(mcRPGPlayer, "power")) {
                        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                        return true;
                    }*/
                    if (DisplayManager.getInstance().doesPlayerHaveDisplay(player)) {
                        DisplayManager.getInstance().getDisplay(player).cancel();
                        DisplayManager.getInstance().removePlayersDisplay(player);
                    }
                    LeaderboardScoreboard leaderboardScoreboard = new LeaderboardScoreboard(mcRPGPlayer, LeaderboardType.POWER, player.getScoreboard(), 15, 1);
                    McRPG.getInstance().getDisplayManager().setGenericDisplay(leaderboardScoreboard);
                    player.setScoreboard(leaderboardScoreboard.getBoard());
                    return true;
                }
                else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("power") || args[0].equalsIgnoreCase("powerlevel")) {
                        /*if (McRPG.getInstance().getLeaderboardManager().isLoading(mcRPGPlayer)) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }
                        if (McRPG.getInstance().getLeaderboardManager().updateRank(mcRPGPlayer, "power")) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }*/
                        if (DisplayManager.getInstance().doesPlayerHaveDisplay(player)) {
                            DisplayManager.getInstance().getDisplay(player).cancel();
                            DisplayManager.getInstance().removePlayersDisplay(player);
                        }
                        LeaderboardScoreboard leaderboardScoreboard = new LeaderboardScoreboard(mcRPGPlayer, LeaderboardType.POWER, player.getScoreboard(), 15, 1);
                        McRPG.getInstance().getDisplayManager().setGenericDisplay(leaderboardScoreboard);
                        player.setScoreboard(leaderboardScoreboard.getBoard());
                        return true;
                    }
                    else if (Skills.isSkill(args[0])) {
                        Skills skill = Skills.fromString(args[0]);
                        /*if (McRPG.getInstance().getLeaderboardManager().isLoading(mcRPGPlayer)) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }
                        if (McRPG.getInstance().getLeaderboardManager().updateRank(mcRPGPlayer, args[0].toLowerCase())) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }*/
                        if (DisplayManager.getInstance().doesPlayerHaveDisplay(player)) {
                            DisplayManager.getInstance().getDisplay(player).cancel();
                            DisplayManager.getInstance().removePlayersDisplay(player);
                        }
                        LeaderboardScoreboard leaderboardScoreboard = new LeaderboardScoreboard(mcRPGPlayer, LeaderboardType.SKILL, player.getScoreboard(), skill, 15, 1);
                        McRPG.getInstance().getDisplayManager().setGenericDisplay(leaderboardScoreboard);
                        player.setScoreboard(leaderboardScoreboard.getBoard());
                        return true;
                    }
                }
                else {
                    if (!Methods.isInt(args[1])) {
                        sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getConfig().getString("Messages.Commands.Utility.NotAnInt")));
                        return true;
                    }
                    else {
                        int page = Integer.parseInt(args[1]);
                        if (args[0].equalsIgnoreCase("power") || args[0].equalsIgnoreCase("powerlevel")) {
                        /*if (McRPG.getInstance().getLeaderboardManager().isLoading(mcRPGPlayer)) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }
                        if (McRPG.getInstance().getLeaderboardManager().updateRank(mcRPGPlayer, "power")) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }*/
                            if (DisplayManager.getInstance().doesPlayerHaveDisplay(player)) {
                                DisplayManager.getInstance().getDisplay(player).cancel();
                                DisplayManager.getInstance().removePlayersDisplay(player);
                            }
                            LeaderboardScoreboard leaderboardScoreboard = new LeaderboardScoreboard(mcRPGPlayer, LeaderboardType.POWER, player.getScoreboard(), 15, page);
                            McRPG.getInstance().getDisplayManager().setGenericDisplay(leaderboardScoreboard);
                            player.setScoreboard(leaderboardScoreboard.getBoard());
                            return true;
                        }
                        else if (Skills.isSkill(args[0])) {
                            Skills skill = Skills.fromString(args[0]);
                        /*if (McRPG.getInstance().getLeaderboardManager().isLoading(mcRPGPlayer)) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }
                        if (McRPG.getInstance().getLeaderboardManager().updateRank(mcRPGPlayer, args[0].toLowerCase())) {
                            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + "&ePlease wait while we load in your data..."));
                            return true;
                        }*/
                            if (DisplayManager.getInstance().doesPlayerHaveDisplay(player)) {
                                DisplayManager.getInstance().getDisplay(player).cancel();
                                DisplayManager.getInstance().removePlayersDisplay(player);
                            }
                            LeaderboardScoreboard leaderboardScoreboard = new LeaderboardScoreboard(mcRPGPlayer, LeaderboardType.SKILL, player.getScoreboard(), skill, 15, page);
                            McRPG.getInstance().getDisplayManager().setGenericDisplay(leaderboardScoreboard);
                            player.setScoreboard(leaderboardScoreboard.getBoard());
                            return true;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}
