package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityAddToLoadoutEvent;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityRemovedFromLoadoutEvent;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityUpgradeEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.gui.*;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.AbilityType;
import us.eunoians.mcrpg.types.DisplayType;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("SuspiciousMethodCalls")
public class InvClickEvent implements Listener {

  private FileConfiguration config;

  public InvClickEvent(McRPG plugin) {
    config = McRPG.getInstance().getLangFile();

  }

  @SuppressWarnings("Duplicates")
  @EventHandler(priority = EventPriority.HIGHEST)
  public void invClickEvent(InventoryClickEvent e) {
    Player p = (Player) e.getWhoClicked();
    //If this is a gui
    if(GUITracker.isPlayerTracked(p)) {
      //Cancel event
      e.setCancelled(true);
      //Ignore player inventory
      if(e.getClickedInventory() instanceof PlayerInventory) {
        return;
      }
      McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
      //Cuz null errors are fun
      if(e.getCurrentItem() == null) return;
      GUI currentGUI = GUITracker.getPlayersGUI(p);

      //Overriding abilities gui, used for active abilities
      if(currentGUI instanceof AbilityOverrideGUI) {
        AbilityOverrideGUI overrideGUI = (AbilityOverrideGUI) currentGUI;
        int slot = e.getSlot();
        if(slot == 16) {
          mp.removePendingAbilityUnlock((UnlockedAbilities) overrideGUI.getReplaceAbility().getGenericAbility());
          mp.saveData();
          p.closeInventory();
          return;
        }
        else {
          if(slot == 10) {
            AbilityAddToLoadoutEvent abilityAddToLoadoutEvent = new AbilityAddToLoadoutEvent(mp, overrideGUI.getReplaceAbility());
            Bukkit.getPluginManager().callEvent(abilityAddToLoadoutEvent);
            if(abilityAddToLoadoutEvent.isCancelled()) {
              return;
            }
            AbilityRemovedFromLoadoutEvent abilityRemovedFromLoadoutEvent = new AbilityRemovedFromLoadoutEvent(mp, overrideGUI.getAbiltyToReplace());
            Bukkit.getPluginManager().callEvent(abilityRemovedFromLoadoutEvent);
            if(abilityRemovedFromLoadoutEvent.isCancelled()) {
              return;
            }
            if(mp.getCooldown(Skills.fromString(overrideGUI.getAbiltyToReplace().getGenericAbility().getSkill())) != -1) {
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
      if(currentGUI instanceof EditLoadoutSelectGUI) {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_SELECT_GUI);
        int slot = e.getSlot();
        if(slot == guiConfig.getInt("DefaultAbilitiesItem.Slot")) {
          EditDefaultAbilitiesGUI editDefaultAbilitiesGUI = new EditDefaultAbilitiesGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(editDefaultAbilitiesGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editDefaultAbilitiesGUI);
        }
        else if(slot == guiConfig.getInt("ReplaceAbilitiesItem.Slot")) {
          if(mp.getEndTimeForReplaceCooldown() != 0) {
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
            return;
          }
          ReplaceSkillsGUI replaceSkillsGUI = new ReplaceSkillsGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(replaceSkillsGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, replaceSkillsGUI);
        }
        else if(slot == guiConfig.getInt("UnlockedAbilitiesItem.Slot")) {
          EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.TOGGLE);
          currentGUI.setClearData(false);
          p.openInventory(editLoadoutGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
        }
        else if(slot == guiConfig.getInt("BackButton.Slot")){
          if(GUITracker.doesPlayerHavePrevious(mp)) {
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
      }

      if(currentGUI instanceof SettingsGUI) {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SETTINGS_GUI);
        if(e.getSlot() == guiConfig.getInt("ChangeDisplaySettings.Slot")) {
          ItemStack displayItem = new ItemStack(Material.BLAZE_ROD);
          ItemMeta displayMeta = displayItem.getItemMeta();
          if(mp.getDisplayType() == DisplayType.ACTION_BAR) {
            displayItem.setType(Material.SIGN);
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.ScoreBoard")));
            mp.setDisplayType(DisplayType.SCOREBOARD);
          }
          else if(mp.getDisplayType() == DisplayType.SCOREBOARD) {
            displayItem.setType(Material.DRAGON_HEAD);
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.BossBar")));
            mp.setDisplayType(DisplayType.BOSS_BAR);
          }
          else if(mp.getDisplayType() == DisplayType.BOSS_BAR) {
            displayMeta.setDisplayName(Methods.color(guiConfig.getString("ChangeDisplaySettings.ActionBar")));
            mp.setDisplayType(DisplayType.ACTION_BAR);
          }
          displayMeta.setLore(Methods.colorLore(guiConfig.getStringList("ChangeDisplaySettings.Lore")));
          displayItem.setItemMeta(displayMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), displayItem);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("KeepHandEmpty.Slot")) {
          ItemStack itemPickup = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta itemPickupMeta = itemPickup.getItemMeta();
          if(!mp.isKeepHandEmpty()) {
            itemPickupMeta.setDisplayName(Methods.color(guiConfig.getString("KeepHandEmpty.Enabled")));
          }
          else {
            itemPickup.setType(Material.RED_STAINED_GLASS_PANE);
            itemPickupMeta.setDisplayName(Methods.color(guiConfig.getString("KeepHandEmpty.Disabled")));
          }
          mp.setKeepHandEmpty(!mp.isKeepHandEmpty());
          itemPickupMeta.setLore(Methods.colorLore(guiConfig.getStringList("KeepHandEmpty.Lore")));
          itemPickup.setItemMeta(itemPickupMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), itemPickup);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("IgnoreTips.Slot")) {
          ItemStack tipItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta tipItemMeta = tipItem.getItemMeta();
          if(!mp.isIgnoreTips()) {
            tipItemMeta.setDisplayName(Methods.color(guiConfig.getString("IgnoreTips.Enabled")));
          }
          else {
            tipItem.setType(Material.RED_STAINED_GLASS_PANE);
            tipItemMeta.setDisplayName(Methods.color(guiConfig.getString("IgnoreTips.Disabled")));
          }
          mp.setIgnoreTips(!mp.isIgnoreTips());
          tipItemMeta.setLore(Methods.colorLore(guiConfig.getStringList("IgnoreTips.Lore")));
          tipItem.setItemMeta(tipItemMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), tipItem);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("MobHealthDisplay.Slot")) {
          MobHealthbarUtils.MobHealthbarType healthbarType = mp.getHealthbarType();
          ItemStack healthItem = new ItemStack(Material.BUBBLE_CORAL_BLOCK);
          ItemMeta healthMeta = healthItem.getItemMeta();
          if(healthbarType == MobHealthbarUtils.MobHealthbarType.DISABLED) {
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.Bar")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.BAR);
          }
          else if(healthbarType == MobHealthbarUtils.MobHealthbarType.HEARTS) {
            healthItem.setType(Material.DEAD_FIRE_CORAL_BLOCK);
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.None")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.DISABLED);
          }
          else if(healthbarType == MobHealthbarUtils.MobHealthbarType.BAR) {
            healthItem.setType(Material.FIRE_CORAL_BLOCK);
            healthMeta.setDisplayName(Methods.color(guiConfig.getString("MobHealthDisplay.Hearts")));
            mp.setHealthbarType(MobHealthbarUtils.MobHealthbarType.HEARTS);
          }
          healthMeta.setLore(Methods.colorLore(guiConfig.getStringList("MobHealthDisplay.Lore")));
          healthItem.setItemMeta(healthMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), healthItem);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("AutoDenyNewAbilities.Slot")) {
          ItemStack autoDenyItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
          ItemMeta autoDenyItemMeta = autoDenyItem.getItemMeta();
          if(!mp.isAutoDeny()) {
            autoDenyItemMeta.setDisplayName(Methods.color(guiConfig.getString("AutoDenyNewAbilities.Enabled")));
          }
          else {
            autoDenyItem.setType(Material.RED_STAINED_GLASS_PANE);
            autoDenyItemMeta.setDisplayName(Methods.color(guiConfig.getString("AutoDenyNewAbilities.Disabled")));
          }
          mp.setAutoDeny(!mp.isAutoDeny());
          autoDenyItemMeta.setLore(Methods.colorLore(guiConfig.getStringList("AutoDenyNewAbilities.Lore")));
          autoDenyItem.setItemMeta(autoDenyItemMeta);
          currentGUI.getGui().getInv().setItem(e.getSlot(), autoDenyItem);
          p.updateInventory();
        }
        else if(e.getSlot() == guiConfig.getInt("BackButton.Slot")) {
          HomeGUI main = new HomeGUI(mp);
          currentGUI.setClearData(false);
          p.openInventory(main.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, main);
        }
        return;
      }

      //Dealing with ability accepting
      if(currentGUI instanceof AcceptAbilityGUI) {
        AcceptAbilityGUI acceptAbilityGUI = (AcceptAbilityGUI) currentGUI;
        int slot = e.getSlot();
        if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_ABILITY) {
          if(slot == 16) {
            //This is for canceling
            mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            mp.saveData();
            p.closeInventory();
            return;
          }
          if(slot == 10 && mp.getAbilityLoadout().size() < 9) {
            //If they accept and their loadout isnt full
            AbilityAddToLoadoutEvent event = new AbilityAddToLoadoutEvent(mp, acceptAbilityGUI.getAbility());
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) {
              return;
            }
            mp.addAbilityToLoadout((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            mp.removePendingAbilityUnlock((UnlockedAbilities) acceptAbilityGUI.getAbility().getGenericAbility());
            acceptAbilityGUI.getAbility().setToggled(true);
            mp.saveData();
            p.closeInventory();
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName())));
            return;
          }
          else if(slot == 10) {
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
          else {
            return;
          }
        }
        else if(acceptAbilityGUI.getAcceptType() == AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE) {
          if(slot == 16) {
            //This is for canceling
            p.closeInventory();
            return;
          }
          if(slot == 10) {
            AbilityUpgradeEvent event = new AbilityUpgradeEvent(mp, acceptAbilityGUI.getAbility(), acceptAbilityGUI.getAbility().getCurrentTier(), acceptAbilityGUI.getAbility().getCurrentTier() + 1);
            event.setCancelled(event.getNextTier() > 5);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled()) {
              return;
            }
            mp.setAbilityPoints(mp.getAbilityPoints() - 1);
            acceptAbilityGUI.getAbility().setCurrentTier(acceptAbilityGUI.getAbility().getCurrentTier() + 1);
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 5, 1);
            mp.saveData();
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.UpgradedAbility").replace("%Ability%", acceptAbilityGUI.getAbility().getGenericAbility().getName())
                    .replace("%Tier%", "Tier " + Methods.convertToNumeral(acceptAbilityGUI.getAbility().getCurrentTier()))));
            if(mp.getAbilityPoints() > 0) {
              GUI gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
              currentGUI.setClearData(false);
              p.openInventory(gui.getGui().getInv());
              GUITracker.replacePlayersGUI(mp, gui);
              return;
            }
            p.closeInventory();
            return;
          }
          else {
            return;
          }
        }
      }

      else if(currentGUI instanceof SubSkillGUI) {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SUBSKILL_GUI);
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")) {
          if(GUITracker.doesPlayerHavePrevious(mp)) {
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
      }

      if(currentGUI instanceof SelectReplaceGUI) {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SELECT_REPLACE_SKILLS_GUI);
        SelectReplaceGUI selectReplaceGUI = (SelectReplaceGUI) currentGUI;
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")) {
          if(GUITracker.doesPlayerHavePrevious(mp)) {
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
        if(e.getSlot() > selectReplaceGUI.getAbilities().size() - 1) {
          return;
        }
        else if(!mp.getBaseAbility(selectReplaceGUI.getAbilities().get(e.getSlot())).isUnlocked()) {
          return;
        }
        BaseAbility baseAbility = mp.getBaseAbility(selectReplaceGUI.getAbilities().get(e.getSlot()));
        if(mp.getAbilityLoadout().size() < 9) {
          if(mp.getAbilityLoadout().contains(baseAbility.getGenericAbility())) {
            return;
          }
          AbilityAddToLoadoutEvent event = new AbilityAddToLoadoutEvent(mp, baseAbility);
          Bukkit.getPluginManager().callEvent(event);
          if(event.isCancelled()) {
            return;
          }
          boolean hasActive = false;
          for(int i = 0; i < mp.getAbilityLoadout().size(); i++) {
            UnlockedAbilities ab = mp.getAbilityLoadout().get(i);
            if((ab.getAbilityType() == AbilityType.ACTIVE && baseAbility.getGenericAbility().getAbilityType() == AbilityType.ACTIVE) && ab.getSkill().equalsIgnoreCase(baseAbility.getGenericAbility().getSkill())) {
              mp.getAbilityLoadout().set(i, (UnlockedAbilities) baseAbility.getGenericAbility());
              hasActive = true;
              break;
            }
          }
          if(!hasActive) {
            mp.addAbilityToLoadout(selectReplaceGUI.getAbilities().get(e.getSlot()));
          }
          mp.saveData();
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", baseAbility.getGenericAbility().getName())));
          return;
        }
        else {
          EditLoadoutGUI editLoadoutGUI = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_REPLACE, baseAbility);
          currentGUI.setClearData(false);
          p.openInventory(editLoadoutGUI.getGui().getInv());
          GUITracker.replacePlayersGUI(mp, editLoadoutGUI);
        }
        return;
      }

      //Remote Transfer GUI
      if(currentGUI instanceof RemoteTransferGUI) {
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.REMOTE_TRANSFER_GUI);
        if(e.getCurrentItem().getType() == Material.AIR) {
          return;
        }
        else {
          RemoteTransfer ab = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
          if(e.getSlot() == e.getInventory().getSize() - 1) {
            ab.setToggled(!ab.isToggled());
            if(!ab.isToggled()) {
              ItemStack current = e.getCurrentItem();
              current.removeEnchantment(Enchantment.DURABILITY);
              ItemMeta meta = current.getItemMeta();
              List<String> lore = meta.getLore();
              lore.remove(meta.getLore().size() - 1);
              lore.add(Methods.color(guiConfig.getString("ToggledOff")));
              meta.setLore(lore);
              current.setItemMeta(meta);
              e.getInventory().setItem(e.getSlot(), current);
              ((Player) e.getWhoClicked()).updateInventory();
            }
            else {
              ItemStack current = e.getCurrentItem();
              ItemMeta meta = current.getItemMeta();
              List<String> lore = meta.getLore();
              lore.remove(meta.getLore().size() - 1);
              lore.add(Methods.color(guiConfig.getString("ToggledOn")));
              meta.setLore(lore);
              current.setItemMeta(meta);
              e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
              e.getInventory().setItem(e.getSlot(), current);
              ((Player) e.getWhoClicked()).updateInventory();
            }
            return;
          }
          else {
            if(e.getCurrentItem().containsEnchantment(Enchantment.DURABILITY)) {
              ab.getItemsToSync().put(e.getCurrentItem().getType(), false);
              e.getCurrentItem().removeEnchantment(Enchantment.DURABILITY);
              return;
            }
            else {
              e.getCurrentItem().addUnsafeEnchantment(Enchantment.DURABILITY, 1);
              ab.getItemsToSync().put(e.getCurrentItem().getType(), true);
              return;
            }
          }
        }
      }

      else if(currentGUI instanceof EditDefaultAbilitiesGUI) {
        EditDefaultAbilitiesGUI editDefaultAbilitiesGUI = (EditDefaultAbilitiesGUI) currentGUI;
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_DEFAULT_ABILITIES_GUI);
        if(e.getSlot() == guiConfig.getInt("BackButton.Slot")){
          if(GUITracker.doesPlayerHavePrevious(mp)) {
            currentGUI.setClearData(false);
            GUI old = GUITracker.getPlayersPreviousGUI(mp);
            old.setClearData(true);
            p.openInventory(old.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, old);
            return;
          }
        }
        if(e.getSlot() > editDefaultAbilitiesGUI.getDefaultAbilityList().size() - 1 || e.getSlot() == 0) {
          return;
        }
        //TODO cuz i havent finished archery... ^^ and remove the +1
        BaseAbility abilityToChange = editDefaultAbilitiesGUI.getDefaultAbilityList().get(e.getSlot() -1);
        abilityToChange.setToggled(!abilityToChange.isToggled());
        if(!abilityToChange.isToggled()) {
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
        else {
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
      else if(currentGUI instanceof EditLoadoutGUI) {
        EditLoadoutGUI editLoadoutGUI = (EditLoadoutGUI) currentGUI;
        FileConfiguration guiConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.EDIT_LOADOUT_GUI);
        if(e.getSlot() > mp.getAbilityLoadout().size() - 1) {
          return;
        }
        BaseAbility abilityToChange = mp.getBaseAbility(editLoadoutGUI.getAbilityFromSlot(e.getSlot()));
        if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.TOGGLE) {
          if(abilityToChange.getGenericAbility() == UnlockedAbilities.REMOTE_TRANSFER) {
            RemoteTransferGUI remoteTransferGUI = new RemoteTransferGUI(mp, mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER));
            currentGUI.setClearData(false);
            p.openInventory(remoteTransferGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, remoteTransferGUI);
            return;
          }
          abilityToChange.setToggled(!abilityToChange.isToggled());
          if(!abilityToChange.isToggled()) {
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
          else {
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
        else if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_UPGRADE) {
          UnlockedAbilities unlockedAbility = (UnlockedAbilities) abilityToChange.getGenericAbility();
          if(abilityToChange.getCurrentTier() < 5) {
            if(unlockedAbility.tierUnlockLevel(abilityToChange.getCurrentTier() + 1) > mp.getSkill(unlockedAbility.getSkill()).getCurrentLevel()) {
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
              return;
            }
            AcceptAbilityGUI gui = new AcceptAbilityGUI(mp, abilityToChange, AcceptAbilityGUI.AcceptType.ACCEPT_UPGRADE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else {
            p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
            return;
          }
        }
        else {
          if(editLoadoutGUI.getReplaceAbility().getGenericAbility().getAbilityType() == AbilityType.ACTIVE) {
            for(int i = 0; i < mp.getAbilityLoadout().size(); i++) {
              UnlockedAbilities unlockedAbilities = mp.getAbilityLoadout().get(i);
              if(e.getSlot() != i && unlockedAbilities.getAbilityType() == AbilityType.ACTIVE && unlockedAbilities.getSkill().equalsIgnoreCase(editLoadoutGUI.getReplaceAbility().getGenericAbility().getSkill())) {
                p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
                p.closeInventory();
                p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.HasActive")));
                return;
              }
            }
          }
          editLoadoutGUI.getAbilities().set(e.getSlot(), (UnlockedAbilities) editLoadoutGUI.getReplaceAbility().getGenericAbility());
          mp.getAbilityLoadout().set(e.getSlot(), (UnlockedAbilities) editLoadoutGUI.getReplaceAbility().getGenericAbility());
          p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Guis.AcceptedAbility").replace("%Ability%", editLoadoutGUI.getReplaceAbility().getGenericAbility().getName())));
          if(editLoadoutGUI.getEditType() == EditLoadoutGUI.EditType.ABILITY_REPLACE) {
            int cooldown = McRPG.getInstance().getConfig().getInt("Configuration.ReplaceAbilityCooldown");
            if(cooldown != 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.MINUTE, cooldown);
              mp.setEndTimeForReplaceCooldown(cal.getTimeInMillis());
            }
          }
          p.closeInventory();
        }
        mp.saveData();
        return;
      }

      GUIEventBinder binder = null;
      if(currentGUI.getGui().getBoundEvents() != null) {
        binder = currentGUI.getGui().getBoundEvents().stream().filter(guiBinder -> guiBinder.getSlot() == e.getSlot()).findFirst().orElse(null);
      }
      if(binder == null) return;
      for(String eventBinder : binder.getBoundEventList()) {
        String[] events = eventBinder.split(":");
        String event = events[0];
        if(event.equalsIgnoreCase("Permission")) {
          String perm = events[1];
          if(!p.hasPermission(perm)) {
            GUITracker.stopTrackingPlayer(p);
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + config.getString("Messages.Commands.Utility.NoPerms")));
            return;
          }
          else {
            continue;
          }
        }
        else if(event.equalsIgnoreCase("Command")) {
          String sender = events[1];
          if(sender.equalsIgnoreCase("console")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), events[2].replaceAll("%Player%", p.getName()));
            continue;
          }
          else if(sender.equalsIgnoreCase("player")) {
            p.performCommand(events[2]);
            continue;
          }
        }
        else if(event.equalsIgnoreCase("close")) {
          GUITracker.stopTrackingPlayer(p);
          p.closeInventory();
          continue;
        }
        else if(event.equalsIgnoreCase("back")) {
          if(GUITracker.doesPlayerHavePrevious(p)) {
            GUI previousGUI = GUITracker.getPlayersPreviousGUI(p);
            previousGUI.setClearData(true);
            currentGUI.setClearData(false);
            p.openInventory(previousGUI.getGui().getInv());
            GUITracker.replacePlayersGUI(p, previousGUI);
            continue;
          }
          else {
            GUITracker.stopTrackingPlayer(p);
            continue;
          }
        }
        else if(event.equalsIgnoreCase("Open")) {
          GUITracker.stopTrackingPlayer(p);
          p.closeInventory();
          p.sendMessage(Methods.color("&cThis has yet to be implemented"));
          return;
        }
        else if(event.equalsIgnoreCase("OpenNative")) {
          GUI gui = null;
          if(events[1].equalsIgnoreCase("EditLoadoutGUI")) {
            gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.TOGGLE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("EditLoadoutSelectGUI")) {
            gui = new EditLoadoutSelectGUI(mp);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SettingsGUI")) {
            gui = new SettingsGUI(mp);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("UpgradeAbilityGUI")) {
            if(mp.getAbilityPoints() == 0) {
              p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 5, 1);
              return;
            }
            gui = new EditLoadoutGUI(mp, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SubSkillGUI")) {
            Skills skill = Skills.fromString(events[2]);
            gui = new SubSkillGUI(mp, skill);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
          else if(events[1].equalsIgnoreCase("SelectReplaceGUI")) {
            Skills skill = Skills.fromString(events[2]);
            gui = new SelectReplaceGUI(mp, skill);
            currentGUI.setClearData(false);
            p.openInventory(gui.getGui().getInv());
            GUITracker.replacePlayersGUI(mp, gui);
            return;
          }
        }
        else if(event.equalsIgnoreCase("OpenFile")) {
          GUI gui = null;
          if(events[1].equalsIgnoreCase("skillsgui.yml")) {
            gui = new SkillGUI(mp);
          }
          else if(events[1].equalsIgnoreCase("maingui.yml")) {
            gui = new HomeGUI(mp);
          }
          else {
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
}
