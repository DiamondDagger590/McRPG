package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.gui.GUI;
import us.eunoians.mcmmox.gui.GUIEventBinder;
import us.eunoians.mcmmox.gui.GUITracker;

public class InvClickEvent implements Listener {

  //TODO for Diamond to do. Overhaul old system and recreate it to be functional for what we want
  @EventHandler
  public void invClickEvent(InventoryClickEvent e) {
        /*
        Player p = (Player) e.getWhoClicked();
        if(GUITracker.isPlayerTracked(p)) {
            e.setCancelled(true);
            if(e.getCurrentItem() == null) {
                return;
            }
            GUI currentGUI = GUITracker.getPlayersGUI(p);
            for(GUIEventBinder eventBinder : currentGUI.getGUIBuilder().getBoundEvents()) {
                String[] events = eventBinder.getBoundEvent().split("/");
                for(String ee : events) {
                    if(e.getSlot() == eventBinder.getSlot()) {
                        String[] eventInfo = ee.split(":");
                        String event = eventInfo[0];
                        if(event.equalsIgnoreCase("Permission")) {
                            String perm = eventInfo[1];
                            if(!p.hasPermission(perm)) {
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                p.sendMessage(Methods.color(Main.getPluginPrefix() + config.getString("Messages.Util.NoPerms")));
                                return;
                            }
                            else {
                                continue;
                            }
                        }
                        else if(event.equalsIgnoreCase("Condition")) {
                            String condition = eventInfo[1];
                            if(condition.equalsIgnoreCase("BoosterAmount")) {
                                BoosterType type = BoosterType.fromString(eventInfo[2]);
                                boolean isLucky = false;
                                if(eventInfo[3].equalsIgnoreCase("Lucky")) {
                                    isLucky = true;
                                }
                                BoosterPlayer bp = BoosterPlayerManager.getInstance().getBoosterPlayer(p.getUniqueId());
                                if(isLucky) {
                                    if(eventInfo[4].equalsIgnoreCase("NotZero") && !(bp.getLuckyBoosterAmount(type) > 0)) {
                                        currentGUI.setShouldClearData(true);
                                        p.closeInventory();
                                        GUITracker.stopTrackingPlayer(p);
                                        p.sendMessage(Methods.color(Main.getPluginPrefix() + config.getString("Messages.Util.ConditionNotMet")));
                                        return;
                                    }
                                    else {
                                        continue;
                                    }
                                }
                                else {
                                    if(eventInfo[4].equalsIgnoreCase("NotZero") && !(bp.getNormalBoosterAmount(type) > 0)) {
                                        currentGUI.setShouldClearData(true);
                                        p.closeInventory();
                                        GUITracker.stopTrackingPlayer(p);
                                        p.sendMessage(Methods.color(Main.getPluginPrefix() + config.getString("Messages.Util.ConditionNotMet")));
                                        return;
                                    }
                                    else {
                                        continue;
                                    }
                                }
                            }
                        }
                        else if(event.equalsIgnoreCase("Command")) {
                            String sender = eventInfo[1];
                            if(sender.equalsIgnoreCase("console")) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), eventInfo[2].replaceAll("%Player%", p.getName()));
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                continue;
                            }
                            else if(sender.equalsIgnoreCase("player")){
                                p.performCommand(eventInfo[2]);
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                continue;
                            }
                        }
                        else if(event.equalsIgnoreCase("redeem")) {
                            AmountGUI gui = (AmountGUI) currentGUI;
                            if(eventInfo[1].equalsIgnoreCase("Custom")) {
                                ListenerBit bit = new ListenerBit(p.getUniqueId(), gui.getType(), gui.getObject());
                                ChatListener.listen(bit);
                                p.sendMessage(Methods.color(Main.getPluginPrefix() + "&aYou have 15 seconds to input an amount."));
                            }
                            else if(eventInfo[1].equalsIgnoreCase("all")) {
                                int amount = BoosterPlayerManager.getInstance().getBoosterPlayer(p.getUniqueId()).getExpAmount(gui.getType());
                                gui.redeemExp(amount);
                            }
                            else {
                                int amount = Integer.parseInt(eventInfo[1]);
                                gui.redeemExp(amount);
                            }
                            currentGUI.setShouldClearData(true);
                            p.closeInventory();
                            GUITracker.stopTrackingPlayer(p);
                            return;
                        }
                        else if(event.equalsIgnoreCase("close")) {
                            currentGUI.setShouldClearData(true);
                            p.closeInventory();
                            GUITracker.stopTrackingPlayer(p);
                            return;
                        }
                        else if(event.equalsIgnoreCase("back")) {
                            if(GUITracker.doesPlayerHavePrevious(p)) {
                                GUI previousGUI = GUITracker.getPlayersPreviousGUI(p);
                                currentGUI.setShouldClearData(false);
                                p.openInventory(previousGUI.getGUIBuilder().getInventory());
                                GUITracker.replacePlayersGUI(p, previousGUI);
                                return;
                            }
                            else {
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                return;
                            }
                        }
                        else if(event.equalsIgnoreCase("activate")) {
                            if(currentGUI instanceof ConfirmationGUI) {
                                ConfirmationGUI confirm = (ConfirmationGUI) currentGUI;
                                confirm.confirmBooster();
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                return;
                            }
                            else {
                                currentGUI.setShouldClearData(true);
                                p.closeInventory();
                                GUITracker.stopTrackingPlayer(p);
                                return;
                            }
                        }
                        else if(event.contains("Open")) {
                            String[] eventData = eventInfo;
                            GUIBuilder builder = new GUIBuilder(eventData[1]);
                            BoosterPlayer player = BoosterPlayerManager.getInstance().getBoosterPlayer(p.getUniqueId());
                            GUI gui = null;
                            if(eventData[1].equalsIgnoreCase("ConfirmationGUI")) {
                                BoosterType type = BoosterType.fromString(eventData[2]);
                                boolean isLucky = false;
                                if(eventData[3].equalsIgnoreCase("Lucky")) {
                                    isLucky = true;
                                }
                                gui = new ConfirmationGUI(player, builder, type, isLucky);
                                ((ConfirmationGUI) gui).parseLore(type);
                            }
                            else if(eventData[1].equalsIgnoreCase("AllGUI")) {
                                AmountGUI current = (AmountGUI) GUITracker.getPlayersGUI(p);
                                AmountGUI newGUI = new AmountGUI("AllGUI", current.getBoosterPlayer(), current.getType(), current.getObject());
                                current.setShouldClearData(false);
                                p.openInventory(newGUI.getGUIBuilder().getInventory());
                                GUITracker.replacePlayersGUI(p, newGUI);
                                return;
                            }
                            else if(eventData[1].equalsIgnoreCase("AmountGUI")) {
                                String luckyObject = eventData[2];
                                LuckyObject objec = null;
                                BoosterType type = BoosterType.VANILLA;
                                if(LuckyJobs.isAJob(luckyObject)) {
                                    objec = LuckyJobs.fromString(luckyObject);
                                    type = BoosterType.JOBS;
                                }
                                else if(LuckySkills.isASkill(luckyObject)) {
                                    objec = LuckySkills.fromString(luckyObject);
                                    type = BoosterType.MCMMO;
                                }
                                gui = new AmountGUI(player, type, objec);
                            }
                            else{
                                String possibleType = eventData[1];
                                if(possibleType.equalsIgnoreCase("SwitchGUI") || possibleType.equalsIgnoreCase("McMMOGUI") || possibleType.equalsIgnoreCase("VanillaGUI") || possibleType.equalsIgnoreCase("JobsGUI")) {
                                    BoosterType actualType = BoosterType.fromString(possibleType.replace("GUI", ""));
                                    builder.setNewInventory(Methods.parseGUILore(builder.getInventory(), player, actualType));
                                }

                                gui = new MiscGUI(builder, player);
                            }
                            currentGUI.setShouldClearData(false);
                            p.openInventory(builder.getInventory());
                            GUITracker.replacePlayersGUI(p, gui);
                            return;
                        }
                    }

                }

            }
        }*/
  }

}
