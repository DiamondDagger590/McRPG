package us.eunoians.mcrpg.events.vanilla;

import de.tr7zw.changeme.nbtapi.NBTItem;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityAddToLoadoutEvent;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityRemovedFromLoadoutEvent;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUpgradeEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.exceptions.PartyNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.api.util.blood.BloodManager;
import us.eunoians.mcrpg.api.util.brewing.PotionUtils;
import us.eunoians.mcrpg.api.util.brewing.standmeta.BrewingGUI;
import us.eunoians.mcrpg.gui.AbilityOverrideGUI;
import us.eunoians.mcrpg.gui.AcceptAbilityGUI;
import us.eunoians.mcrpg.gui.AllGUI;
import us.eunoians.mcrpg.gui.AmountGUI;
import us.eunoians.mcrpg.gui.EditDefaultAbilitiesGUI;
import us.eunoians.mcrpg.gui.EditLoadoutGUI;
import us.eunoians.mcrpg.gui.EditLoadoutSelectGUI;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.GUIEventBinder;
import us.eunoians.mcrpg.gui.GUITracker;
import us.eunoians.mcrpg.gui.HomeGUI;
import us.eunoians.mcrpg.gui.PartyBankGUI;
import us.eunoians.mcrpg.gui.PartyMainGUI;
import us.eunoians.mcrpg.gui.PartyMemberGUI;
import us.eunoians.mcrpg.gui.PartyPrivateBankGUI;
import us.eunoians.mcrpg.gui.PartyRoleGUI;
import us.eunoians.mcrpg.gui.PartyUpgradesGUI;
import us.eunoians.mcrpg.gui.RedeemStoredGUI;
import us.eunoians.mcrpg.gui.RemoteTransferGUI;
import us.eunoians.mcrpg.gui.ReplaceSkillsGUI;
import us.eunoians.mcrpg.gui.SelectReplaceGUI;
import us.eunoians.mcrpg.gui.SettingsGUI;
import us.eunoians.mcrpg.gui.SkillGUI;
import us.eunoians.mcrpg.gui.SubSkillGUI;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyManager;
import us.eunoians.mcrpg.party.PartyMember;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.AbilityType;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.PartyPermissions;
import us.eunoians.mcrpg.types.PartyRoles;
import us.eunoians.mcrpg.types.PartyUpgrades;
import us.eunoians.mcrpg.types.RedeemType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SuspiciousMethodCalls")
public class InvClickEvent implements Listener{
  
  static Map<UUID, BukkitTask> antiCheatTasks = new HashMap<>();
  private FileConfiguration config;
  
  public InvClickEvent(McRPG plugin){
    config = McRPG.getInstance().getLangFile();
    
  }
  
