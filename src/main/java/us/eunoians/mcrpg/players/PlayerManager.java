package us.eunoians.mcrpg.players;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.TipType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerManager {

    //Players who are currently logged on
    private static HashMap<UUID, McRPGPlayer> players = new HashMap<>();
    private static ArrayList<UUID> playersFrozen = new ArrayList<UUID>();
    private static McRPG plugin;
    private static BukkitTask saveTask;

    public PlayerManager(McRPG plugin) {
        PlayerManager.plugin = plugin;
    }

    public static void addMcRPGPlayer(Player player, boolean freeze) {
        if (players.containsKey(player.getUniqueId())) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (freeze) {
            playersFrozen.add(uuid);
        }

        BukkitTask task = new BukkitRunnable() {
            public void run() {
                McRPGPlayer mp = new McRPGPlayer(uuid);
                mp.getUsedTips().add(TipType.LOGIN_TIP);
                if (mp.isOnline()) {
                    if (!McRPG.getInstance().getFileManager().getFile(FileManager.Files.CONFIG).getBoolean("Configuration.DisableTips") && !mp.isIgnoreTips()) {
                        List<String> possibleMessages = McRPG.getInstance().getLangFile().getStringList("Messages.Tips.LoginTips");
                        Random rand = new Random();
                        int val = rand.nextInt(possibleMessages.size());
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (mp.isOnline()) {
                                    mp.getPlayer().sendMessage(Methods.color(mp.getPlayer(), possibleMessages.get(val)));
                                }
                            }
                        }.runTaskLater(McRPG.getInstance(), 40L);
                    }
                    players.put(uuid, mp);
                }
                playersFrozen.remove(uuid);
            }
        }.runTaskAsynchronously(plugin);
    }

    public static boolean isPlayerFrozen(UUID uuid) {
        if (isPlayerStored(uuid)) {
            playersFrozen.remove(uuid);
        }
        return playersFrozen.contains(uuid);
    }

    public static McRPGPlayer getPlayer(UUID uuid) throws McRPGPlayerNotFoundException {
        if (players.containsKey(uuid)) {
            return players.get(uuid);
        }
        else {
            throw new McRPGPlayerNotFoundException("Player is not found or loaded yet.");
        }
    }

    public static boolean isPlayerStored(UUID uuid) {
        return players.containsKey(uuid);
    }

    public static void removePlayer(UUID uuid) {

        if(players.containsKey(uuid)){
            McRPGPlayer mcRPGPlayer = players.remove(uuid);

            if(plugin.isEnabled()){
                mcRPGPlayer.saveData();
            }
        }
    }

    public static void startSave(Plugin p) {
        plugin = (McRPG) p;

        if (saveTask != null) {
            System.out.println(Methods.color(plugin.getPluginPrefix() + "&eRestarting player saving task...."));
            saveTask.cancel();
        }

        saveTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (p.isEnabled()) {
                    PlayerManager.run();
                }
            }
        }.runTaskTimerAsynchronously(p, 500, ((McRPG) p).getFileManager().getFile(FileManager.Files.CONFIG).getInt("Configuration.SaveInterval") * 1200);

        System.out.println(Methods.color(plugin.getPluginPrefix() + "&aPlayer saving task has been started!"));

        new BukkitRunnable() {
            @Override
            public void run() {
                Collection<McRPGPlayer> clone = ((HashMap<UUID, McRPGPlayer>) players.clone()).values();

                if (p.isEnabled()) {

                    for (McRPGPlayer mp : clone) {

                        if (isPlayerFrozen(mp.getUuid())) {
                            continue;
                        }

                        mp.updateCooldowns();
                    }
                }
            }
        }.runTaskTimer(p, 0, 20);
    }


    private static CompletableFuture<Void> run() {
        List<McRPGPlayer> playerList = new ArrayList<>(players.values());

        CompletableFuture<Void>[] completableFutures = new CompletableFuture[playerList.size()];

        for (int i = 0; i < playerList.size(); i++) {
            completableFutures[i] = playerList.get(i).saveData();
        }

        return CompletableFuture.allOf(completableFutures);
    }

    private static CompletableFuture<Void> runAndShutdown() {
        List<McRPGPlayer> playerList = new ArrayList<>(players.values());
        players.clear();

        CompletableFuture<Void>[] completableFutures = new CompletableFuture[playerList.size()];

        for (int i = 0; i < playerList.size(); i++) {
            completableFutures[i] = playerList.get(i).saveData();
        }

        return CompletableFuture.allOf(completableFutures);
    }

    public static void saveAll() {
        run();
    }

    public static CompletableFuture<Void> shutDownManager() {

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        if (saveTask != null) {
            saveTask.cancel();
        }

        runAndShutdown().thenAccept(completableFuture::complete)
                .exceptionally(throwable -> {
            throwable.printStackTrace();
            completableFuture.completeExceptionally(throwable);
            return null;
        });

        return completableFuture;
    }
}
