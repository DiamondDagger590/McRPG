package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fitness.RunnersDiet;
import us.eunoians.mcrpg.abilities.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.RunnersDietEvent;
import us.eunoians.mcrpg.api.events.mcrpg.woodcutting.NymphsVitalityEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.worldguard.EntryLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MoveEvent implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void playerMove(PlayerMoveEvent playerMoveEvent) {

      Player player = playerMoveEvent.getPlayer();
      UUID uniqueId = player.getUniqueId();

      if (PlayerManager.isPlayerFrozen(uniqueId)) {
            playerMoveEvent.setCancelled(true);
            return;
        }
        McRPGPlayer mcRPGPlayer;
        try {
            mcRPGPlayer = PlayerManager.getPlayer(uniqueId);
        }
        catch (McRPGPlayerNotFoundException exception) {
            return;
        }

      Location from = playerMoveEvent.getFrom();
      Location to = playerMoveEvent.getTo();

      if (mcRPGPlayer.getAcceptedTeleportRequest() != null && (from.getX() != to.getX() ||
                                                                from.getY() != to.getY() || from.getZ() != to.getZ())) {
            mcRPGPlayer.getAcceptedTeleportRequest().getWaitTask().cancel();
            mcRPGPlayer.setAcceptedTeleportRequest(null);
            mcRPGPlayer.getPlayer().sendMessage(Methods.color(mcRPGPlayer.getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Parties.TeleportationCanceled")));
        }
        if (mcRPGPlayer.getAbilityLoadout() == null) {
            return;
        }
        //Disabled Worlds
        if (McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
                McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(player.getWorld().getName())) {
            return;
        }
        if (mcRPGPlayer.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.NYMPHS_VITALITY) &&
                UnlockedAbilities.NYMPHS_VITALITY.isEnabled() && mcRPGPlayer.getBaseAbility(UnlockedAbilities.NYMPHS_VITALITY).isToggled()) {
            Biome biome = player.getLocation().getBlock().getBiome();
            FileConfiguration woodCuttingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG);
            if (woodCuttingConfig.getStringList("NymphsVitalityConfig.Biomes").contains(biome.name())) {
                NymphsVitality nymphsVitality = (NymphsVitality) mcRPGPlayer.getBaseAbility(UnlockedAbilities.NYMPHS_VITALITY);
                int minHunger = woodCuttingConfig.getInt("NymphsVitalityConfig.Tier" + Methods.convertToNumeral(nymphsVitality.getCurrentTier()) + ".MinimumHunger");

                if (player.getFoodLevel() < minHunger) {

                    NymphsVitalityEvent nymphsVitalityEvent = new NymphsVitalityEvent(mcRPGPlayer, nymphsVitality, player.getFoodLevel() + 1, player.getFoodLevel());
                    Bukkit.getPluginManager().callEvent(nymphsVitalityEvent);
                    if (!nymphsVitalityEvent.isCancelled()) {
                        player.setFoodLevel(nymphsVitalityEvent.getNewHunger());
                    }
                }
            }
        }
        if (UnlockedAbilities.RUNNERS_DIET.isEnabled() && mcRPGPlayer.getAbilityLoadout().contains(UnlockedAbilities.RUNNERS_DIET)
                && mcRPGPlayer.getBaseAbility(UnlockedAbilities.RUNNERS_DIET).isToggled()) {
            FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
            RunnersDiet runnersDiet = (RunnersDiet) mcRPGPlayer.getBaseAbility(UnlockedAbilities.RUNNERS_DIET);
            int minHunger = fitnessConfig.getInt("RunnersDietConfig.Tier" + Methods.convertToNumeral(runnersDiet.getCurrentTier())
                                                     + ".MinHunger");
            if (player.isSprinting() && player.getFoodLevel() < minHunger) {
                RunnersDietEvent runnersDietEvent = new RunnersDietEvent(mcRPGPlayer, runnersDiet);
                Bukkit.getPluginManager().callEvent(runnersDietEvent);
                if (!runnersDietEvent.isCancelled()) {
                    player.setFoodLevel(player.getFoodLevel() + 1);
                }
            }
        }
        if (McRPG.getInstance().isWorldGuardEnabled()) {
            WGSupportManager supportManager = McRPG.getInstance().getWgSupportManager();
            World w = to.getWorld();
            if (!supportManager.isWorldTracker(w)) {
                return;
            }
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager manager = container.get(BukkitAdapter.adapt(w));
            assert manager != null;
            ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(to));
            HashMap<String, WGRegion> regions = supportManager.getRegionManager().get(w);
            EntryLimiterParser entryLimiterParser = new EntryLimiterParser();
            for (ProtectedRegion region : set) {
                if (regions.containsKey(region.getId())) {
                    List<String> entryExpressions = regions.get(region.getId()).getEnterExpressions();
                    for (String expression : entryExpressions) {
                        if (entryLimiterParser.evaluateExpression(mcRPGPlayer, expression)) {
                            playerMoveEvent.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
}