  @SuppressWarnings("Duplicates")
  @EventHandler(priority = EventPriority.HIGHEST)
  public void invClickEvent(InventoryClickEvent e){
    if(PlayerManager.isPlayerFrozen(e.getWhoClicked().getUniqueId())){
      return;
    }
    FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
    Player p = (Player) e.getWhoClicked();
    
    if(e.getCursor() != null && e.getCursor().getType() == Material.REDSTONE && e.getCurrentItem() != null){
      ItemStack cursor = e.getCursor();
      NBTItem nbtItem = new NBTItem(cursor);
      if(nbtItem.hasKey("McRPGBlood")){
        BloodManager.BloodType bloodType = BloodManager.BloodType.getFromID(nbtItem.getString("BloodType"));
        if(bloodType.isMaterialApplicable(e.getCurrentItem().getType())){
          ItemStack current = e.getCurrentItem();
          NBTItem currentNBT = new NBTItem(current);
          if(currentNBT.hasKey("McRPGBloodItem")){
            return;
          }
          currentNBT.setBoolean("McRPGBloodItem", true);
          currentNBT.setDouble("ShatterChance", BloodManager.getInstance().getBloodWrapper(bloodType).getItemShatterChance());
          currentNBT.setInteger("ExpBoost", nbtItem.getInteger("ExpBoost"));
          current = currentNBT.getItem();
          List<String> lore = current.hasItemMeta() && current.getItemMeta().hasLore() ? current.getItemMeta().getLore() : new ArrayList<>();
          lore.add(Methods.color(p, McRPG.getInstance().getLangFile().getString("Messages.Blood.BloodLore").replace("%ExpBoost%", Integer.toString(currentNBT.getInteger("ExpBoost")))));
          ItemMeta itemMeta = current.getItemMeta();
          itemMeta.setLore(lore);
          current.setItemMeta(itemMeta);
          e.getClickedInventory().setItem(e.getSlot(), current);
          e.getCursor().setAmount(e.getCursor().getAmount() - 1);
          p.updateInventory();
          e.setCancelled(true);
          return;
        }
      }
    }
    
    //If this is a gui
    if(GUITracker.isPlayerTracked(p)){
      //Cancel event
      e.setCancelled(true);
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(p.getUniqueId());
      }catch(McRPGPlayerNotFoundException exception){
        return;
      }
      
      GUI currentGUI = GUITracker.getPlayersGUI(p);
      
      //Brewing GUI comes before player inventory checks
      if(currentGUI instanceof BrewingGUI){
        if(McRPG.getInstance().isNcpEnabled()){
          UUID uuid = p.getUniqueId();
          NCPExemptionManager.exemptPermanently(uuid);
          if(antiCheatTasks.containsKey(uuid)){
            antiCheatTasks.remove(uuid).cancel();
          }
          BukkitTask bukkitTask = new BukkitRunnable(){
            @Override
            public void run(){
              NCPExemptionManager.unexempt(uuid);
              antiCheatTasks.remove(uuid).cancel();
            }
          }.runTaskLater(McRPG.getInstance(), 5 * 20);
          antiCheatTasks.put(uuid, bukkitTask);
        }
        BrewingGUI brewingGUI = (BrewingGUI) currentGUI;
        if(e.getClickedInventory() == null){
          return;
        }
        ItemStack itemStack = e.getClickedInventory().getItem(e.getSlot());
        if(itemStack != null){
          NBTItem nbtItem = new NBTItem(itemStack);
        }
        if(e.getClickedInventory() instanceof PlayerInventory){
          e.setCancelled(false);
          
          //Handle ingredients
          if(e.getCurrentItem() != null && PotionUtils.isIngredient(e.getCurrentItem()) && e.getClick() == ClickType.SHIFT_LEFT &&
               (brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE || brewingGUI.getIngredient().isSimilar(e.getCurrentItem()))){
            int currentAmount = brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ? 0 : brewingGUI.getIngredient().getAmount();
            int maxSize = brewingGUI.getIngredient().getMaxStackSize();
            int maxDiff = maxSize - currentAmount;
            int actualDiff = Math.min(e.getCurrentItem().getAmount(), maxDiff);
            if(brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
              ItemStack newIngredient = new ItemStack(e.getCurrentItem().getType(), actualDiff);
              brewingGUI.setIngredient(newIngredient);
            }
            else{
              brewingGUI.getIngredient().setAmount(brewingGUI.getIngredient().getAmount() + actualDiff);
            }
            brewingGUI.updateIngredient();
            e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - actualDiff);
            e.setCancelled(true);
          }
          else if(e.getCurrentItem() != null && PotionUtils.isPotionItem(e.getCurrentItem()) && e.getClick() == ClickType.SHIFT_LEFT){
            int slot = brewingGUI.getFirstEmptySlot();
            if(slot == -1){
              return;
            }
            if(e.getCurrentItem().getAmount() > 1){
              ItemStack newPotion = e.getCurrentItem().clone();
              newPotion.setAmount(1);
              brewingGUI.setPotion(newPotion, slot);
              e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
            }
            else{
              brewingGUI.setPotion(e.getCurrentItem(), slot);
              e.getCurrentItem().setAmount(0);
            }
            e.setCancelled(true);
          }
          if(brewingGUI.checkForBrewingTask()){
            brewingGUI.setPlayerStartingBrew((Player) e.getWhoClicked());
            BukkitTask brewingTask = brewingGUI.startBrewTask();
          }
          return;
        }
        else{
          e.setCancelled(true);
          if(e.getSlot() == 0){
            if((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.NUMBER_KEY) && p.getInventory().firstEmpty() != -1
                 && !(brewingGUI.getFuel() == null || brewingGUI.getFuel().getType() == Material.AIR || brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE)){
              e.setCancelled(false);
              brewingGUI.setFuel(new ItemStack(Material.AIR));
              new BukkitRunnable(){
                @Override
                public void run(){
                  brewingGUI.resetFuelGlass();
                }
              }.runTaskLater(McRPG.getInstance(), 1);
              brewingGUI.save();
              return;
            }
            if(e.getCursor() == null || e.getCursor().getType() == Material.AIR){
              if(brewingGUI.getFuel().getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                ItemStack fuel = brewingGUI.getFuel().clone();
                brewingGUI.resetFuelGlass();
                e.setCursor(fuel);
                /*new BukkitRunnable(){
                  @Override
                  public void run(){
                    e.setCursor(fuel);
                  }
                }.runTaskLater(McRPG.getInstance(), 1);*/
              }
              brewingGUI.save();
              return;
            }
            else if(PotionUtils.isFuel(e.getCursor())){
              if(brewingGUI.getFuel().getAmount() < 64 &&
                   (brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE || brewingGUI.getFuel().isSimilar(e.getCursor()))){
                if(e.getClick() == ClickType.LEFT){
                  int currentAmount = brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ? 0 : brewingGUI.getFuel().getAmount();
                  int maxSize = brewingGUI.getFuel().getMaxStackSize();
                  int maxDiff = maxSize - currentAmount;
                  int actualDiff = Math.min(e.getCursor().getAmount(), maxDiff);
                  if(brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                    ItemStack newFuel = new ItemStack(e.getCursor().getType(), actualDiff);
                    brewingGUI.setFuel(newFuel);
                  }
                  else{
                    brewingGUI.getFuel().setAmount(brewingGUI.getFuel().getAmount() + actualDiff);
                  }
                  e.getCursor().setAmount(e.getCursor().getAmount() - actualDiff);
                  brewingGUI.updateFuelItems();
                }
                if(e.getClick() == ClickType.RIGHT){
                  int currentAmount = brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ? 0 : brewingGUI.getFuel().getAmount();
                  if(currentAmount == 64){
                    return;
                  }
                  if(brewingGUI.getFuel().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                    ItemStack newFuel = new ItemStack(e.getCursor().getType(), 1);
                    brewingGUI.setFuel(newFuel);
                  }
                  else{
                    brewingGUI.getFuel().setAmount(brewingGUI.getFuel().getAmount() + 1);
                  }
                  e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                  brewingGUI.updateFuelItems();
                }
              }
              if(brewingGUI.checkForBrewingTask()){
                brewingGUI.startBrewTask();
              }
              brewingGUI.save();
            }
          }
          else if(e.getSlot() == 13){
            if((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.NUMBER_KEY) && p.getInventory().firstEmpty() != -1
                 && !(brewingGUI.getIngredient() == null || brewingGUI.getIngredient().getType() == Material.AIR || brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE)){
              e.setCancelled(false);
              new BukkitRunnable(){
                @Override
                public void run(){
                  brewingGUI.resetIngredientGlass();
                }
              }.runTaskLater(McRPG.getInstance(), 1);
              return;
            }
            if(e.getCursor() == null || e.getCursor().getType() == Material.AIR){
              if(brewingGUI.getIngredient().getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                ItemStack ingredient = brewingGUI.getIngredient().clone();
                brewingGUI.resetIngredientGlass();
                e.setCursor(ingredient);
                /*new BukkitRunnable(){
                  @Override
                  public void run(){
                    e.setCursor(ingredient);
                  }
                }.runTaskLater(McRPG.getInstance(), 1);*/
                brewingGUI.updateIngredient();
              }
              return;
            }
            else if(PotionUtils.isIngredient(e.getCursor())){
              if(brewingGUI.getIngredient().getAmount() < 64 &&
                   (brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE || brewingGUI.getIngredient().isSimilar(e.getCursor()))){
                if(e.getClick() == ClickType.LEFT){
                  int currentAmount = brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ? 0 : brewingGUI.getIngredient().getAmount();
                  int maxSize = brewingGUI.getIngredient().getMaxStackSize();
                  int maxDiff = maxSize - currentAmount;
                  int actualDiff = Math.min(e.getCursor().getAmount(), maxDiff);
                  if(brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                    ItemStack newIngredient = new ItemStack(e.getCursor().getType(), actualDiff);
                    brewingGUI.setIngredient(newIngredient);
                  }
                  else{
                    brewingGUI.getIngredient().setAmount(brewingGUI.getIngredient().getAmount() + actualDiff);
                    brewingGUI.setIngredient(brewingGUI.getIngredient());
                  }
                  e.getCursor().setAmount(e.getCursor().getAmount() - actualDiff);
                  brewingGUI.updateIngredient();
                }
                if(e.getClick() == ClickType.RIGHT){
                  int currentAmount = brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE ? 0 : brewingGUI.getIngredient().getAmount();
                  if(currentAmount == 64){
                    return;
                  }
                  if(brewingGUI.getIngredient().getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                    ItemStack newIngredient = new ItemStack(e.getCursor().getType(), 1);
                    brewingGUI.setIngredient(newIngredient);
                  }
                  else{
                    brewingGUI.getIngredient().setAmount(brewingGUI.getIngredient().getAmount() + 1);
                    //TODO fix this
                    brewingGUI.setIngredient(brewingGUI.getIngredient());
                  }
                  e.getCursor().setAmount(e.getCursor().getAmount() - 1);
                  brewingGUI.updateIngredient();
                }
              }
              else{
                ItemStack temp = brewingGUI.getIngredient().clone();
                ItemStack old = e.getCursor().clone();
                brewingGUI.setIngredient(old);
                e.setCursor(temp);
              }
              brewingGUI.setLastInteractedPlayer(p);
              if(brewingGUI.checkForBrewingTask()){
                brewingGUI.startBrewTask();
              }
            }
          }
          else if(brewingGUI.isPotionSlot(e.getSlot())){
            if((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.NUMBER_KEY) && p.getInventory().firstEmpty() != -1
                 && !(brewingGUI.getPotion(e.getSlot()) == null || brewingGUI.getPotion(e.getSlot()).getType() == Material.AIR || brewingGUI.getPotion(e.getSlot()).getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE)){
              e.setCancelled(false);
              new BukkitRunnable(){
                @Override
                public void run(){
                  brewingGUI.removePotion(e.getSlot());
                }
              }.runTaskLater(McRPG.getInstance(), 1);
              return;
            }
            if(e.getCursor() == null || e.getCursor().getType() == Material.AIR){
              if(brewingGUI.getPotion(e.getSlot()).getType() != Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                ItemStack potion = brewingGUI.getPotion(e.getSlot()).clone();
                brewingGUI.removePotion(e.getSlot());
                e.setCursor(potion);
                /*new BukkitRunnable(){
                  @Override
                  public void run(){
                    e.setCursor(potion);
                  }
                }.runTaskLater(McRPG.getInstance(), 1);*/
              }
              return;
            }
            else if(PotionUtils.isPotionItem(e.getCursor())){
              if(e.getClick() == ClickType.LEFT){
                if(brewingGUI.getPotion(e.getSlot()).getType() == Material.LIGHT_BLUE_STAINED_GLASS_PANE){
                  ItemStack potion = e.getCursor().clone();
                  potion.setAmount(1);
                  brewingGUI.setPotion(potion, e.getSlot());
                  if(brewingGUI.checkForBrewingTask()){
                    brewingGUI.startBrewTask();
                  }
                }
                else{
                  if(e.getCursor().getAmount() == 1){
                    ItemStack temp = e.getInventory().getItem(e.getSlot()).clone();
                    ItemStack potion = e.getCursor().clone();
                    brewingGUI.setPotion(potion, e.getSlot());
                    e.setCursor(temp);
                    if(brewingGUI.checkForBrewingTask()){
                      brewingGUI.startBrewTask();
                    }
                  }
                  return;
                }
                e.getCursor().setAmount(e.getCursor().getAmount() - 1);
              }
            }
          }
          else if(brewingGUI.isSpecialItemSlot(e.getSlot()) && (e.getCursor() == null || e.getCursor().getType() == Material.AIR)){
            if((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.NUMBER_KEY) && p.getInventory().firstEmpty() != -1
                 && !(brewingGUI.getSpecialItem(e.getSlot()) == null || brewingGUI.getSpecialItem(e.getSlot()).getType() == Material.AIR || brewingGUI.getSpecialItem(e.getSlot()).getType() == Material.PURPLE_STAINED_GLASS_PANE)){
              e.setCancelled(false);
              new BukkitRunnable(){
                @Override
                public void run(){
                  brewingGUI.removeSpecialReward(e.getSlot());
                }
              }.runTaskLater(McRPG.getInstance(), 1);
              return;
            }
            if(brewingGUI.getSpecialItem(e.getSlot()) != null){
              e.setCursor(brewingGUI.getSpecialItem(e.getSlot()));
              brewingGUI.removeSpecialReward(e.getSlot());
            }
          }
        }
        return;
      }
      
