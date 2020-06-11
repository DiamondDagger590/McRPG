package us.eunoians.mcrpg.events.vanilla;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.herbalism.NaturesWrath;
import us.eunoians.mcrpg.api.events.mcrpg.herbalism.NaturesWrathEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.books.SkillBookFactory;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.players.PlayerReadyBit;
import us.eunoians.mcrpg.types.GainReason;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CheckReadyEvent implements Listener{
  
  private static final ArrayList<UUID> playersToIgnore = new ArrayList<>();
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void checkReady(PlayerInteractEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      return;
    }
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getPlayer().getWorld().getName())) {
      return;
    }
    Player p = e.getPlayer();
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(p.getUniqueId());
    }catch(McRPGPlayerNotFoundException exception){
      return;
    }
    ItemStack heldItem = e.getItem();
    Calendar cal = Calendar.getInstance();
    
    //skill book checks
    if(heldItem != null && e.getAction() != null && e.getHand() == EquipmentSlot.HAND &&
         (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && heldItem.getType() != null && heldItem.getType() == Material.ENCHANTED_BOOK && Methods.isSkillBook(heldItem)){
      NBTItem nbtItem = new NBTItem(heldItem);
      if(nbtItem.hasKey("UpgradeSkill")){
        UnlockedAbilities ab = UnlockedAbilities.fromString(nbtItem.getString("UpgradeAbility"));
        BaseAbility baseAbility = mp.getBaseAbility(ab);
        if(baseAbility.isUnlocked()){
          if(baseAbility.getCurrentTier() <= nbtItem.getInteger("UpgradeHighTier") && baseAbility.getCurrentTier() >= nbtItem.getInteger("UpgradeLowTier")){
            if(nbtItem.getBoolean("RequireLevel")){
              Skills skill = Skills.fromString(nbtItem.getString("RequireSkill"));
              int requireLevel = nbtItem.getInteger("RequireLevel");
              if(mp.getSkill(skill).getCurrentLevel() < requireLevel){
                p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.RequiredLevel")
                                                                                         .replace("%Level%", Integer.toString(requireLevel)).replace("%Skill%", skill.getDisplayName())));
                return;
              }
            }
            int oldTier = baseAbility.getCurrentTier();
            int tierInc = nbtItem.getInteger("UpgradeTierAmount");
            int newTier = baseAbility.getCurrentTier() + tierInc <= ab.getMaxTier() ? baseAbility.getCurrentTier() + tierInc : ab.getMaxTier();
            baseAbility.setCurrentTier(newTier);
            mp.saveData();
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.Upgraded")
                                                                                     .replace("%OldTier%", Integer.toString(oldTier)).replace("%NewTier%", Integer.toString(newTier)).replace("%Ability%", ab.getDisplayName())));
            heldItem.setAmount(heldItem.getAmount() - 1);
            if(heldItem.getAmount() <= 0){
              heldItem.setType(Material.AIR);
              p.updateInventory();
            }
            return;
          }
          else{
            p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.InvalidTier")));
            return;
          }
        }
        else{
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.NoAbility")));
          return;
        }
      }
      if(nbtItem.hasKey("UnlockSkill")){
        UnlockedAbilities ab = UnlockedAbilities.fromString(nbtItem.getString("UnlockAbility"));
        BaseAbility baseAbility = mp.getBaseAbility(ab);
        if(!baseAbility.isUnlocked()){
          if(nbtItem.getBoolean("RequireLevel")){
            Skills skill = Skills.fromString(nbtItem.getString("RequireSkill"));
            int requireLevel = nbtItem.getInteger("RequireLevel");
            if(mp.getSkill(skill).getCurrentLevel() < requireLevel){
              p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.RequiredLevel")
                                                                                       .replace("%Level%", Integer.toString(requireLevel)).replace("%Skill%", skill.getDisplayName())));
              return;
            }
          }
          int unlockTier = nbtItem.getInteger("UnlockTier");
          unlockTier = unlockTier <= ab.getMaxTier() ? unlockTier : ab.getMaxTier();
          baseAbility.setCurrentTier(unlockTier);
          baseAbility.setUnlocked(true);
          mp.addPendingAbilityUnlock(ab);
          mp.saveData();
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.Unlocked")
                                                                                   .replace("%Tier%", Integer.toString(unlockTier)).replace("%Ability%", ab.getDisplayName())));
          heldItem.setAmount(heldItem.getAmount() - 1);
          if(heldItem.getAmount() <= 0){
            heldItem.setType(Material.AIR);
            p.updateInventory();
          }
          return;
        }
        else{
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.SkillBooks.AlreadyUnlocked")));
          return;
        }
      }
    }
    
    //Handle artifacts
    if(heldItem != null && e.getAction() != null && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && Methods.isArtifact(heldItem)){
      NBTItem artifact = new NBTItem(heldItem);
      if(artifact.hasKey("RedeemableExpAmount")){
        mp.giveRedeemableExp(artifact.getInteger("RedeemableExpAmount"));
        heldItem.setAmount(heldItem.getAmount() - 1);
        p.updateInventory();
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.RedeemableExpArtifactUsed")
                                                                                 .replace("%RedeemableExpAmount%", Integer.toString(artifact.getInteger("RedeemableExpAmount")))));
        return;
      }
      else if(artifact.hasKey("SkillExpAmount")){
        int expAmount = artifact.getInteger("SkillExpAmount");
        Skills skills = Skills.fromString(artifact.getString("SkillToUse"));
        if(mp.getSkill(skills).getCurrentLevel() < skills.getMaxLevel()){
          mp.giveExp(skills, expAmount, GainReason.ARTIFACT);
          heldItem.setAmount(heldItem.getAmount() - 1);
          p.updateInventory();
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.SkillExpArtifactUsed")
                                                                                   .replace("%SkillExpAmount%", Integer.toString(artifact.getInteger("SkillExpAmount")))
                                                                                   .replace("%Skill%", skills.getDisplayName())));
          return;
        }
        else{
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.SkillMaxed")));
          return;
        }
      }
      else if(artifact.hasKey("RedeemableLevelAmount")){
        mp.giveRedeemableLevels(artifact.getInteger("RedeemableLevelAmount"));
        heldItem.setAmount(heldItem.getAmount() - 1);
        p.updateInventory();
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.RedeemableLevelArtifactUsed")
                                                                                 .replace("%RedeemableLevelAmount%", Integer.toString(artifact.getInteger("RedeemableLevelAmount")))));
        return;
      }
      else if(artifact.hasKey("AbilityPointAmount")){
        mp.setAbilityPoints(mp.getAbilityPoints() + artifact.getInteger("AbilityPointAmount"));
        heldItem.setAmount(heldItem.getAmount() - 1);
        p.updateInventory();
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.AbilityPointArtifactUsed")
                                                                                 .replace("%AbilityPointAmount%", Integer.toString(artifact.getInteger("AbilityPointAmount")))));
        return;
      }
      else if(artifact.hasKey("UnlockBookAmount")){
        for(int i = 0; i < artifact.getInteger("UnlockBookAmount"); i++){
          p.getLocation().getWorld().dropItemNaturally(p.getLocation(), SkillBookFactory.generateUnlockBook());
        }
        heldItem.setAmount(heldItem.getAmount() - 1);
        p.updateInventory();
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.UnlockBookArtifactUsed")
                                                                                 .replace("%UnlockBookAmount%", Integer.toString(artifact.getInteger("UnlockBookAmount")))));
        return;
      }
      else if(artifact.hasKey("UpgradeBookAmount")){
        for(int i = 0; i < artifact.getInteger("UpgradeBookAmount"); i++){
          p.getLocation().getWorld().dropItemNaturally(p.getLocation(), SkillBookFactory.generateUpgradeBook());
        }
        heldItem.setAmount(heldItem.getAmount() - 1);
        p.updateInventory();
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.UpgradeBookArtifactUsed")
                                                                                 .replace("%UpgradeBookAmount%", Integer.toString(artifact.getInteger("UpgradeBookAmount")))));
        return;
      }
      else if(artifact.hasKey("CooldownReset") && cal.getTimeInMillis() >= mp.getCooldownResetArtifactCooldownTime()){
        int remainingUses = artifact.getInteger("RemainingUseAmount");
        int maxUses = artifact.getInteger("MaxUseAmount");
        if(remainingUses - 1 == 0){
          heldItem.setAmount(heldItem.getAmount() - 1);
          p.updateInventory();
        }
        else{
          artifact.setInteger("RemainingUseAmount", remainingUses - 1);
          ItemStack item = artifact.getItem();
          ItemMeta meta = item.getItemMeta();
          FileConfiguration artifactFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARTIFACT_FILE);
          List<String> newLore = new ArrayList<>();
          for(String s : artifactFile.getStringList(artifact.getString("CreationType") + ".Effects.CooldownReset.Lore")){
            newLore.add(Methods.color(s.replace("%RemainingUsesAmount%", Integer.toString(remainingUses - 1)).replace("%MaxUsesAmount%", Integer.toString(maxUses))));
          }
          meta.setLore(newLore);
          item.setItemMeta(meta);
          p.getInventory().setItemInMainHand(item);
        }
        mp.resetCooldowns();
        cal.add(Calendar.SECOND, 5);
        mp.setCooldownResetArtifactCooldownTime(cal.getTimeInMillis());
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.CooldownArtifactUsed")));
        return;
      }
      else if(artifact.hasKey("MagnetRange") && cal.getTimeInMillis() >= mp.getMagnetArtifactCooldownTime()){
        int remainingUses = artifact.getInteger("RemainingUseAmount");
        int maxUses = artifact.getInteger("MaxUseAmount");
        int magnetRange = artifact.getInteger("MagnetRange");
        if(remainingUses - 1 == 0){
          heldItem.setAmount(heldItem.getAmount() - 1);
          p.updateInventory();
        }
        else{
          artifact.setInteger("RemainingUseAmount", remainingUses - 1);
          ItemStack item = artifact.getItem();
          ItemMeta meta = item.getItemMeta();
          FileConfiguration artifactFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.ARTIFACT_FILE);
          List<String> newLore = new ArrayList<>();
          for(String s : artifactFile.getStringList(artifact.getString("CreationType") + ".Effects.Magnet.Lore")){
            newLore.add(Methods.color(s.replace("%MagnetRadius%", Integer.toString(magnetRange)).replace("%RemainingMagnetUses%", Integer.toString(remainingUses - 1)).replace("%MaxMagnetUses%", Integer.toString(maxUses))));
          }
          meta.setLore(newLore);
          item.setItemMeta(meta);
          p.getInventory().setItemInMainHand(item);
        }
        //Following code has been is a modification of https://github.com/heatseeker0/ItemMagnet/blob/master/src/main/java/com/mcspacecraft/itemmagnet/ItemMagnet.java
        for(Entity entity : p.getNearbyEntities(magnetRange, 1, magnetRange)){
          if(entity instanceof Item){
            Item item = (Item) entity;
            Location itemLocation = item.getLocation();
            
            // Make sure we don't pick up items we just threw on the ground
            if(item.getPickupDelay() > item.getTicksLived()){
              continue;
            }
            //double playerDistance = p.getLocation().distanceSquared(itemLocation);
            item.setVelocity(p.getLocation().toVector().subtract(itemLocation.toVector()));//.normalize());
          }
        }
        cal.add(Calendar.SECOND, 3);
        mp.setMagnetArtifactCooldownTime(cal.getTimeInMillis());
        p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Artifacts.MagnetArtifactUsed")));
        return;
      }
    }
    
    //verify a proper ready action/special case for archery
    if(e.isCancelled() && e.getAction() != Action.RIGHT_CLICK_AIR){
      if(e.getHand() != null && e.getAction() != null && heldItem != null && e.getHand() == EquipmentSlot.HAND && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && heldItem.getType() == Material.BOW){
        if(mp.isReadying()){
          return;
        }
        Skills skillType = Skills.ARCHERY;
        if(mp.getCooldown(skillType) != -1){
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                                           McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownActive").replace("%Skill%", skillType.getDisplayName())
                                             .replace("%Time%", Integer.toString((int) mp.getCooldown(skillType)))));
          return;
        }
        readyHandler(p, mp, skillType, e.getClickedBlock());
        return;
      }
      return;
    }
    
    Block target = e.getClickedBlock();
    Material type;
    if(target == null){
      type = Material.AIR;
    }
    else{
      type = target.getType();
    }
    
    if(heldItem == null){
      heldItem = new ItemStack(Material.AIR);
    }
    
    if(type == Material.CHEST || type == Material.ENDER_CHEST || type == Material.TRAPPED_CHEST
         || type == Material.BEACON || type.name().contains("DOOR") || type.name().contains("SIGN") || type.name().contains("SHULKER") || type == Material.ENCHANTING_TABLE || type == Material.LEVER
         || type.name().contains("BUTTON") || type == Material.REPEATER || type == Material.COMPARATOR || type == Material.CRAFTING_TABLE || type.name().contains("FURNACE") || type.name().contains("SMOKER")
         || type == Material.ITEM_FRAME || type.name().contains("CARTOGRAPHY") || type.name().contains("COMPOSTER")){
      return;
    }
    if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && UnlockedAbilities.NATURES_WRATH.isEnabled() &&
         mp.getAbilityLoadout().contains(UnlockedAbilities.NATURES_WRATH) && mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH).isToggled()){
      //verify they have a weapon or tool. prevents annoying food bug
      if(Methods.getSkillsItem(p.getInventory().getItemInMainHand().getType(), type) != null && !playersToIgnore.contains(p.getUniqueId())){
        NaturesWrath naturesWrath = (NaturesWrath) mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH);
        FileConfiguration herbalism = McRPG.getInstance().getFileManager().getFile(FileManager.Files.HERBALISM_CONFIG);
        String key = "NaturesWrathConfig.Tier" + Methods.convertToNumeral(naturesWrath.getCurrentTier()) + ".";
        ItemStack offHand = p.getInventory().getItemInOffHand();
        if((offHand != null && Methods.isDiamondFlower(offHand.getType())) ||
             (p.getItemInHand() != null && Methods.isDiamondFlower(p.getItemInHand().getType()))){
          boolean isOffHand = Methods.isDiamondFlower(offHand.getType());
          Material itemType = isOffHand ? offHand.getType() : p.getItemInHand().getType();
          FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
          Sound eatSound = Sound.valueOf(soundFile.getString("Sounds.Herbalism.NaturesWrath.Sound"));
          float volume = Float.parseFloat(soundFile.getString("Sounds.Herbalism.NaturesWrath.Volume"));
          float pitch = Float.parseFloat(soundFile.getString("Sounds.Herbalism.NaturesWrath.Pitch"));
          if(itemType == Material.POPPY && herbalism.getBoolean(key + itemType.toString())){
            int hungerLost = herbalism.getInt(key + "HungerLost");
            int hungerLimit = herbalism.getInt(key + "HungerLimit");
            if(p.getFoodLevel() - hungerLost >= hungerLimit){
              int modifier = herbalism.getInt(key + "Modifier");
              int duration = herbalism.getInt(key + "Duration");
              PotionEffectType effectType = PotionEffectType.INCREASE_DAMAGE;
              NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
              Bukkit.getPluginManager().callEvent(naturesWrathEvent);
              if(!naturesWrathEvent.isCancelled()){
                p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.POPPY));
                p.getLocation().getWorld().playSound(p.getLocation(), eatSound, volume, pitch);
                if(isOffHand){
                  p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                }
                else{
                  p.getItemInHand().setAmount(p.getItemInHand().getAmount());
                }
                p.updateInventory();
                p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
                p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
              }
            }
          }
          else if(itemType == Material.DANDELION && herbalism.getBoolean(key + itemType.toString())){
            int hungerLost = herbalism.getInt(key + "HungerLost");
            int hungerLimit = herbalism.getInt(key + "HungerLimit");
            if(p.getFoodLevel() - hungerLost >= hungerLimit){
              int modifier = herbalism.getInt(key + "Modifier");
              int duration = herbalism.getInt(key + "Duration");
              PotionEffectType effectType = PotionEffectType.FAST_DIGGING;
              NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
              Bukkit.getPluginManager().callEvent(naturesWrathEvent);
              if(!naturesWrathEvent.isCancelled()){
                p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.DANDELION));
                p.getLocation().getWorld().playSound(p.getLocation(), eatSound, volume, pitch);
                if(isOffHand){
                  p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                }
                else{
                  p.getItemInHand().setAmount(p.getItemInHand().getAmount());
                }
                p.updateInventory();
                p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
                p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
              }
            }
          }
          else if(itemType == Material.BLUE_ORCHID && herbalism.getBoolean(key + itemType.toString())){
            int hungerLost = herbalism.getInt(key + "HungerLost");
            int hungerLimit = herbalism.getInt(key + "HungerLimit");
            if(p.getFoodLevel() - hungerLost >= hungerLimit){
              int modifier = herbalism.getInt(key + "Modifier");
              int duration = herbalism.getInt(key + "Duration");
              PotionEffectType effectType = PotionEffectType.SPEED;
              NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
              Bukkit.getPluginManager().callEvent(naturesWrathEvent);
              if(!naturesWrathEvent.isCancelled()){
                p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.BLUE_ORCHID));
                p.getLocation().getWorld().playSound(p.getLocation(), eatSound, volume, pitch);
                if(isOffHand){
                  p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                }
                else{
                  p.getItemInHand().setAmount(p.getItemInHand().getAmount());
                }
                p.updateInventory();
                p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
                p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
              }
            }
          }
          else if(itemType == Material.LILAC && herbalism.getBoolean(key + itemType.toString())){
            int hungerLost = herbalism.getInt(key + "HungerLost");
            int hungerLimit = herbalism.getInt(key + "HungerLimit");
            if(p.getFoodLevel() - hungerLost >= hungerLimit){
              int modifier = herbalism.getInt(key + "Modifier");
              int duration = herbalism.getInt(key + "Duration");
              PotionEffectType effectType = PotionEffectType.DAMAGE_RESISTANCE;
              NaturesWrathEvent naturesWrathEvent = new NaturesWrathEvent(mp, naturesWrath, hungerLost, modifier, effectType, duration);
              Bukkit.getPluginManager().callEvent(naturesWrathEvent);
              if(!naturesWrathEvent.isCancelled()){
                p.getLocation().getWorld().spawnParticle(Particle.ITEM_CRACK, p.getEyeLocation(), 1, 0, 0, 0, new ItemStack(Material.LILAC));
                p.getLocation().getWorld().playSound(p.getLocation(), eatSound, volume, pitch);
                if(isOffHand){
                  p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
                }
                else{
                  p.getItemInHand().setAmount(p.getItemInHand().getAmount());
                }
                p.updateInventory();
                p.setFoodLevel(p.getFoodLevel() - naturesWrathEvent.getHungerLost());
                p.addPotionEffect(new PotionEffect(naturesWrathEvent.getEffectType(), naturesWrathEvent.getDuration() * 20, naturesWrathEvent.getModifier() - 1));
              }
            }
          }
          playersToIgnore.add(p.getUniqueId());
          new BukkitRunnable(){
            @Override
            public void run(){
              playersToIgnore.remove(p.getUniqueId());
            }
          }.runTaskLater(McRPG.getInstance(), 3 * 20);
        }
      }
    }
    //Verify that the player is in a state able to ready abilities
    //If the slot is not their hand or they arent right clicking or the player is charging or if their gamemode isnt survival or if they are holding air
    if(e.getHand() == null || e.getHand() != EquipmentSlot.HAND || (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) || ShiftToggle.isPlayerCharging(p) || p.getGameMode() != GameMode.SURVIVAL){
      return;
    }
    else{
      //If they are already readying we dont need to do anything
      if(mp.isReadying()){
        return;
      }
      else{
        //Get the skill from the material of the item
        Block b = e.getClickedBlock();
        Material m = Material.AIR;
        if(b != null){
          m = b.getType();
        }
        Material heldItemType = heldItem.getType();
        if(heldItem.getType() == Material.AIR && Methods.specialHandDigggingCase(m) && mp.getAbilityLoadout().contains(UnlockedAbilities.HAND_DIGGING)){
          heldItemType = Material.DIAMOND_SHOVEL;
        }
        Skills skillType = Methods.getSkillsItem(heldItemType, m);
        if(skillType == null || skillType == Skills.ARCHERY){
          return;
        }
        if(p.getInventory().getItemInOffHand().getType() == Material.POPPY || p.getInventory().getItemInOffHand().getType() == Material.DANDELION
             || p.getInventory().getItemInOffHand().getType() == Material.LILAC || p.getInventory().getItemInOffHand().getType() == Material.BLUE_ORCHID){
          if(UnlockedAbilities.NATURES_WRATH.isEnabled() && mp.getAbilityLoadout().contains(UnlockedAbilities.NATURES_WRATH) && mp.getBaseAbility(UnlockedAbilities.NATURES_WRATH).isToggled()){
            if(!playersToIgnore.contains(p.getUniqueId())){
              return;
            }
          }
        }
        if(McRPG.getInstance().getConfig().getBoolean("Configuration.RequireEmptyOffHand") && p.getInventory().getItemInOffHand().getType() != Material.AIR){
          return;
        }
        else if(mp.isRequireEmptyOffHand() && p.getInventory().getItemInOffHand().getType() != Material.AIR){
          return;
        }
        if(mp.getCooldown(skillType) > -1){
          p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                                           McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownActive").replace("%Skill%", skillType.getDisplayName())
                                             .replace("%Time%", Integer.toString((int) mp.getCooldown(skillType)))));
          return;
        }
        else if(mp.getCooldown(skillType) <= -1){
          mp.removeAbilityOnCooldown(skillType);
        }
        readyHandler(p, mp, skillType, e.getClickedBlock());
      }
    }
  }
  
  private void readyHandler(Player p, McRPGPlayer mp, Skills skillType, Block block){
    if(mp.doesPlayerHaveActiveAbilityFromSkill(skillType)){
      BaseAbility ab = mp.getBaseAbility(mp.getActiveAbilityForSkill(skillType));
      if(mp.getActiveAbilities().contains(ab.getGenericAbility())){
        return;
      }
      if(!ab.isToggled() || !ab.getGenericAbility().isEnabled() || ab.getGenericAbility() == UnlockedAbilities.NATURES_WRATH
           || ab.getGenericAbility() == UnlockedAbilities.DEMETERS_SHRINE || ab.getGenericAbility() == UnlockedAbilities.HESPERIDES_APPLES){
        return;
      }
      String skill = "";
      if(skillType == Skills.SWORDS){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Swords");
      }
      else if(skillType == Skills.MINING){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Mining");
      }
      else if(skillType == Skills.UNARMED || (skillType == Skills.EXCAVATION && ab.getGenericAbility() == UnlockedAbilities.HAND_DIGGING)){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Unarmed");
      }
      else if(skillType == Skills.EXCAVATION){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Excavation");
      }
      else if(skillType == Skills.HERBALISM){
        if(block == null){
          return;
        }
        if(block.getType() == Material.GRASS_BLOCK){
          return;
        }
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Herbalism");
      }
      else if(skillType == Skills.ARCHERY){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Archery");
      }
      else if(skillType == Skills.WOODCUTTING){
        //To deal with bark stripping
        if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG).getBoolean("CrouchForReady") &&
             !p.isSneaking() && block.getType().toString().contains("LOG") && !block.getType().toString().contains("STRIPPED")){
          return;
        }
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Woodcutting");
      }
      else if(skillType == Skills.AXES){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Axes");
      }
      else if(skillType == Skills.TAMING){
        skill = McRPG.getInstance().getLangFile().getString("SkillItemNames.Taming");
      }
      p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() +
                                       McRPG.getInstance().getLangFile().getString("Messages.Players.PlayerReady").replace("%Skill_Item%", skill)));
      PlayerReadyBit bit = new PlayerReadyBit(mp.getActiveAbilityForSkill(skillType), mp);
      mp.setReadyingAbilityBit(bit);
      mp.setReadying(true);
    }
  }
}
