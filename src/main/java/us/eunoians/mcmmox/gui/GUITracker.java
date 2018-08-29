package us.eunoians.mcmmox.gui;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import us.eunoians.mcmmox.players.McMMOPlayer;

/*
This class is a static class that keeps track of a players current gui
*/
public class GUITracker {

    private static HashMap<UUID, GUITrackerBit> guiTracker = new HashMap<UUID, GUITrackerBit>();

    public static boolean isPlayerTracked(Player p) {
        boolean result = false;
        if(guiTracker.containsKey(p.getUniqueId())){
            result = true;
        }
        return result;
    }

    public static boolean isPlayerTracked(McMMOPlayer p) {
        boolean result = false;
        if(guiTracker.containsKey(p.getUuid())) {
            result = true;
        }
        return result;
    }

    public static boolean isPlayerTracked(UUID p) {
        boolean result = false;
        if(guiTracker.containsKey(p)) {
            result = true;
        }
        return result;
    }


    public static boolean doesPlayerHavePrevious(McMMOPlayer p) {
        if(guiTracker.containsKey(p.getUuid())){
            if(guiTracker.get(p.getUuid()).hasPreviousGUI()){
                return true;
            }
        }
        return false;
    }

    public static boolean doesPlayerHavePrevious(Player p) {
        if(guiTracker.containsKey(p.getUniqueId())) {
            if (guiTracker.get(p.getUniqueId()).hasPreviousGUI()) {
                return true;
            }
        }
        return false;
    }

    public static boolean doesPlayerHavePrevious(UUID p) {
        if(guiTracker.containsKey(p)) {
            if (guiTracker.get(p).hasPreviousGUI()) {
                return true;
            }
        }
        return false;
    }


    public static void stopTrackingPlayer(McMMOPlayer p) {
        guiTracker.remove(p.getUuid());
    }

    public static void stopTrackingPlayer(Player p) {
        guiTracker.remove(p.getUniqueId());
    }

    public static void stopTrackingPlayer(UUID uuid) {
        guiTracker.remove(uuid);
    }


    public static GUI getPlayersGUI(McMMOPlayer p) {
        return guiTracker.get(p.getUuid()).getCurrentGUI();
    }

    public static GUI getPlayersGUI(Player p) {
        return guiTracker.get(p.getUniqueId()).getCurrentGUI();
    }

    public static GUI getPlayersGUI(UUID p) {
        return guiTracker.get(p).getCurrentGUI();
    }


    public static GUI getPlayersPreviousGUI(McMMOPlayer p) {
        return guiTracker.get(p.getUuid()).getPreviousGUI();
    }

    public static GUI getPlayersPreviousGUI(Player p) {
        return guiTracker.get(p.getUniqueId()).getPreviousGUI();
    }

    public static GUI getPlayersPreviousGUI(UUID p) {
        return guiTracker.get(p).getPreviousGUI();
    }

    public static GUI replacePlayersGUI(McMMOPlayer p, GUI newGUI) {
        setPlayersCurrentGUI(p.getUuid(), newGUI, true);
        return newGUI;
    }
    public static GUI replacePlayersGUI(Player p, GUI newGUI) {
        setPlayersCurrentGUI(p.getUniqueId(), newGUI, true);
        return newGUI;
    }

    public static GUI replacePlayersGUI(UUID p, GUI newGUI) {
        setPlayersCurrentGUI(p, newGUI, true);
        return newGUI;
    }

    public static void trackPlayer(McMMOPlayer p, GUI gui) {
        GUITrackerBit bit = new GUITrackerBit(gui);
        guiTracker.put(p.getUuid(), bit);
    }

    public static void trackPlayer(Player p, GUI gui) {
        GUITrackerBit bit = new GUITrackerBit(gui);
        guiTracker.put(p.getUniqueId(), bit);
    }

    public static void trackPlayer(UUID p, GUI gui) {
        GUITrackerBit bit = new GUITrackerBit(gui);
        guiTracker.put(p, bit);
    }

    private static void setPlayersPreviousGUI(UUID uuid, GUI old) {
        guiTracker.get(uuid).setPreviousGUI(old);
    }

    private static void setPlayersCurrentGUI(UUID uuid, GUI current, boolean replaceOld){
        if(replaceOld){
            GUI old = guiTracker.get(uuid).getCurrentGUI();
            setPlayersPreviousGUI(uuid, old);
            guiTracker.get(uuid).setCurrentGUI(current);
        }
        else{
            guiTracker.get(uuid).setCurrentGUI(current);
            setPlayersPreviousGUI(uuid, null);
        }
    }

}