      if(currentGUI instanceof PartyBankGUI || currentGUI instanceof PartyPrivateBankGUI){
        Party party = currentGUI instanceof PartyBankGUI ? ((PartyBankGUI) currentGUI).getParty() : ((PartyPrivateBankGUI) currentGUI).getParty();
        int maxSlot = PartyUpgrades.getPrivateBankSizeAtTier(party.getUpgradeTier(PartyUpgrades.PRIVATE_BANK_SIZE));
        if(currentGUI instanceof PartyPrivateBankGUI){
          if(e.getSlot() > maxSlot && !(e.getClickedInventory() instanceof PlayerInventory)){
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 2);
            return;
          }
          else if(e.getClickedInventory() instanceof PlayerInventory && e.getClick() == ClickType.SHIFT_LEFT){
            if(currentGUI.getGui().getInv().firstEmpty() > maxSlot){
              e.setCancelled(true);
              return;
            }
          }
          else if(e.getCurrentItem() != null && e.getCurrentItem().equals(Party.getFillerGlass())){
            e.setCancelled(true);
            return;
          }
        }
        e.setCancelled(false);
        return;
      }
      
      //Cuz null errors are fun
      if(e.getCurrentItem() == null){
        return;
      }
      //Ignore player inventory
      if(e.getClickedInventory() instanceof PlayerInventory){
        return;
      }
      //Overriding abilities gui, used for active abilities
      if(currentGUI instanceof AbilityOverrideGUI){
        AbilityOverrideGUI overrideGUI = (AbilityOverrideGUI) currentGUI;
        int slot = e.getSlot();
        if(slot == 16){
          mp.removePendingAbilityUnlock((UnlockedAbilities) overrideGUI.getReplaceAbility().getGenericAbility());
          mp.saveData();
          p.closeInventory();
          return;
        }
        else{
          if(slot == 10){
            AbilityAddToLoadoutEvent abilityAddToLoadoutEvent = new AbilityAddToLoadoutEvent(mp, overrideGUI.getReplaceAbility());
            Bukkit.getPluginManager().callEvent(abilityAddToLoadoutEvent);
            if(abilityAddToLoadoutEvent.isCancelled()){
              return;
            }
            AbilityRemovedFromLoadoutEvent abilityRemovedFromLoadoutEvent = new AbilityRemovedFromLoadoutEvent(mp, overrideGUI.getAbiltyToReplace());
            Bukkit.getPluginManager().callEvent(abilityRemovedFromLoadoutEvent);
            if(abilityRemovedFromLoadoutEvent.isCancelled()){
              return;
            }
            if(mp.getCooldown(overrideGUI.getAbiltyToReplace().getGenericAbility().getSkill()) != -1){
              mp.removeAbilityOnCooldown((UnlockedAbilities) overrideGUI.getAbiltyToReplace().getGenericAbility());
            }
            mp.replaceAbility((UnlockedAbilities) overrideGUI.getAbiltyToReplace().getGenericAbility(), (UnlockedAbilities) overrideGUI.getReplaceAbility().getGenericAbility());
            mp.removePendingAbilityUnlock((UnlockedAbilities) overrideGUI.getReplaceAbility().getGenericAbility());
            mp.saveData();
            p.closeInventory();
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", overrideGUI.getReplaceAbility().getGenericAbility().getName())));
            return;
          }
        }
      }
      
      //Selecting what loadout to edit
      if(currentGUI instanceof EditLoadoutSelectGUI){
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_SELECT_GUI);
        int slot = e.getSlot();
        if(slot == guiConfig.getInt("DefaultAbilitiesItem.Slot")){
          EditDefaultAbilitiesGUI editDefaultAbilitiesGUI = new EditDefaultAbilitiesGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(editDefaultAbilitiesGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editDefaultAbilitiesGUI);
        }
        else if(slot == guiConfig.getInt("ReplaceAbilitiesItem.Slot")){
          if(mp.getEndTimeForReplaceCooldown() != 0){
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.ReplaceCooldownPending.Sound")),
              Float.parseFloat(soundFile.getString("Sounds.Misc.ReplaceCooldownPending.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.ReplaceCooldownPending.Pitch")));
            return;
          }
          ReplaceSkillsGUI replaceSkillsGUI = new ReplaceSkillsGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(replaceSkillsGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, replaceSkillsGUI);
        }
        else if(slot == guiConfig.getInt("UnlockedAbilitiesItem.Slot")){
          EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.TOGGLE);
          currentGUI.setClearData(false);
          p.openInventory(editLoadoutGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
        }
        else if(slot == guiConfig.getInt("BackButton.Slot")){
          if(GUITracker.doesPlayerHavePrevious(mp)){
            currentGUI.setClearData(false);
            GUI home = new HomeGUI(mp);
            home.setClearData(true);
            p.openInventory(home.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, home);
            return;
          }
        }
      }
      
      if(currentGUI instanceof SettingsGUI){
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SETTINGS_GUI);
        
        //Handle the McDisplay setting
        if(e.getSlot() == guiConfig.getInt("ChangeDisplaySettings.Slot")){
          ItemStack displayItem = new ItemStack(Material.BLAZE_ROD);
          ItemMeta displayMeta = displayItem.getItemMeta();
          
          //Deal with each type specifically
          if(mp.getDisplayType() == DisplayType.ACTION_BAR){
            displayItem.setType(Material.OAK_SIGN);
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.ScoreBoard")));
            mp.setDisplayType(DisplayType.SCOREBOARD);
          }
          else if(mp.getDisplayType() == DisplayType.SCOREBOARD){
            displayItem.setType(Material.DRAGON_HEAD);
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.BossBar")));
            mp.setDisplayType(DisplayType.BOSS_BAR);
          }
          else if(mp.getDisplayType() == DisplayType.BOSS_BAR){
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.ActionBar")));
            mp.setDisplayType(DisplayType.ACTION_BAR);
          }
          
          //Update the lore and inventory
          displayMeta.setLore(Methods.colorLore(guiConfig.getStringList("ChangeDisplaySettings.Lore")));
          displayItem.setItemMeta(displayMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), displayItem);
          p.updateInventory();
        }
        
        //Handle the keep hand empty setting (ur welcome unarmed users)
        else if(e.getSlot() == guiConfig.getInt("KeepHandEmpty.Slot")){
          ItemStack itemPickup = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta itemPickupMeta = itemPickup.getItemMeta();
          
          //Handle changing of the setting lore
          if(!mp.isKeepHandEmpty()){
            itemPickupMeta.setDisplayName(Methods.color(guiConfig.getString("KeepHandEmpty.Enabled")));
          }
          else{
            itemPickup.setType(Material.RED_STAINED_GLASS_PANE);
            itemPickupMeta.setDisplayName(Methods.color(guiConfig.getString("KeepHandEmpty.Disabled")));
          }
          
          //Invert setting and update the gui
          mp.setKeepHandEmpty(!mp.isKeepHandEmpty());
          itemPickupMeta.setLore(Methods.colorLore(guiConfig.getStringList("KeepHandEmpty.Lore")));
          itemPickup.setItemMeta(itemPickupMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), itemPickup);
          p.updateInventory();
        }
        
        //Handle ignore tips setting (but why? :( )
        else if(e.getSlot() == guiConfig.getInt("IgnoreTips.Slot")){
          ItemStack tipItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta tipItemMeta = tipItem.getItemMeta();
  
          //Handle changing of the setting lore
          if(!mp.isIgnoreTips()){
            tipItemMeta.setDisplayName(Methods.color(guiConfig.getString("IgnoreTips.Enabled")));
          }
          else{
            tipItem.setType(Material.RED_STAINED_GLASS_PANE);
            tipItemMeta.setDisplayName(Methods.color(guiConfig.getString("IgnoreTips.Disabled")));
          }
  
          //Invert setting and update the gui
          mp.setIgnoreTips(!mp.isIgnoreTips());
          tipItemMeta.setLore(Methods.colorLore(guiConfig.getStringList("IgnoreTips.Lore")));
          tipItem.setItemMeta(tipItemMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), tipItem);
          p.updateInventory();
        }
        
        //Handle changing the health display
        else if(e.getSlot() == guiConfig.getInt("MobHealthDisplay.Slot")){
          MobHealthbarUtils.MobHealthbarType healthbarType = mp.getHealthbarType();
          ItemStack healthItem = new ItemStack(Material.BUBBLE_CORAL_BLOCK);
          ItemMeta healthMeta = healthItem.getItemMeta();
          
          //Change the typing of this setting and change the display name
          if(healthbarType == MobHealthbarUtils.MobHealthbarType.DISABLED){
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.Bar")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.BAR);
          }
          else if(healthbarType == MobHealthbarUtils.MobHealthbarType.HEARTS){
            healthItem.setType(Material.DEAD_FIRE_CORAL_BLOCK);
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.None")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.DISABLED);
          }
          else if(healthbarType == MobHealthbarUtils.MobHealthbarType.BAR){
            healthItem.setType(Material.FIRE_CORAL_BLOCK);
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.Hearts")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.HEARTS);
          }
          
          //Update the lore and inventory
          healthMeta.setLore(Methods.colorLore(guiConfig.getStringList("MobHealthDisplay.Lore")));
          healthItem.setItemMeta(healthMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), healthItem);
          p.updateInventory();
        }
        
        //Handle auto denying new abilities (mostly for admins)
        else if(e.getSlot() == guiConfig.getInt("AutoDenyNewAbilities.Slot")){
          ItemStack autoDenyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta autoDenyItemMeta = autoDenyItem.getItemMeta();
          
          //Handle updating the display name and material if needed
          if(!mp.isAutoDeny()){
            autoDenyItemMeta.setDisplayName(Methods.color(guiConfig.getString("AutoDenyNewAbilities.Enabled")));
          }
          else{
            autoDenyItem.setType(Material.RED_STAINED_GLASS_PANE);
            autoDenyItemMeta.setDisplayName(Methods.color(guiConfig.getString("AutoDenyNewAbilities.Disabled")));
          }
          
          //Invert setting and update gui
          mp.setAutoDeny(!mp.isAutoDeny());
          autoDenyItemMeta.setLore(Methods.colorLore(guiConfig.getStringList("AutoDenyNewAbilities.Lore")));
          autoDenyItem.setItemMeta(autoDenyItemMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), autoDenyItem);
          p.updateInventory();
        }
        //Handle legacy files
        else if(e.getSlot() == guiConfig.getInt("EmptyOffHand.Slot", 12)){
          ItemStack emptyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta emptyMeta = emptyItem.getItemMeta();
          
          //Update display name and material if needed
          if(!mp.isRequireEmptyOffHand()){
            emptyMeta.setDisplayName(Methods.color(guiConfig.getString("EmptyOffHand.Enabled", "&aOff Hand Must Be Empty")));
          }
          else{
            emptyItem.setType(Material.RED_STAINED_GLASS_PANE);
            emptyMeta.setDisplayName(Methods.color(guiConfig.getString("EmptyOffHand.Disabled", "&cOff Hand Can Have An Item In It")));
          }
          
          //Invert setting and update inventory
          mp.setRequireEmptyOffHand(!mp.isRequireEmptyOffHand());
          List<String> lore = guiConfig.contains("EmptyOffHand.Lore") ? guiConfig.getStringList("EmptyOffHand.Lore") : Arrays.asList("&eIf enabled, then in order to ready", "&eabilities, your offhand must be empty.");
          emptyMeta.setLore(Methods.colorLore(lore));
          emptyItem.setItemMeta(emptyMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), emptyItem);
          p.updateInventory();
        }
        
        //Handle a locked hotbar slot that should remain empty
        else if(e.getSlot() == guiConfig.getInt("UnarmedIgnoreSlot.Slot", 14)){
          ItemStack ignoreItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta ignoreMeta = ignoreItem.getItemMeta();
          
          //Some fun math to handle incrementing slot/disabling
          mp.setUnarmedIgnoreSlot(mp.getUnarmedIgnoreSlot() == 8 ? -1 : mp.getUnarmedIgnoreSlot() + 1);
          
          //Update display anme and material as needed
          if(mp.getUnarmedIgnoreSlot() != -1){
            ignoreMeta.setDisplayName(Methods.color(guiConfig.getString("UnarmedIgnoreSlot.Enabled", "&aSlot #%Slot% Is Being Kept Empty").replace("%Slot%", Integer.toString(mp.getUnarmedIgnoreSlot() + 1))));
          }
          else{
            ignoreItem.setType(Material.RED_STAINED_GLASS_PANE);
            ignoreMeta.setDisplayName(Methods.color(guiConfig.getString("UnarmedIgnoreSlot.Disabled", "&cCurrently No Slot Is Being Kept Empty")));
          }
          
          //Update the lore and inventory
          List<String> lore = guiConfig.contains("UnarmedIgnoreSlot.Lore") ? guiConfig.getStringList("UnarmedIgnoreSlot.Lore") : Arrays.asList("&eIf enabled, this setting will keep picked up", "&eitems from going into the selected slo");
          ignoreMeta.setLore(Methods.colorLore(lore));
          ignoreItem.setItemMeta(ignoreMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), ignoreItem);
          p.updateInventory();
        }
        
        else if(e.getSlot() == guiConfig.getInt("AutoAcceptPartyTP.Slot", 16)){
          ItemStack acceptTpItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta acceptTpMeta = acceptTpItem.getItemMeta();
  
          //Update display name and material if needed
          if(!mp.isAutoAcceptPartyInvites()){
            acceptTpMeta.setDisplayName(Methods.color(guiConfig.getString("AutoAcceptPartyTP.Enabled", "&aAuto Accept Party Teleports")));
          }
          else{
            acceptTpItem.setType(Material.RED_STAINED_GLASS_PANE);
            acceptTpMeta.setDisplayName(Methods.color(guiConfig.getString("AutoAcceptPartyTP.Disabled", "&cDont Auto Accept Party Teleports")));
          }
  
          //Invert setting and update inventory
          mp.setAutoAcceptPartyInvites(!mp.isAutoAcceptPartyInvites());
          List<String> lore = guiConfig.contains("AutoAcceptPartyTP.Lore") ? guiConfig.getStringList("AutoAcceptPartyTP.Lore") :
                                Arrays.asList("&eIf enabled, then any party teleport", "&erequests will be auto accepted");
          acceptTpMeta.setLore(Methods.colorLore(lore));
          acceptTpItem.setItemMeta(acceptTpMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), acceptTpItem);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("BackButton.Slot")){
          HomeGUI main = new HomeGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(main.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, main);
        }
        return;
      }
      
      //Dealing with ability accepting
      if(currentGUI instanceof AcceptAbilityGUI){
        AcceptAbilityGUI acceptAbilityGUI = (AcceptAbilityGUI) currentGUI;
        int slot = e.getSlot();
        if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY){
          if(slot == 16){
            //This is for canceling
            mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            mp.saveData();
            p.closeInventory();
            checkAndOpenPending(mp);
            return;
          }
          if(slot == 10 && mp.getAbilityLoadout().size() < McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities")){
            //If they accept and their loadout isnt full
            AbilityAddToLoadoutEvent event = new AbilityAddToLoadoutEvent(mp, acceptAbilityGUI.getAbility());
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()){
              return;
            }
            mp.addAbilityToLoadout((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            acceptAbilityGUI.getAbility().setToggled(true);
            mp.saveData();
            p.closeInventory();
            checkAndOpenPending(mp);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName())));
            return;
          }
          else if(slot == 10){
            //If their loadout is full but they want this ability
            BaseAbility ability = acceptAbilityGUI.getAbility();
            mp.removePendingAbilityUnlock((UnlockedAbilities) ability.getGenericAbility());
            mp.saveData();
            EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_OVERRIDE, ability);
            currentGUI.setClearData(false);
            p.openInventory(editLoadoutGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
            return;
          }
          else{
            return;
          }
        }
        else if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE){
          if(slot == 16){
            //This is for canceling
            p.closeInventory();
            return;
          }
          if(slot == 10){
            AbilityUpgradeEvent event = new AbilityUpgradeEvent(mp, acceptAbilityGUI.getAbility(), acceptAbilityGUI.getAbility().getCurrentTier(), acceptAbilityGUI.getAbility().getCurrentTier() + 1);
            event.setCancelled(event.getNextTier() > ((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility()).getMaxTier());
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()){
              return;
            }
            mp.setAbilityPoints(mp.getAbilityPoints() - 1);
            acceptAbilityGUI.getAbility().setCurrentTier(acceptAbilityGUI.getAbility().getCurrentTier() + 1);
            
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.UpgradeAbility.Sound")),
              Float.parseFloat(soundFile.getString("Sounds.Misc.UpgradeAbility.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.UpgradeAbility.Pitch")));
            mp.saveData();
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.UpgradedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName())
                                                                                     .replace("%Tier%", "Tier " + Methods.convertToNumeral(acceptAbilityGUI.getAbility().getCurrentTier()))));
            if(mp.getAbilityPoints() > 0){
              GUI gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
              return;
            }
            p.closeInventory();
            return;
          }
          else{
            return;
          }
        }
      }
      
      else if(currentGUI instanceof SubSkillGUI){
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SUBSKILL_GUI);
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")){
          if(GUITracker.doesPlayerHavePrevious(mp)){
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
      }
      
      if(currentGUI instanceof SelectReplaceGUI){
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SELECT_REPLACE_SKILLS_GUI);
        SelectReplaceGUI selectReplaceGUI = (SelectReplaceGUI) currentGUI;
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")){
          //GUIItem item = currentGUI.getGui().getItems().get(currentGUI.getGui().getItems().size() - 2);
          //TODO custom back button
          if(GUITracker.doesPlayerHavePrevious(mp)){
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
        //what does this do????????
		  /*
		if(e.getSlot() == 8){
		  currentGUI.setClearData(false);
		  ReplaceSkillsGUI replaceSkillsGUI = new ReplaceSkillsGUI(mp);
		  p.openInventory(replaceSkillsGUI.getGui().getInv());
		  GUITracker.replacePlayersGUI(mp, replaceSkillsGUI);
		  return;
		}*/
        if(e.getSlot() > selectReplaceGUI.getAbilities().size() - 1){
          return;
        }
        else if(!mp.getBaseAbility(selectReplaceGUI.getAbilities().get(e.getSlot())).isUnlocked()){
          return;
        }
        BaseAbility baseAbility = mp.getBaseAbility(selectReplaceGUI.getAbilities().get(e.getSlot()));
        if(mp.getAbilityLoadout().size() < McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities")){
          if(mp.getAbilityLoadout().contains(baseAbility.getGenericAbility())){
            return;
          }
          AbilityAddToLoadoutEvent event = new AbilityAddToLoadoutEvent(mp, baseAbility);
          Bukkit.getPluginManager().callEvent(event);
          if(event.isCancelled()){
            return;
          }
          boolean hasActive = false;
          for(int i = 0; i < mp.getAbilityLoadout().size(); i++){
            UnlockedAbilities ab = mp.getAbilityLoadout().get(i);
            if((ab.getAbilityType() == AbilityType.ACTIVE && baseAbility.getGenericAbility().getAbilityType() == AbilityType.ACTIVE) && ab.getSkill().equals(baseAbility.getGenericAbility().getSkill())){
              mp.getAbilityLoadout().set(i, (UnlockedAbilities) baseAbility.getGenericAbility());
              hasActive = true;
              break;
            }
          }
          if(!hasActive){
            mp.addAbilityToLoadout(selectReplaceGUI.getAbilities().get(e.getSlot()));
          }
          mp.saveData();
          GUI lastGUI = GUITracker.getPlayersPreviousGUI(p);
          GUITracker.stopTrackingPlayer(p);
          selectReplaceGUI.getGui().rebuildGUI();
          p.openInventory(selectReplaceGUI.getGui().getInv());
          GUITracker.trackPlayer(p, selectReplaceGUI);
          GUITracker.setPlayersPreviousGUI(p, lastGUI);
          
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", baseAbility.getGenericAbility().getName())));
          return;
        }
        else{
          EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_REPLACE, baseAbility);
          currentGUI.setClearData(false);
          p.openInventory(editLoadoutGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
        }
        return;
      }
      
      //Remote Transfer GUI
      if(currentGUI instanceof RemoteTransferGUI){
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.REMOTE_TRANSFER_GUI);
        if(e.getCurrentItem().getType() == Material.AIR){
          return;
        }
        else{
          RemoteTransfer ab = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
          if(e.getSlot() == e.getInventory().getSize() - 1){
            ab.setToggled(!ab.isToggled());
            if(!ab.isToggled()){
              ItemStack current = e.getCurrentItem();
              current.removeEnchantment(Enchantment.DURABILITY);
              ItemMeta meta = current.getItemMeta();
              List<String> lore = meta.getLore();
              lore.remove(meta.getLore().size() - 1);
              lore.add(Methods.color(p, guiConfig.getString("ToggledOff")));
              meta.setLore(lore);
              current.setItemMeta(meta);
              e.getInventory().setItem(e.getSlot(), current);
              ((Player) e.getWhoClicked()).updateInventory();
            }
            else{
              ItemStack current = e.getCurrentItem();
              ItemMeta meta = current.getItemMeta();
              List<String> lore = meta.getLore();
              lore.remove(meta.getLore().size() - 1);
              lore.add(Methods.color(p, guiConfig.getString("ToggledOn")));
              meta.setLore(lore);
              current.setItemMeta(meta);
              e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
              e.getInventory().setItem(e.getSlot(), current);
              ((Player) e.getWhoClicked()).updateInventory();
            }
            return;
          }
          else{
            if(e.getCurrentItem().containsEnchantment(Enchantment.DURABILITY)){
              ab.getItemsToSync().put(e.getCurrentItem().getType(), false);
              e.getCurrentItem().removeEnchantment(Enchantment.DURABILITY);
              return;
            }
            else{
              e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
              ab.getItemsToSync().put(e.getCurrentItem().getType(), true);
              return;
            }
          }
        }
      }
      
      else if(currentGUI instanceof EditDefaultAbilitiesGUI){
        EditDefaultAbilitiesGUI editDefaultAbilitiesGUI = (EditDefaultAbilitiesGUI) currentGUI;
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_DEFAULT_ABILITIES_GUI);
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")){
          if(GUITracker.doesPlayerHavePrevious(mp)){
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
        BaseAbility abilityToChange = editDefaultAbilitiesGUI.getDefaultAbilityList().get(e.getSlot());
        abilityToChange.setToggled(!abilityToChange.isToggled());
        if(!abilityToChange.isToggled()){
          ItemStack current = e.getCurrentItem();
          current.removeEnchantment(Enchantment.DURABILITY);
          ItemMeta meta = current.getItemMeta();
          List<String> lore = meta.getLore();
          lore.remove(meta.getLore().size() - 1);
          lore.add(Methods.color(guiConfig.getString("AbilityItems.ToggledOff")));
          meta.setLore(lore);
          current.setItemMeta(meta);
          ((Player) e.getWhoClicked()).updateInventory();
        }
        else{
          ItemStack current = e.getCurrentItem();
          ItemMeta meta = current.getItemMeta();
          List<String> lore = meta.getLore();
          lore.remove(meta.getLore().size() - 1);
          lore.add(Methods.color(guiConfig.getString("AbilityItems.ToggledOn")));
          meta.setLore(lore);
          current.setItemMeta(meta);
          e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
          ((Player) e.getWhoClicked()).updateInventory();
        }
        return;
      }
      
      //Deal with the various editloadout guis
      else if(currentGUI instanceof EditLoadoutGUI){
        EditLoadoutGUI editLoadoutGUI = (EditLoadoutGUI) currentGUI;
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_GUI);
        if(e.getSlot() > mp.getAbilityLoadout().size() - 1){
          return;
        }
        BaseAbility abilityToChange = mp.getBaseAbility(editLoadoutGUI.getAbilityFromSlot(e.getSlot()));
        if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.TOGGLE){
          if(abilityToChange.getGenericAbility() == UnlockedAbilities.REMOTE_TRANSFER){
            RemoteTransferGUI remoteTransferGUI = new RemoteTransferGUI(mp, mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER));
            currentGUI.setClearData(false);
            p.openInventory(remoteTransferGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, remoteTransferGUI);
            return;
          }
          abilityToChange.setToggled(!abilityToChange.isToggled());
          if(!abilityToChange.isToggled()){
            ItemStack current = e.getCurrentItem();
            current.removeEnchantment(Enchantment.DURABILITY);
            ItemMeta meta = current.getItemMeta();
            List<String> lore = meta.getLore();
            lore.remove(meta.getLore().size() - 1);
            lore.add(Methods.color(guiConfig.getStringList("AbilityItem.ToggledOffLore").get(0)));
            meta.setLore(lore);
            current.setItemMeta(meta);
            ((Player) e.getWhoClicked()).updateInventory();
          }
          else{
            ItemStack current = e.getCurrentItem();
            ItemMeta meta = current.getItemMeta();
            List<String> lore = meta.getLore();
            lore.remove(meta.getLore().size() - 1);
            lore.add(Methods.color(guiConfig.getStringList("AbilityItem.ToggledOnLore").get(0)));
            meta.setLore(lore);
            current.setItemMeta(meta);
            e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ((Player) e.getWhoClicked()).updateInventory();
          }
        }
        else if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_UPGRADE){
          UnlockedAbilities unlockedAbility = (UnlockedAbilities) abilityToChange.getGenericAbility();
          if(abilityToChange.getCurrentTier() < unlockedAbility.getMaxTier()){
            if(unlockedAbility.tierUnlockLevel(abilityToChange.getCurrentTier() + 1) > mp.getSkill(unlockedAbility.getSkill()).getCurrentLevel()){
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Pitch")));
              return;
            }
            AcceptAbilityGUI gui = new AcceptAbilityGUI(mp, abilityToChange, AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else{
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Sound")),
              Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Pitch")));
            return;
          }
        }
        else{
          if(editLoadoutGUI.getReplaceAbility().getGenericAbility().getAbilityType() == AbilityType.ACTIVE){
            for(int i = 0; i < mp.getAbilityLoadout().size(); i++){
              UnlockedAbilities unlockedAbilities = mp.getAbilityLoadout().get(i);
              if(e.getSlot() != i && unlockedAbilities.getAbilityType() == AbilityType.ACTIVE && unlockedAbilities.getSkill().equals(editLoadoutGUI.getReplaceAbility().getGenericAbility().getSkill())){
                p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.DenyReplace.Sound")),
                  Float.parseFloat(soundFile.getString("Sounds.Misc.DenyReplace.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.DenyReplace.Pitch")));
                p.closeInventory();
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.HasActive")));
                return;
              }
            }
          }
          editLoadoutGUI.getAbilities().set(e.getSlot(), (UnlockedAbilities) editLoadoutGUI.getReplaceAbility().getGenericAbility());
          mp.getAbilityLoadout().set(e.getSlot(), (UnlockedAbilities) editLoadoutGUI.getReplaceAbility().getGenericAbility());
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", editLoadoutGUI.getReplaceAbility().getGenericAbility().getName())));
          if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_REPLACE){
            int cooldown = McRPG.getInstance().getConfig().getInt("Configuration.ReplaceAbilityCooldown");
            if(cooldown != 0){
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.MINUTE, cooldown);
              mp.setEndTimeForReplaceCooldown(cal.getTimeInMillis());
            }
          }
          p.closeInventory();
          if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_OVERRIDE){
            checkAndOpenPending(mp);
          }
        }
        mp.saveData();
        return;
      }
      
      else if(currentGUI instanceof PartyRoleGUI){
        PartyRoleGUI partyRoleGUI = (PartyRoleGUI) currentGUI;
        if(partyRoleGUI.getPartyPermissionsMap().containsKey(e.getSlot())){
          FileConfiguration partyRoleFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_ROLE_GUI);
          PartyPermissions partyPermission = partyRoleGUI.getPartyPermissionsMap().get(e.getSlot());
          Party party = partyRoleGUI.getParty();
          PartyRoles currentRole = party.getRoleForPermission(partyPermission);
          PartyRoles nextRole;
          if(currentRole == PartyRoles.OWNER){
            nextRole = PartyRoles.MOD;
          }
          else if(currentRole == PartyRoles.MOD){
            nextRole = PartyRoles.MEMBER;
          }
          else{
            nextRole = PartyRoles.OWNER;
          }
          String key = partyPermission.getName().replace(" ", "") + "." + nextRole.getName() + ".";
          ItemStack itemStack = e.getCurrentItem();
          itemStack.setType(Material.getMaterial(partyRoleFile.getString(key + "Material")));
          ItemMeta itemMeta = itemStack.getItemMeta();
          itemMeta.setDisplayName(Methods.color(partyRoleFile.getString(key + "DisplayName")));
          itemMeta.setLore(Methods.colorLore(partyRoleFile.getStringList(key + "Lore")));
          itemStack.setItemMeta(itemMeta);
          e.setCurrentItem(itemStack);
          party.setRoleForPermission(partyPermission, nextRole);
          for(HumanEntity viewer : e.getInventory().getViewers()){
            ((Player) viewer).updateInventory();
          }
        }
        return;
      }
      
      else if(currentGUI instanceof PartyUpgradesGUI){
        PartyUpgradesGUI partyUpgradesGUI = (PartyUpgradesGUI) currentGUI;
        if(partyUpgradesGUI.getPartyUpgradesMap().containsKey(e.getSlot())){
          Party party = partyUpgradesGUI.getParty();
          PartyMember partyMember = party.getPartyMember(p.getUniqueId());
          if(party.getPartyUpgradePoints() > 0){
            PartyUpgrades partyUpgrades = partyUpgradesGUI.getPartyUpgradesMap().get(e.getSlot());
            int maxTier = PartyUpgrades.getMaxTier(partyUpgrades);
            int currentTier = party.getUpgradeTier(partyUpgrades);
            if(currentTier < maxTier){
              if(partyMember.getPartyRole().getId() <= party.getRoleForPermission(PartyPermissions.UPGRADE_PARTY).getId()){
                party.setPartyUpgradePoints(party.getPartyUpgradePoints() - 1);
                party.setUpgradeTier(partyUpgrades, currentTier + 1);
                p.closeInventory();
                new BukkitRunnable(){
                  @Override
                  public void run(){
                    try{
                      PartyUpgradesGUI newGUI = new PartyUpgradesGUI(mp);
                      p.openInventory(newGUI.getGui().getInv());
                      GUITracker.trackPlayer(p, newGUI);
                    }catch(PartyNotFoundException ex){
                    }
                  }
                }.runTaskLater(McRPG.getInstance(), 1);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
                for(Player player : party.getOnlinePlayers()){
                  player.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Commands.Parties.PartyUpgraded")
                                                                                             .replace("%Player%", p.getName()).replace("%Upgrade%", partyUpgrades.getName())
                                                                                             .replace("%Level%", Integer.toString(currentTier + 1))));
                }
              }
              else{
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Commands.Parties.PartyUpgraded")));
              }
            }
          }
        }
        else if(e.getSlot() == McRPG.getInstance().getFileManager().getFile(FileManager.Files. PARTY_UPGRADES_GUI).getInt("BackButton.Slot", 55)){
          if(GUITracker.doesPlayerHavePrevious(p)){
            GUI previousGUI = GUITracker.getPlayersPreviousGUI(p);
            previousGUI.setClearData(true);
            currentGUI.setClearData(false);
            p.openInventory(previousGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(p, previousGUI);
          }
          else{
            GUI previousGUI = new PartyMainGUI(mp);
            previousGUI.setClearData(true);
            currentGUI.setClearData(false);
            p.openInventory(previousGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(p, previousGUI);          }
        }
        return;
      }
      
      GUIEventBinder binder = null;
      if(currentGUI.getGui().getBoundEvents() != null){
        binder = currentGUI.getGui().getBoundEvents().stream().filter(guiBinder -> guiBinder.getSlot() == e.getSlot()).findFirst().orElse(null);
      }
      if(binder == null) return;
      for(String eventBinder : binder.getBoundEventList()){
        String[] events = eventBinder.split(":");
        String event = events[0];
        if(event.equalsIgnoreCase("Permission")){
          String perm = events[1];
          if(!p.hasPermission(perm)){
            GUITracker.stopTrackingPlayer(p);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
            return;
          }
          else{
            continue;
          }
        }
        else if(event.equalsIgnoreCase("Command")){
          String sender = events[1];
          if(sender.equalsIgnoreCase("console")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), events[2].replace("%Player%", p.getName()));
            continue;
          }
          else if(sender.equalsIgnoreCase("player")){
            p.performCommand(events[2]);
            continue;
          }
        }
        else if(event.equalsIgnoreCase("close")){
          GUITracker.stopTrackingPlayer(p);
          p.closeInventory();
          continue;
        }
        else if(event.equalsIgnoreCase("back")){
          if(GUITracker.doesPlayerHavePrevious(p)){
            GUI previousGUI = GUITracker.getPlayersPreviousGUI(p);
            previousGUI.setClearData(true);
            currentGUI.setClearData(false);
            p.openInventory(previousGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(p, previousGUI);
            continue;
          }
          else{
            GUITracker.stopTrackingPlayer(p);
            continue;
          }
        }
        else if(event.equalsIgnoreCase("Redeem")){
          Skills skill = null;
          RedeemType redeemType = null;
          if(currentGUI instanceof AmountGUI){
            AmountGUI amountGUI = (AmountGUI) currentGUI;
            skill = amountGUI.getSkill();
            redeemType = amountGUI.getType();
          }
          else if(currentGUI instanceof AllGUI){
            AllGUI allGUI = (AllGUI) currentGUI;
            skill = allGUI.getRedeemBit().getSkill();
            redeemType = allGUI.getRedeemBit().getRedeemType();
          }
          if(Methods.isInt(events[1])){
            int amount = Integer.parseInt(events[1]);
            if(redeemType == RedeemType.EXP){
              if(mp.getRedeemableExp() - amount < 0){
                e.setCancelled(true);
                return;
              }
              mp.giveExp(skill, amount, GainReason.REDEEM);
              mp.setRedeemableExp(mp.getRedeemableExp() - amount);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedExp")
                                                                                       .replace("%Skill%", skill.getName()).replace("%Amount%", Integer.toString(amount))));
              p.closeInventory();
              return;
            }
            else{
              if(mp.getRedeemableLevels() - amount < 0){
                e.setCancelled(true);
                return;
              }
              if(amount + mp.getSkill(skill).getCurrentLevel() > mp.getSkill(skill).getType().getMaxLevel()){
                amount = mp.getSkill(skill).getType().getMaxLevel() - mp.getSkill(skill).getCurrentLevel();
              }
              mp.getSkill(skill).giveLevels(mp, amount, McRPG.getInstance().getConfig().getBoolean("Configuration.Redeeming.RedeemLevelsResetExp"));
              mp.setRedeemableLevels(mp.getRedeemableLevels() - amount);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedLevels")
                                                                                       .replace("%Skill%", skill.getName()).replace("%Amount%", Integer.toString(amount))));
              p.closeInventory();
              return;
            }
          }
          else if(events[1].equalsIgnoreCase("custom")){
            mp.setListenForCustomExpInput(true);
            mp.setRedeemBit(new RedeemBit(redeemType, skill));
            p.closeInventory();
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.Listening")));
            return;
          }
          else if(events[1].equalsIgnoreCase("all")){
            if(redeemType == RedeemType.EXP){
              mp.giveExp(skill, mp.getRedeemableExp(), GainReason.REDEEM);
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedExp")
                                                                                       .replace("%Skill%", skill.getName()).replace("%Amount%", Integer.toString(mp.getRedeemableExp()))));
              mp.setRedeemableExp(0);
              p.closeInventory();
              return;
            }
            else{
              int amount = 0;
              if(mp.getRedeemableLevels() + mp.getSkill(skill).getCurrentLevel() > mp.getSkill(skill).getType().getMaxLevel()){
                amount = mp.getSkill(skill).getType().getMaxLevel() - mp.getSkill(skill).getCurrentLevel();
                mp.setRedeemableLevels(mp.getRedeemableLevels() - amount);
              }
              else{
                amount = mp.getRedeemableLevels();
                mp.setRedeemableLevels(0);
              }
              mp.getSkill(skill).giveLevels(mp, amount, McRPG.getInstance().getConfig().getBoolean("Configuration.Redeeming.RedeemLevelsResetExp"));
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.CustomRedeem.RedeemedLevels")
                                                                                       .replace("%Skill%", skill.getName()).replace("%Amount%", Integer.toString(amount))));
              p.closeInventory();
              return;
            }
          }
        }
        else if(event.equalsIgnoreCase("Open")){
          if(currentGUI instanceof AmountGUI){
            if(events[1].equalsIgnoreCase("AllGUI")){
              AmountGUI amountGUI = (AmountGUI) currentGUI;
              AllGUI allGUI = new AllGUI(mp, new RedeemBit(amountGUI.getType(), amountGUI.getSkill()));
              currentGUI.setClearData(false);
              p.openInventory(allGUI.getGui().getInv());
              GUITracker.replacePlayersGUI(p, allGUI);
              return;
            }
          }
          if(currentGUI instanceof RedeemStoredGUI){
            RedeemStoredGUI redeemStoredGUI = (RedeemStoredGUI) currentGUI;
            if(events[1].equalsIgnoreCase("ExpAmountGUI")){
              AmountGUI gui = new AmountGUI(mp, RedeemType.EXP, redeemStoredGUI.getSkill());
              redeemStoredGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(p, gui);
              return;
            }
            else if(events[1].equalsIgnoreCase("LevelAmountGUI")){
              AmountGUI gui = new AmountGUI(mp, RedeemType.LEVEL, redeemStoredGUI.getSkill());
              redeemStoredGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(p, gui);
              return;
            }
          }
          GUITracker.stopTrackingPlayer(p);
          p.closeInventory();
          p.sendMessage(Methods.color("&cThis has yet to be implemented"));
          return;
        }
        else if(event.equalsIgnoreCase("OpenNative")){
          GUI gui;
          if(events[1].equalsIgnoreCase("EditLoadoutGUI")){
            gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.TOGGLE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("EditLoadoutSelectGUI")){
            gui = new EditLoadoutSelectGUI(mp);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SettingsGUI")){
            gui = new SettingsGUI(mp);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("UpgradeAbilityGUI")){
            if(mp.getAbilityPoints() == 0){
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Sound")),
                Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Volume")), Float.parseFloat(soundFile.getString("Sounds.Misc.CantUpgradeAbility.Pitch")));
              return;
            }
            gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SubSkillGUI")){
            Skills skill = Skills.fromString(events[2]);
            gui = new SubSkillGUI(mp, skill);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SelectReplaceGUI")){
            Skills skill = Skills.fromString(events[2]);
            gui = new SelectReplaceGUI(mp, skill);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("PartyMemberGUI")){
            try{
              gui = new PartyMemberGUI(mp);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
            }catch(PartyNotFoundException ex){
              ex.printStackTrace();
            }
          }
          else if(events[1].equalsIgnoreCase("PartyBankGUI")){
            try{
              gui = new PartyBankGUI(mp);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
            }catch(PartyNotFoundException ex){
              ex.printStackTrace();
            }
          }
          else if(events[1].equalsIgnoreCase("PartyUpgradesGUI")){
            try{
              gui = new PartyUpgradesGUI(mp);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
            }catch(PartyNotFoundException ex){
              ex.printStackTrace();
            }
          }
          else if(events[1].equalsIgnoreCase("PrivateBankGUI")){
            PartyManager partyManager = McRPG.getInstance().getPartyManager();
            if(mp.getPartyID() == null || partyManager.getParty(mp.getPartyID()) == null){
              p.closeInventory();
              return;
            }
            Party party = partyManager.getParty(mp.getPartyID());
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(party.getRoleForPermission(PartyPermissions.PRIVATE_BANK).getId() < partyMember.getPartyRole().getId()){
              p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1);
              return;
            }
            try{
              gui = new PartyPrivateBankGUI(mp);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
            }catch(PartyNotFoundException ex){
              ex.printStackTrace();
            }
          }
          else if(events[1].equalsIgnoreCase("RolesGUI")){
            PartyManager partyManager = McRPG.getInstance().getPartyManager();
            if(mp.getPartyID() == null || partyManager.getParty(mp.getPartyID()) == null){
              p.closeInventory();
              return;
            }
            Party party = partyManager.getParty(mp.getPartyID());
            PartyMember partyMember = party.getPartyMember(p.getUniqueId());
            if(partyMember.getPartyRole().getId() != 0){
              p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1);
              return;
            }
            gui = new PartyRoleGUI(mp, party);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
          }
        }
        else if(event.equalsIgnoreCase("OpenFile")){
          GUI gui = null;
          if(events[1].equalsIgnoreCase("skillsgui.yml")){
            gui = new SkillGUI(mp);
          }
          else if(events[1].equalsIgnoreCase("maingui.yml")){
            gui = new HomeGUI(mp);
          }
          else{
            p.sendMessage("Not added yet");
            p.closeInventory();
            return;
          }
          currentGUI.setClearData(false);
          p.openInventory(gui.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, gui);
          return;
        }
      }
    }
  }
  
  private void checkAndOpenPending(McRPGPlayer mp){
    Player p = mp.getPlayer();
    if(mp.hasPendingAbility()){
      UnlockedAbilities ability = mp.getPendingUnlockAbilities().get(0);
      if(ability.getAbilityType() == AbilityType.ACTIVE){
        BaseAbility baseAbility = mp.getBaseAbility(ability);
        if(mp.doesPlayerHaveActiveAbilityFromSkill(ability.getSkill())){
          BaseAbility oldAbility = mp.getBaseAbility(mp.getActiveAbilityForSkill(ability.getSkill()));
          AbilityOverrideGUI overrideGUI = new AbilityOverrideGUI(mp, oldAbility, baseAbility);
          p.openInventory(overrideGUI.getGui().getInv());
          GUITracker.trackPlayer(p, overrideGUI);
        }
        else{
          GUI gui = new AcceptAbilityGUI(mp, baseAbility, AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY);
          p.openInventory(gui.getGui().getInv());
          GUITracker.trackPlayer(p, gui);
        }
      }
      else{
        BaseAbility baseAbility = mp.getBaseAbility(ability);
        GUI gui = new AcceptAbilityGUI(mp, baseAbility, AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY);
        p.openInventory(gui.getGui().getInv());
        GUITracker.trackPlayer(p, gui);
      }
    }
  }
}
