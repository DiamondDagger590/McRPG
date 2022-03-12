package us.eunoians.mcrpg.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.displays.ExpDisplayType;
import us.eunoians.mcrpg.api.displays.ExpScoreboardDisplay;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.api.util.books.SkillBookFactory;
import us.eunoians.mcrpg.database.tables.PlayerDataDAO;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.skills.Skill;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.PartyRoles;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static us.eunoians.mcrpg.types.Skills.fromString;
import static us.eunoians.mcrpg.types.Skills.isSkill;
import static us.eunoians.mcrpg.types.Skills.values;

@SuppressWarnings ("Duplicates")
public class McAdmin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        McRPG plugin = McRPG.getInstance();
        FileConfiguration config = plugin.getLangFile();
        if (sender instanceof Player) {
            Player admin = (Player) sender;
            //Disabled worlds
            String world = admin.getWorld().getName();
            if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                    McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
                return true;
            }
            if (args.length < 3) {
                sendHelpMessage(admin);
                return true;
            }
            else {
                if (args[0].equalsIgnoreCase("give")) {
                    if (args[1].equalsIgnoreCase("abilitypoints")) {
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.points"))) {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                mp.setAbilityPoints(mp.getAbilityPoints() + amount);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.AbilityPoints").replace("%Amount%", args[3])));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.setAbilityPoints(mp.getAbilityPoints() + amount);
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("exp")) {
                        //If the command does not include the skill parameter
                        if (args.length < 5) {
                            if (args.length < 4) {
                                sendHelpMessage(admin);
                                return true;
                            }
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.exp"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                if (!Methods.isInt(args[3])) {
                                    admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                    return true;
                                }
                                else {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        mp.setRedeemableExp(mp.getRedeemableExp() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        admin.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableExp").replace("%Amount%", args[3]).replace("%Player%", ((Player) offlinePlayer).getDisplayName())));
                                        ((Player) offlinePlayer).sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.RedeemableExp").replace("%Amount%", args[3])));
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        mp.setRedeemableExp(mp.getRedeemableExp() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        admin.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableExp").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                        return true;
                                    }
                                }
                            }
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            if (!isSkill(args[4])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.exp"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            Skills skill = fromString(args[4]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill s = mp.getSkill(skill);
                                s.giveExp(mp, amount, GainReason.COMMAND);
                                s.updateExpToLevel();
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        expDisplayType.sendUpdate(s.getCurrentExp(), s.getExpToLevel(), s.getCurrentLevel(), amount);
                                    }
                                }
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Exp")
                                    .replace("%Amount%", args[3]).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.getSkill(skill).giveExp(mp, amount, GainReason.COMMAND);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("level")) {
                        if (args.length < 5) {
                            if (args.length < 4) {
                                sendHelpMessage(admin);
                                return true;
                            }
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.level"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                if (!Methods.isInt(args[3])) {
                                    admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                    return true;
                                }
                                else {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        mp.setRedeemableLevels(mp.getRedeemableLevels() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        admin.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableLevels").replace("%Amount%", args[3]).replace("%Player%", ((Player) offlinePlayer).getDisplayName())));
                                        ((Player) offlinePlayer).sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.RedeemableLevels").replace("%Amount%", args[3])));
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        mp.setRedeemableLevels(mp.getRedeemableLevels() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        admin.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableLevels").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                        return true;
                                    }
                                }
                            }
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            if (!isSkill(args[4])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.level"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            Skills skill = fromString(args[4]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill s = mp.getSkill(skill);
                                s.giveLevels(mp, amount, true);
                                s.updateExpToLevel();
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        expDisplayType.sendUpdate(s.getCurrentExp(), s.getExpToLevel(), s.getCurrentLevel(), 0);
                                    }
                                }
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Level")
                                    .replace("%Amount%", args[3]).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.getSkill(skill).giveLevels(mp, amount, true);
                                mp.getSkill(skill).updateExpToLevel();

                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("ability")) {
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!UnlockedAbilities.isAbility(args[3])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                                return true;
                            }
                            if (!(admin.hasPermission("admin.*") || admin.hasPermission("admin.give.*") || admin.hasPermission("admin.give.ability"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                if (!ability.isPassiveAbility() && mp.doesPlayerHaveActiveAbilityFromSkill(ability.getSkill())) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.HasActive")));
                                    return true;
                                }
                                if (mp.getAbilityLoadout().size() == McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities")) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.LoadoutFull")));
                                    return true;
                                }
                                if (mp.getAbilityLoadout().contains(ability)) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AlreadyHave")));
                                    return true;
                                }
                                BaseAbility baseAbility = mp.getBaseAbility(ability);
                                if (!baseAbility.isUnlocked()) {
                                    baseAbility.setCurrentTier(1);
                                }
                                if (baseAbility.getCurrentTier() == 0) {
                                    baseAbility.setCurrentTier(1);
                                }
                                baseAbility.setUnlocked(true);
                                baseAbility.setToggled(true);
                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).updateBlocks();
                                }
                                mp.getAbilityLoadout().add(ability);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Ability").replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                if (!ability.isPassiveAbility() && mp.doesPlayerHaveActiveAbilityFromSkill(ability.getSkill())) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.HasActive")));
                                    return true;
                                }
                                BaseAbility baseAbility = mp.getBaseAbility(ability);
                                if (!baseAbility.isUnlocked()) {
                                    baseAbility.setCurrentTier(1);
                                }
                                baseAbility.setUnlocked(true);
                                baseAbility.setToggled(true);
                                mp.getAbilityLoadout().add(ability);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("book")) {
                        //mcadmin give book %type% %player%
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[3])) {
                            if (!(args[2].equalsIgnoreCase("unlock") || args[2].equalsIgnoreCase("upgrade"))) {
                                sendHelpMessage(admin);
                                return true;
                            }
                            if (!(admin.hasPermission("admin.*") || admin.hasPermission("admin.give.*") || admin.hasPermission("admin.give.book"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                            if (!offlinePlayer.isOnline()) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerOffline")));
                                return true;
                            }
                            else {
                                Player p = (Player) offlinePlayer;
                                ItemStack book = args[2].equalsIgnoreCase("unlock") ? SkillBookFactory.generateUnlockBook() : SkillBookFactory.generateUpgradeBook();
                                p.getWorld().dropItemNaturally(p.getLocation(), book);
                                p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Book").replace("%Player%", offlinePlayer.getName()).replace("%Type%", args[2])));
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Book").replace("%Player%", offlinePlayer.getName()).replace("%Type%", args[2])));
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else {
                        sendHelpMessage(admin);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("replace")) {
                    if (args.length < 4) {
                        sendHelpMessage(admin);
                        return true;
                    }
                    if (Methods.hasPlayerLoggedInBefore(args[1])) {
                        if (!UnlockedAbilities.isAbility(args[2]) && !UnlockedAbilities.isAbility(args[3])) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                            return true;
                        }
                        else {
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.replace.*") || admin.hasPermission("mcadmin.replace.ability"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            UnlockedAbilities old = UnlockedAbilities.fromString(args[2]);
                            UnlockedAbilities newAbility = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                if (!mp.getAbilityLoadout().contains(old)) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                        .replace("%Ability%", old.getName())));
                                    return true;
                                }
                                else {
                                    BaseAbility ab = mp.getBaseAbility(newAbility);
                                    if (!ab.isUnlocked()) {
                                        ab.setCurrentTier(1);
                                    }
                                    ab.setToggled(true);
                                    ab.setUnlocked(true);
                                    mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced").replace("%Player%", offlinePlayer.getName())
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Replaced")
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    return true;
                                }
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                if (!mp.getAbilityLoadout().contains(old)) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                        .replace("%Ability%", old.getName())));
                                    return true;
                                }
                                else {
                                    BaseAbility ab = mp.getBaseAbility(newAbility);
                                    if (!ab.isUnlocked()) {
                                        ab.setCurrentTier(1);
                                    }
                                    ab.setToggled(true);
                                    ab.setUnlocked(true);
                                    mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced")
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    return true;
                                }
                            }
                        }
                    }
                    else {
                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("remove")) {
                    if (Methods.hasPlayerLoggedInBefore(args[1])) {
                        if (!UnlockedAbilities.isAbility(args[2])) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                            return true;
                        }
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.remove.*") || admin.hasPermission("mcadmin.remove.ability"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                            return true;
                        }
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        UnlockedAbilities ability = UnlockedAbilities.fromString(args[2]);
                        if (offlinePlayer.isOnline()) {
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException exception) {
                                return true;
                            }
                            if (!mp.getAbilityLoadout().contains(ability)) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                    .replace("%Ability%", ability.getName())));
                                return true;
                            }
                            else {
                                mp.getAbilityLoadout().remove(ability);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
                                    .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Removed.Ability")
                                    .replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            if (!mp.getAbilityLoadout().contains(ability)) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                    .replace("%Ability%", ability.getName())));
                                return true;
                            }
                            else {
                                mp.getAbilityLoadout().remove(ability);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
                                    .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                    }
                    else {
                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        return true;
                    }

                }
                else if (args[0].equalsIgnoreCase("view")) {
                    if (args[1].equalsIgnoreCase("loadout")) {
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.view.*") || admin.hasPermission("mcadmin.view.loadout"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                if (offlinePlayer.isOnline()) {
                                    McRPGPlayer mp;
                                    try {
                                        mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                    }
                                    catch (McRPGPlayerNotFoundException exception) {
                                        return true;
                                    }
                                    admin.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
                                    mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(admin::sendMessage);
                                    return true;
                                }
                                else {
                                    McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                    admin.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
                                    mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(admin::sendMessage);
                                    return true;
                                }
                            }
                            else {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else if (isSkill(args[1])) {
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.view.*") || admin.hasPermission("mcadmin.view." + fromString(args[1])
                            .getName().toLowerCase()))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                Skills skill = fromString(args[1]);
                                if (offlinePlayer.isOnline()) {
                                    McRPGPlayer mp;
                                    try {
                                        mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                    }
                                    catch (McRPGPlayerNotFoundException exception) {
                                        return true;
                                    }
                                    Skill skillInfo = mp.getSkill(skill);
                                    admin.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
                                    admin.sendMessage(Methods.color("&eCurrent Level: " + skillInfo.getCurrentLevel()));
                                    admin.sendMessage(Methods.color("&eCurrent Exp: " + skillInfo.getCurrentExp()));
                                    admin.sendMessage(Methods.color("&eExp To Level: " + skillInfo.getExpToLevel()));
                                    skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(admin::sendMessage);
                                    return true;
                                }
                                else {
                                    McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                    Skill skillInfo = mp.getSkill(skill);
                                    admin.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
                                    admin.sendMessage(Methods.color("&eCurrent Level: " + skillInfo.getCurrentLevel()));
                                    admin.sendMessage(Methods.color("&eCurrent Exp: " + skillInfo.getCurrentExp()));
                                    admin.sendMessage(Methods.color("&eExp To Level: " + skillInfo.getExpToLevel()));
                                    skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(admin::sendMessage);
                                    return true;
                                }
                            }
                            else {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else {
                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("cooldown")) {
                    if (args[1].equalsIgnoreCase("set")) {
                        if (args.length < 5) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (!UnlockedAbilities.isAbility(args[3])) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility()) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (!Methods.isInt(args[4])) {
                                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
                                        return true;
                                    }
                                    else {
                                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.set"))) {
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                            return true;
                                        }
                                        int cooldown = Integer.parseInt(args[4]);
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.SECOND, cooldown);
                                        if (offlinePlayer.isOnline()) {
                                            McRPGPlayer mp;
                                            try {
                                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                            }
                                            catch (McRPGPlayerNotFoundException exception) {
                                                return true;
                                            }
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.WasSet")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
                                            mp.saveData();
                                            return true;
                                        }
                                        else {
                                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            mp.saveData();
                                            return true;
                                        }
                                    }
                                }
                            }
                            else {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else if (args[1].equalsIgnoreCase("remove")) {
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (args[3].equalsIgnoreCase("replace")) {
                            if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.remove"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                if (mp.getEndTimeForReplaceCooldown() <= 0) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.NoReplaceCooldown")
                                        .replace("%Player%", offlinePlayer.getName())));
                                    return true;
                                }
                                mp.setEndTimeForReplaceCooldown(0);
                                mp.updateCooldowns();
                                mp.saveData();
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.ReplaceRemove")
                                    .replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.ReplaceRemoved")));
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                if (mp.getEndTimeForReplaceCooldown() <= 0) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.NoReplaceCooldown")
                                        .replace("%Player%", offlinePlayer.getName())));
                                    return true;
                                }
                                mp.setEndTimeForReplaceCooldown(0);
                                mp.updateCooldowns();
                                mp.saveData();
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.ReplaceRemove")
                                    .replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        if (!UnlockedAbilities.isAbility(args[3])) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility() && ability != UnlockedAbilities.DIVINE_ESCAPE) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.remove"))) {
                                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                        return true;
                                    }
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        if (mp.getCooldown(ability) == -1) {
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
                                            return true;
                                        }
                                        mp.removeAbilityOnCooldown(ability);
                                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
                                            .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                        offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Removed")
                                            .replace("%Ability%", ability.getName())));
                                        mp.saveData();
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        if (mp.getCooldown(ability) == -1) {
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
                                            return true;
                                        }
                                        mp.removeAbilityOnCooldown(ability);
                                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
                                            .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                        mp.saveData();
                                        return true;
                                    }
                                }
                            }
                            else {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else if (args[1].equalsIgnoreCase("add")) {
                        if (args.length < 5) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (!UnlockedAbilities.isAbility(args[2])) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility()) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (!Methods.isInt(args[4])) {
                                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
                                        return true;
                                    }
                                    else {
                                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.cooldown.*") || admin.hasPermission("mcadmin.cooldown.add"))) {
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                            return true;
                                        }
                                        int cooldown = Integer.parseInt(args[4]);
                                        Calendar cal = Calendar.getInstance();
                                        if (offlinePlayer.isOnline()) {
                                            McRPGPlayer mp;
                                            try {
                                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                            }
                                            catch (McRPGPlayerNotFoundException exception) {
                                                return true;
                                            }
                                            long oldCooldown = mp.getCooldown(ability);
                                            cal.setTimeInMillis(oldCooldown);
                                            cal.add(Calendar.SECOND, cooldown);
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Added")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
                                            mp.saveData();
                                            return true;
                                        }
                                        else {
                                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                            long oldCooldown = mp.getCooldown(ability);
                                            cal.setTimeInMillis(oldCooldown);
                                            cal.add(Calendar.SECOND, cooldown);
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            mp.saveData();
                                            return true;
                                        }
                                    }
                                }
                            }
                            else {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else {
                        sendHelpMessage(admin);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (args[1].equalsIgnoreCase("skill")) {
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.skill"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (!isSkill(args[3])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            Skills skillEnum = fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill skill = mp.getSkill(skillEnum);
                                for (BaseAbility baseAbility : skill.getAbilities()) {
                                    baseAbility.setUnlocked(false);
                                    baseAbility.setCurrentTier(0);
                                    baseAbility.setToggled(true);
                                    if (baseAbility instanceof RemoteTransfer) {
                                        ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                        mp.setLinkedToRemoteTransfer(false);
                                        RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                    }
                                }
                                skill.setCurrentExp(0);
                                skill.setCurrentLevel(0);
                                skill.updateExpToLevel();
                                ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equals(skill)).collect(Collectors.toCollection(ArrayList::new));
                                for (UnlockedAbilities remove : toRemove) {
                                    mp.getAbilityLoadout().remove(remove);
                                }
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillReset")
                                    .replace("%Skill%", skill.getType().getDisplayName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillWasReset")
                                    .replace("%Skill%", skill.getType().getDisplayName())));
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        expDisplayType.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
                                    }
                                }
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                Skill skill = mp.getSkill(skillEnum);
                                for (BaseAbility baseAbility : skill.getAbilities()) {
                                    baseAbility.setUnlocked(false);
                                    baseAbility.setCurrentTier(0);
                                    baseAbility.setToggled(true);
                                    if (baseAbility instanceof RemoteTransfer) {
                                        ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                        mp.setLinkedToRemoteTransfer(false);
                                        RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                    }
                                }
                                ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equals(skill)).collect(Collectors.toCollection(ArrayList::new));
                                for (UnlockedAbilities remove : toRemove) {
                                    mp.getAbilityLoadout().remove(remove);
                                }
                                skill.setCurrentExp(0);
                                skill.setCurrentLevel(0);
                                mp.updatePowerLevel();
                                skill.updateExpToLevel();
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillReset")
                                    .replace("%Skill%", skill.getType().getDisplayName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("ability")) {
                        if (args.length < 4) {
                            sendHelpMessage(admin);
                            return true;
                        }
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.ability"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (!UnlockedAbilities.isAbility(args[3])) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                                return true;
                            }
                            UnlockedAbilities abilityEnum = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
                                baseAbility.setUnlocked(false);
                                baseAbility.setCurrentTier(0);
                                baseAbility.setToggled(true);

                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                    mp.setLinkedToRemoteTransfer(false);
                                    RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                }

                                UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
                                mp.getAbilityLoadout().remove(abilities);

                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
                                    .replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityWasReset")
                                    .replace("%Ability%", abilities.getName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());

                                BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
                                baseAbility.setUnlocked(false);
                                baseAbility.setCurrentTier(0);
                                baseAbility.setToggled(true);

                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                    mp.setLinkedToRemoteTransfer(false);
                                    RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                }

                                UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
                                mp.getAbilityLoadout().remove(abilities);
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
                                    .replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("player")) {
                        if (!(admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.reset.*") || admin.hasPermission("mcadmin.reset.player"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Arrays.stream(values()).forEach(s -> {
                                    mp.getSkill(s).resetSkill();
                                });
                                mp.getPendingUnlockAbilities().clear();
                                mp.getAbilityLoadout().clear();
                                mp.setAbilityPoints(0);
                                mp.setRedeemableExp(0);
                                mp.setRedeemableLevels(0);
                                mp.updatePowerLevel();
                                if (mp.getReadyingAbilityBit() != null) {
                                    Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                                    mp.setReadyingAbilityBit(null);
                                }

                                ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
                                mp.setLinkedToRemoteTransfer(false);
                                RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());

                                mp.setReadying(false);
                                mp.setLinkedToRemoteTransfer(false);

                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        Skill skill = mp.getSkill(expDisplayType.getSkill());
                                        expDisplayType.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
                                    }
                                }
                                mp.setDisplayType(DisplayType.SCOREBOARD);

                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerReset")
                                    .replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerWasReset")));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());

                                Arrays.stream(values()).forEach(s -> mp.getSkill(s).resetSkill());
                                mp.updatePowerLevel();
                                Arrays.stream(values()).forEach(s -> mp.getSkill(s).updateExpToLevel());
                                mp.getAbilityLoadout().clear();
                                mp.setAbilityPoints(0);
                                mp.setRedeemableExp(0);
                                mp.setRedeemableLevels(0);
                                if (mp.getReadyingAbilityBit() != null) {
                                    Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                                    mp.setReadyingAbilityBit(null);
                                }
                                ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
                                mp.setLinkedToRemoteTransfer(false);
                                RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerReset")
                                    .replace("%Player%", offlinePlayer.getName())));
                                mp.setReadying(false);
                                mp.setDisplayType(DisplayType.SCOREBOARD);
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("party")) {
                    // /mcadmin party fdisband %player%
                    if (args[1].equalsIgnoreCase("fdisband")) {
                        if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.fdisband"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException e) {
                                mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            }
                            if (mp.getPartyID() != null) {
                                Party party = plugin.getPartyManager().getParty(mp.getPartyID());
                                for (UUID uuid : party.getAllMemberUUIDs()) {
                                    if (!uuid.equals(admin.getUniqueId())) {
                                        OfflinePlayer targ = Bukkit.getOfflinePlayer(uuid);
                                        if (targ.isOnline()) {
                                            ((Player) targ).sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyWasForceDisbanded")));
                                        }
                                    }
                                }
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceDisbandedParty")));
                                plugin.getPartyManager().removeParty(party.getPartyID());
                            }
                            else {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        }
                        return true;
                    }
                    // /mcadmin party fkick %player%
                    if (args[1].equalsIgnoreCase("fkick")) {
                        if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.fkick"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException e) {
                                mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            }
                            if (mp.getPartyID() != null) {
                                Party party = plugin.getPartyManager().getParty(mp.getPartyID());
                                party.kickPlayer(mp.getPartyID());
                                if (offlinePlayer.isOnline()) {
                                    ((Player) offlinePlayer).sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceKickedFromParty")));
                                }
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceKickedPlayer").replace("%Player%", offlinePlayer.getName())));
                            }
                            else {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        }
                        return true;
                    }
                    // /mcadmin party fsetowner %player%
                    if (args[1].equalsIgnoreCase("fsetowner")) {
                        if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.fsetowner"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException e) {
                                mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            }
                            if (mp.getPartyID() != null) {
                                Party party = plugin.getPartyManager().getParty(mp.getPartyID());
                                PartyMember target = party.getPartyMember(offlinePlayer.getUniqueId());
                                if (target.getPartyRole() == PartyRoles.OWNER) {
                                    admin.sendMessage(Methods.color(plugin.getPluginPrefix() + "&cThat player is already the owner of their party"));
                                    return true;
                                }
                                PartyMember previousOwner = null;
                                for (PartyMember partyMember : party.getAllMembers()) {
                                    if (partyMember.getPartyRole() == PartyRoles.OWNER) {
                                        previousOwner = partyMember;
                                        break;
                                    }
                                }
                                if (previousOwner != null) {
                                    previousOwner.setPartyRole(PartyRoles.MOD);
                                    OfflinePlayer offlinePrevOwner = Bukkit.getOfflinePlayer(previousOwner.getUuid());
                                    if (offlinePrevOwner.isOnline()) {
                                        ((Player) offlinePrevOwner).sendMessage(Methods.color(plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyOwnerForciblySet")));
                                    }
                                }
                                target.setPartyRole(PartyRoles.OWNER);
                                if (offlinePlayer.isOnline()) {
                                    ((Player) offlinePlayer).sendMessage(Methods.color(plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.BeenSetOwner")));
                                }
                                party.saveParty();
                                if (offlinePlayer.isOnline()) {
                                    ((Player) offlinePlayer).sendMessage(Methods.color(plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceSetOwnerFromParty")));
                                }
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceSetOwner").replace("%Player%", offlinePlayer.getName())));
                            }
                            else {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        }
                        return true;
                    }
                    if (args.length < 4) {
                        sendHelpMessage(admin);
                        return true;
                    }
                    // /mcadmin party name %player% %name%
                    if (args[1].equalsIgnoreCase("name")) {
                        if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.name"))) {
                            admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException e) {
                                mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            }
                            if (mp.getPartyID() != null) {
                                Party party = McRPG.getInstance().getPartyManager().getParty(mp.getPartyID());
                                party.setName(args[3]);
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.ForceSetPartyName")
                                    .replace("%Player%", offlinePlayer.getName()).replace("%Name%", args[3])));
                            }
                            else {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                            }
                        }
                        else {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        }
                        return true;
                    }
                    if (args.length < 5) {
                        sendHelpMessage(admin);
                        return true;
                    }
                    // /mcadmin party give exp %exp% %player%
                    // /mcadmin party give level %level% %player%
                    if (args[1].equalsIgnoreCase("give")) {
                        // /mcadmin party give exp %exp% %player%
                        if (args[2].equalsIgnoreCase("exp")) {
                            if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.partyexp"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                return true;
                            }
                            if (!Methods.isInt(args[3])) {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            else {
                                int expToGive = Integer.parseInt(args[3]);
                                if (Methods.hasPlayerLoggedInBefore(args[4])) {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[4]);
                                    McRPGPlayer mp;
                                    try {
                                        mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                    }
                                    catch (McRPGPlayerNotFoundException e) {
                                        mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                    }
                                    if (mp.getPartyID() != null) {
                                        Party party = plugin.getPartyManager().getParty(mp.getPartyID());
                                        party.giveExp(expToGive);
                                        admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyExpGiven")
                                            .replace("%Exp%", Integer.toString(expToGive)).replace("%Player%", offlinePlayer.getName())));

                                    }
                                    else {
                                        admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                                    }
                                }
                                else {
                                    admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                }
                            }
                            return true;
                        }
                        // /mcadmin party give level %level% %player%
                        else if (args[2].equalsIgnoreCase("level")) {
                            if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.give.*") || admin.hasPermission("mcadmin.give.partylevel"))) {
                                admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                                return true;
                            }
                            if (!Methods.isInt(args[3])) {
                                admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            else {
                                int levelsToGive = Integer.parseInt(args[3]);
                                if (Methods.hasPlayerLoggedInBefore(args[4])) {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[4]);
                                    McRPGPlayer mp;
                                    try {
                                        mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                    }
                                    catch (McRPGPlayerNotFoundException e) {
                                        mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                    }
                                    if (mp.getPartyID() != null) {
                                        Party party = plugin.getPartyManager().getParty(mp.getPartyID());
                                        if (levelsToGive < 0) {
                                            party.reduceAbilityPoints(party.getPartyLevel() + levelsToGive, party.getPartyLevel());
                                        }
                                        else {
                                            for (int i = 1; i <= levelsToGive; i++) {
                                                if ((party.getPartyLevel() + i) > McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("PartyExp.MaxLevelForUpgradePoints", 20)) {
                                                    continue;
                                                }
                                                if ((party.getPartyLevel() + i) % McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("PartyExp.UpgradePointFactor", 1) == 0) {
                                                    party.setPartyUpgradePoints(party.getPartyUpgradePoints() + 1);
                                                }
                                            }
                                        }
                                        party.setPartyLevel(Math.min(Math.max(party.getPartyLevel() + levelsToGive, 0), McRPG.getInstance().getPartyManager().getMaxLevel()));
                                        admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PartyLevelsGiven")
                                            .replace("%Levels%", Integer.toString(levelsToGive)).replace("%Player%", offlinePlayer.getName())));

                                    }
                                    else {
                                        admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.PlayerNotHaveParty")));
                                    }
                                }
                                else {
                                    admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                }
                            }
                            return true;
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("copy")) {

                    if (!(admin.hasPermission("mcrpg.*") || admin.hasPermission("mcadmin.*") || admin.hasPermission("mcadmin.copy"))) {
                        admin.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Utility.NoPerms")));
                        return true;
                    }

                    try {
                        UUID fromUUID = UUID.fromString(args[1]);
                        UUID toUUID = UUID.fromString(args[2]);

                        if (fromUUID.equals(toUUID)) {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.DuplicateUUIDs")));
                            return true;
                        }

                        Player fromPlayer = Bukkit.getPlayer(fromUUID);
                        Player toPlayer = Bukkit.getPlayer(toUUID);

                        //Enforce both players being offline
                        if (fromPlayer != null || toPlayer != null) {
                            admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.PlayersAreOnline")));
                            return true;
                        }

                        CompletableFuture<Void> copyFuture = PlayerDataDAO.copyPlayerData(fromUUID, toUUID);
                        if (copyFuture != null) {
                            copyFuture.thenAccept(unused -> {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.DataCopied")));
                            });
                        }

                        return true;


                    }
                    catch (IllegalArgumentException e) {
                        admin.sendMessage(Methods.color(admin, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.InvalidUUIDs")));
                        e.printStackTrace();
                    }
                }
                else {
                    sendHelpMessage(admin);
                    return true;
                }
            }
        }
        else {
            if (args.length < 3) {
                sendHelpMessage(sender);
                return true;
            }
            else {
                if (args[0].equalsIgnoreCase("give")) {
                    if (args[1].equalsIgnoreCase("abilitypoints")) {
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                mp.setAbilityPoints(mp.getAbilityPoints() + amount);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.AbilityPoints").replace("%Amount%", args[3])));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.setAbilityPoints(mp.getAbilityPoints() + amount);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AbilityPoints").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("exp")) {
                        if (args.length < 5) {
                            if (args.length < 4) {
                                sendHelpMessage(sender);
                                return true;
                            }
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                if (!Methods.isInt(args[3])) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                    return true;
                                }
                                else {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        mp.setRedeemableExp(mp.getRedeemableExp() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        sender.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableExp").replace("%Amount%", args[3]).replace("%Player%", ((Player) offlinePlayer).getDisplayName())));
                                        ((Player) offlinePlayer).sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.RedeemableExp").replace("%Amount%", args[3])));
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        mp.setRedeemableExp(mp.getRedeemableExp() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        sender.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableExp").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                        return true;
                                    }
                                }
                            }
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            if (!isSkill(args[4])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            Skills skill = fromString(args[4]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill s = mp.getSkill(skill);
                                s.giveExp(mp, amount, GainReason.COMMAND);
                                s.updateExpToLevel();
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        expDisplayType.sendUpdate(s.getCurrentExp(), s.getExpToLevel(), s.getCurrentLevel(), amount);
                                    }
                                }
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Exp")
                                    .replace("%Amount%", args[3]).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.getSkill(skill).giveExp(mp, amount, GainReason.COMMAND);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Exp")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("level")) {
                        if (args.length < 5) {
                            if (args.length < 4) {
                                sendHelpMessage(sender);
                                return true;
                            }
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                if (!Methods.isInt(args[3])) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                    return true;
                                }
                                else {
                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        mp.setRedeemableLevels(mp.getRedeemableLevels() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        sender.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableLevels").replace("%Amount%", args[3]).replace("%Player%", ((Player) offlinePlayer).getDisplayName())));
                                        ((Player) offlinePlayer).sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.RedeemableLevels").replace("%Amount%", args[3])));
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        mp.setRedeemableLevels(mp.getRedeemableLevels() + Integer.parseInt(args[3]));
                                        mp.saveData();
                                        sender.sendMessage(Methods.color((Player) offlinePlayer, plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.RedeemableLevels").replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName())));
                                        return true;
                                    }
                                }
                            }
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!Methods.isInt(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnInt")));
                                return true;
                            }
                            if (!isSkill(args[4])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            int amount = Integer.parseInt(args[3]);
                            Skills skill = fromString(args[4]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill s = mp.getSkill(skill);
                                s.giveLevels(mp, amount, true);
                                s.updateExpToLevel();
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        expDisplayType.sendUpdate(s.getCurrentExp(), s.getExpToLevel(), s.getCurrentLevel(), 0);
                                    }
                                }
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Level")
                                    .replace("%Amount%", args[3]).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                mp.getSkill(skill).giveLevels(mp, amount, true);
                                mp.getSkill(skill).updateExpToLevel();

                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Level")
                                    .replace("%Amount%", args[3]).replace("%Player%", offlinePlayer.getName()).replace("%Skill%", skill.getDisplayName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("ability")) {
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            if (!UnlockedAbilities.isAbility(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                if (!ability.isPassiveAbility() && mp.doesPlayerHaveActiveAbilityFromSkill(ability.getSkill())) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.HasActive")));
                                    return true;
                                }
                                if (mp.getAbilityLoadout().size() == McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities")) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.LoadoutFull")));
                                    return true;
                                }
                                if (mp.getAbilityLoadout().contains(ability)) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.AlreadyHave")));
                                    return true;
                                }
                                BaseAbility baseAbility = mp.getBaseAbility(ability);
                                if (!baseAbility.isUnlocked()) {
                                    baseAbility.setCurrentTier(1);
                                }
                                if (baseAbility.getCurrentTier() == 0) {
                                    baseAbility.setCurrentTier(1);
                                }
                                baseAbility.setUnlocked(true);
                                baseAbility.setToggled(true);
                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).updateBlocks();
                                }
                                mp.getAbilityLoadout().add(ability);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Ability").replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                if (!ability.isPassiveAbility() && mp.doesPlayerHaveActiveAbilityFromSkill(ability.getSkill())) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.HasActive")));
                                    return true;
                                }
                                BaseAbility baseAbility = mp.getBaseAbility(ability);
                                if (!baseAbility.isUnlocked()) {
                                    baseAbility.setCurrentTier(1);
                                }
                                baseAbility.setUnlocked(true);
                                baseAbility.setToggled(true);
                                mp.getAbilityLoadout().add(ability);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Ability").replace("%Player%", offlinePlayer.getName()).replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("book")) {
                        //mcadmin give book %type% %player%
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[3])) {
                            if (!(args[2].equalsIgnoreCase("unlock") || args[2].equalsIgnoreCase("upgrade"))) {
                                sendHelpMessage(sender);
                                return true;
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[3]);
                            if (!offlinePlayer.isOnline()) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerOffline")));
                                return true;
                            }
                            else {
                                Player p = (Player) offlinePlayer;
                                ItemStack book = args[2].equalsIgnoreCase("unlock") ? SkillBookFactory.generateUnlockBook() : SkillBookFactory.generateUpgradeBook();
                                p.getWorld().dropItemNaturally(p.getLocation(), book);
                                p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Book").replace("%Player%", offlinePlayer.getName()).replace("%Type%", args[2])));
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Book").replace("%Player%", offlinePlayer.getName()).replace("%Type%", args[2])));
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }

                    else {
                        sendHelpMessage(sender);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("replace")) {
                    if (args.length < 4) {
                        sendHelpMessage(sender);
                        return true;
                    }
                    if (Methods.hasPlayerLoggedInBefore(args[1])) {
                        if (!UnlockedAbilities.isAbility(args[2]) && !UnlockedAbilities.isAbility(args[3])) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                            return true;
                        }
                        else {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            UnlockedAbilities old = UnlockedAbilities.fromString(args[2]);
                            UnlockedAbilities newAbility = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                if (!mp.getAbilityLoadout().contains(old)) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                        .replace("%Ability%", old.getName())));
                                    return true;
                                }
                                else {
                                    BaseAbility ab = mp.getBaseAbility(newAbility);
                                    if (!ab.isUnlocked()) {
                                        ab.setCurrentTier(1);
                                    }
                                    ab.setToggled(true);
                                    ab.setUnlocked(true);
                                    mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced").replace("%Player%", offlinePlayer.getName())
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Receive.Replaced")
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    return true;
                                }
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                if (!mp.getAbilityLoadout().contains(old)) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                        .replace("%Ability%", old.getName())));
                                    return true;
                                }
                                else {
                                    BaseAbility ab = mp.getBaseAbility(newAbility);
                                    if (!ab.isUnlocked()) {
                                        ab.setCurrentTier(1);
                                    }
                                    ab.setToggled(true);
                                    ab.setUnlocked(true);
                                    mp.getAbilityLoadout().set(mp.getAbilityLoadout().indexOf(old), newAbility);
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.Replaced")
                                        .replace("%Old_Ability%", old.getName()).replace("%New_Ability%", newAbility.getName())));
                                    return true;
                                }
                            }
                        }
                    }
                    else {
                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("remove")) {
                    if (Methods.hasPlayerLoggedInBefore(args[1])) {
                        if (!UnlockedAbilities.isAbility(args[2])) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAbility")));
                            return true;
                        }
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                        UnlockedAbilities ability = UnlockedAbilities.fromString(args[2]);
                        if (offlinePlayer.isOnline()) {
                            McRPGPlayer mp;
                            try {
                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                            }
                            catch (McRPGPlayerNotFoundException exception) {
                                return true;
                            }
                            if (!mp.getAbilityLoadout().contains(ability)) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                    .replace("%Ability%", ability.getName())));
                                return true;
                            }
                            else {
                                mp.getAbilityLoadout().remove(ability);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
                                    .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Removed.Ability")
                                    .replace("%Ability%", ability.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                            if (!mp.getAbilityLoadout().contains(ability)) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility")
                                    .replace("%Ability%", ability.getName())));
                                return true;
                            }
                            else {
                                mp.getAbilityLoadout().remove(ability);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Remove.Ability")
                                    .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                    }
                    else {
                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                        return true;
                    }

                }
                else if (args[0].equalsIgnoreCase("view")) {
                    if (args[1].equalsIgnoreCase("loadout")) {
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
                                mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(sender::sendMessage);
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers Loadout:"));
                                mp.getAbilityLoadout().stream().map(ab -> Methods.color("&e" + ab.getName())).forEach(sender::sendMessage);
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }

                    }
                    else if (isSkill(args[1])) {
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            Skills skill = fromString(args[1]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill skillInfo = mp.getSkill(skill);
                                sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
                                sender.sendMessage(Methods.color("&eCurrent Level: " + skillInfo.getCurrentLevel()));
                                sender.sendMessage(Methods.color("&eCurrent Exp: " + skillInfo.getCurrentExp()));
                                sender.sendMessage(Methods.color("&eExp To Level: " + skillInfo.getExpToLevel()));
                                skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(sender::sendMessage);
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                Skill skillInfo = mp.getSkill(skill);
                                sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + " &aPlayers " + skill.getName() + " Info:"));
                                sender.sendMessage(Methods.color("&eCurrent Level: " + skillInfo.getCurrentLevel()));
                                sender.sendMessage(Methods.color("&eCurrent Exp: " + skillInfo.getCurrentExp()));
                                sender.sendMessage(Methods.color("&eExp To Level: " + skillInfo.getExpToLevel()));
                                skillInfo.getAbilities().stream().map(ability -> Methods.color("&e" + ability.getGenericAbility().getName() + ": Unlocked-" + ability.isUnlocked() + " Tier-" + ability.getCurrentTier())).forEach(sender::sendMessage);
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else {
                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("cooldown")) {
                    if (args[1].equalsIgnoreCase("set")) {
                        if (args.length < 5) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (!UnlockedAbilities.isAbility(args[3])) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility()) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (!Methods.isInt(args[4])) {
                                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
                                        return true;
                                    }
                                    else {
                                        int cooldown = Integer.parseInt(args[4]);
                                        Calendar cal = Calendar.getInstance();
                                        cal.add(Calendar.SECOND, cooldown);
                                        if (offlinePlayer.isOnline()) {
                                            McRPGPlayer mp;
                                            try {
                                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                            }
                                            catch (McRPGPlayerNotFoundException exception) {
                                                return true;
                                            }
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.WasSet")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
                                            mp.saveData();
                                            return true;
                                        }
                                        else {
                                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Set")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            mp.saveData();
                                            return true;
                                        }
                                    }
                                }
                            }
                            else {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else if (args[1].equalsIgnoreCase("remove")) {
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (!UnlockedAbilities.isAbility(args[3])) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility()) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (offlinePlayer.isOnline()) {
                                        McRPGPlayer mp;
                                        try {
                                            mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                        }
                                        catch (McRPGPlayerNotFoundException exception) {
                                            return true;
                                        }
                                        if (mp.getCooldown(ability) == -1) {
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
                                            return true;
                                        }
                                        mp.removeAbilityOnCooldown(ability);
                                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
                                            .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                        offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Removed")
                                            .replace("%Ability%", ability.getName())));
                                        mp.saveData();
                                        return true;
                                    }
                                    else {
                                        McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                        if (mp.getCooldown(ability) == -1) {
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Give.DoesNotHaveAbility").replace("%Ability%", ability.getName())));
                                            return true;
                                        }
                                        mp.removeAbilityOnCooldown(ability);
                                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Remove")
                                            .replace("%Ability%", ability.getName()).replace("%Player%", offlinePlayer.getName())));
                                        mp.saveData();
                                        return true;
                                    }
                                }
                            }
                            else {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else if (args[1].equalsIgnoreCase("add")) {
                        if (args.length < 5) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (!UnlockedAbilities.isAbility(args[2])) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                            return true;
                        }
                        else {
                            if (Methods.hasPlayerLoggedInBefore(args[2])) {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UnlockedAbilities ability = UnlockedAbilities.fromString(args[3]);
                                if (ability.isPassiveAbility()) {
                                    sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotActiveAbility")));
                                    return true;
                                }
                                else {
                                    if (!Methods.isInt(args[4])) {
                                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAInt")));
                                        return true;
                                    }
                                    else {
                                        int cooldown = Integer.parseInt(args[4]);
                                        Calendar cal = Calendar.getInstance();
                                        if (offlinePlayer.isOnline()) {
                                            McRPGPlayer mp;
                                            try {
                                                mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                            }
                                            catch (McRPGPlayerNotFoundException exception) {
                                                return true;
                                            }
                                            long oldCooldown = mp.getCooldown(ability);
                                            cal.setTimeInMillis(oldCooldown);
                                            cal.add(Calendar.SECOND, cooldown);
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Added")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4])));
                                            mp.saveData();
                                            return true;
                                        }
                                        else {
                                            McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                            long oldCooldown = mp.getCooldown(ability);
                                            cal.setTimeInMillis(oldCooldown);
                                            cal.add(Calendar.SECOND, cooldown);
                                            mp.addAbilityOnCooldown(ability, cal.getTimeInMillis());
                                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Cooldown.Add")
                                                .replace("%Ability%", ability.getName()).replace("%Cooldown%", args[4]).replace("%Player%", offlinePlayer.getName())));
                                            mp.saveData();
                                            return true;
                                        }
                                    }
                                }
                            }
                            else {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                                return true;
                            }
                        }
                    }
                    else {
                        sendHelpMessage(sender);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("reset")) {
                    if (args[1].equalsIgnoreCase("skill")) {
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (!isSkill(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotASkill")));
                                return true;
                            }
                            Skills skillEnum = fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Skill skill = mp.getSkill(skillEnum);
                                for (BaseAbility baseAbility : skill.getAbilities()) {
                                    baseAbility.setUnlocked(false);
                                    baseAbility.setCurrentTier(0);
                                    baseAbility.setToggled(true);
                                    if (baseAbility instanceof RemoteTransfer) {
                                        ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                        mp.setLinkedToRemoteTransfer(false);
                                        RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                    }
                                }
                                skill.setCurrentExp(0);
                                skill.setCurrentLevel(0);
                                skill.updateExpToLevel();
                                ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equals(skill)).collect(Collectors.toCollection(ArrayList::new));
                                for (UnlockedAbilities remove : toRemove) {
                                    mp.getAbilityLoadout().remove(remove);
                                }
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillReset")
                                    .replace("%Skill%", skill.getType().getDisplayName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillWasReset")
                                    .replace("%Skill%", skill.getType().getDisplayName())));
                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpScoreboardDisplay) {
                                        ExpScoreboardDisplay expScoreboardDisplay = (ExpScoreboardDisplay) displayManager.getDisplay(p);
                                        expScoreboardDisplay.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
                                    }
                                }
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());
                                Skill skill = mp.getSkill(skillEnum);
                                for (BaseAbility baseAbility : skill.getAbilities()) {
                                    baseAbility.setUnlocked(false);
                                    baseAbility.setCurrentTier(0);
                                    baseAbility.setToggled(true);
                                    if (baseAbility instanceof RemoteTransfer) {
                                        ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                        mp.setLinkedToRemoteTransfer(false);
                                        RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                    }
                                }
                                ArrayList<UnlockedAbilities> toRemove = mp.getAbilityLoadout().stream().filter(ab -> ab.getSkill().equals(skill)).collect(Collectors.toCollection(ArrayList::new));
                                for (UnlockedAbilities remove : toRemove) {
                                    mp.getAbilityLoadout().remove(remove);
                                }
                                skill.setCurrentExp(0);
                                skill.setCurrentLevel(0);
                                mp.updatePowerLevel();
                                skill.updateExpToLevel();
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.SkillReset")
                                    .replace("%Skill%", skill.getType().getDisplayName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("ability")) {
                        if (args.length < 4) {
                            sendHelpMessage(sender);
                            return true;
                        }
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (!UnlockedAbilities.isAbility(args[3])) {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.NotAnAbility")));
                                return true;
                            }
                            UnlockedAbilities abilityEnum = UnlockedAbilities.fromString(args[3]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }

                                BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
                                baseAbility.setUnlocked(false);
                                baseAbility.setCurrentTier(0);
                                baseAbility.setToggled(true);

                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                    mp.setLinkedToRemoteTransfer(false);
                                    RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                }

                                UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
                                mp.getAbilityLoadout().remove(abilities);

                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
                                    .replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityWasReset")
                                    .replace("%Ability%", abilities.getName())));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());

                                BaseAbility baseAbility = mp.getBaseAbility(abilityEnum);
                                baseAbility.setUnlocked(false);
                                baseAbility.setCurrentTier(0);
                                baseAbility.setToggled(true);

                                if (baseAbility instanceof RemoteTransfer) {
                                    ((RemoteTransfer) baseAbility).setLinkedChestLocation(null);
                                    mp.setLinkedToRemoteTransfer(false);
                                    RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                }

                                UnlockedAbilities abilities = (UnlockedAbilities) baseAbility.getGenericAbility();
                                mp.getAbilityLoadout().remove(abilities);
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.AbilityReset")
                                    .replace("%Ability%", abilities.getName()).replace("%Player%", offlinePlayer.getName())));
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                    else if (args[1].equalsIgnoreCase("player")) {
                        if (Methods.hasPlayerLoggedInBefore(args[2])) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                            if (offlinePlayer.isOnline()) {
                                McRPGPlayer mp;
                                try {
                                    mp = PlayerManager.getPlayer(offlinePlayer.getUniqueId());
                                }
                                catch (McRPGPlayerNotFoundException exception) {
                                    return true;
                                }
                                Arrays.stream(values()).forEach(s -> mp.getSkill(s).resetSkill());
                                mp.getAbilityLoadout().clear();
                                mp.setAbilityPoints(0);
                                mp.setRedeemableExp(0);
                                mp.setRedeemableLevels(0);
                                mp.updatePowerLevel();
                                if (mp.getReadyingAbilityBit() != null) {
                                    Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                                    mp.setReadyingAbilityBit(null);
                                }

                                ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
                                mp.setLinkedToRemoteTransfer(false);
                                RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());

                                mp.setReadying(false);
                                mp.setLinkedToRemoteTransfer(false);
                                mp.resetCooldowns();

                                DisplayManager displayManager = McRPG.getInstance().getDisplayManager();
                                Player p = (Player) offlinePlayer;
                                if (displayManager.doesPlayerHaveDisplay(p)) {
                                    if (displayManager.getDisplay(p) instanceof ExpDisplayType) {
                                        ExpDisplayType expDisplayType = (ExpDisplayType) displayManager.getDisplay(p);
                                        Skill skill = mp.getSkill(expDisplayType.getSkill());
                                        expDisplayType.sendUpdate(skill.getCurrentExp(), skill.getExpToLevel(), skill.getCurrentLevel(), 0);
                                    }
                                }
                                mp.setDisplayType(DisplayType.SCOREBOARD);

                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerReset")
                                    .replace("%Player%", offlinePlayer.getName())));
                                offlinePlayer.getPlayer().sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.PlayerWasReset")));
                                mp.saveData();
                                return true;
                            }
                            else {
                                McRPGPlayer mp = new McRPGPlayer(offlinePlayer.getUniqueId());

                                Arrays.stream(values()).forEach(s -> mp.getSkill(s).resetSkill());
                                mp.updatePowerLevel();
                                Arrays.stream(values()).forEach(s -> mp.getSkill(s).updateExpToLevel());
                                mp.getAbilityLoadout().clear();
                                mp.setAbilityPoints(0);
                                mp.setRedeemableExp(0);
                                mp.setRedeemableLevels(0);
                                if (mp.getReadyingAbilityBit() != null) {
                                    Bukkit.getScheduler().cancelTask(mp.getReadyingAbilityBit().getEndTaskID());
                                    mp.setReadyingAbilityBit(null);
                                }
                                ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
                                mp.setLinkedToRemoteTransfer(false);
                                RemoteTransferTracker.removeLocation(offlinePlayer.getUniqueId());
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Reset.Player")
                                    .replace("%Player%", offlinePlayer.getName())));
                                mp.setReadying(false);
                                mp.setDisplayType(DisplayType.SCOREBOARD);
                                mp.saveData();
                                return true;
                            }
                        }
                        else {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.PlayerHasNotLoggedIn")));
                            return true;
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("copy")) {

                    try {
                        UUID fromUUID = UUID.fromString(args[1]);
                        UUID toUUID = UUID.fromString(args[2]);

                        if (fromUUID.equals(toUUID)) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.DuplicateUUIDs")));
                            return true;
                        }

                        Player fromPlayer = Bukkit.getPlayer(fromUUID);
                        Player toPlayer = Bukkit.getPlayer(toUUID);

                        //Enforce both players being offline
                        if (fromPlayer != null || toPlayer != null) {
                            sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.PlayersAreOnline")));
                            return true;
                        }

                        CompletableFuture<Void> copyFuture = PlayerDataDAO.copyPlayerData(fromUUID, toUUID);
                        if (copyFuture != null) {
                            copyFuture.thenAccept(unused -> {
                                sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.DataCopied")));
                            });
                        }

                        return true;


                    }
                    catch (IllegalArgumentException e) {
                        sender.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Admin.Copy.InvalidUUIDs")));
                        return true;
                    }
                }
                else {
                    sendHelpMessage(sender);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendHelpMessage(CommandSender p) {
        McRPG plugin = McRPG.getInstance();
        FileConfiguration config = plugin.getLangFile();
        p.sendMessage(Methods.color(plugin.getPluginPrefix() + config.getString("Messages.Commands.Utility.HelpPrompt").replaceAll("<command>", "mcadmin")));
    }
}