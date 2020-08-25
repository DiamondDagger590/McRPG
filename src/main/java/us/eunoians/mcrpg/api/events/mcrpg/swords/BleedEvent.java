package us.eunoians.mcrpg.api.events.mcrpg.swords;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.Bleed;
import us.eunoians.mcrpg.abilities.swords.BleedPlus;
import us.eunoians.mcrpg.abilities.swords.DeeperWound;
import us.eunoians.mcrpg.abilities.swords.Vampire;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.Random;

public class BleedEvent extends AbilityActivateEvent{
  
  @Getter
  private Entity target;
  @Getter
  private Bleed bleed;
  @Getter @Setter
  private int damage;
  @Getter @Setter
  private int frequency;
  @Getter @Setter
  private int baseDuration;
  @Getter @Setter
  private boolean pierceArmour;
  @Getter
  private int minimumHealthAllowed;
  @Getter @Setter
  private boolean bleedImmunityEnabled;
  @Getter @Setter
  private int bleedImmunityDuration;
  
  
  public BleedEvent(McRPGPlayer user, LivingEntity target, Bleed bleed){
    super(bleed, user, AbilityEventType.COMBAT);
    //Get the swords config
    FileConfiguration config = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
    //Initialize everything
    this.bleed = bleed;
    isCancelled = bleed.isToggled();
    this.target = target;
    this.damage = config.getInt("BleedConfig.BaseDamage");
    this.minimumHealthAllowed = config.getInt("BleedConfig.MinimumHealthAllowed");
    this.frequency = config.getInt("BleedConfig.Frequency");
    this.baseDuration = config.getInt("BleedConfig.BaseDuration");
    this.pierceArmour = config.getBoolean("BleedConfig.BleedPierceArmour");
    this.bleedImmunityEnabled = config.getBoolean("BleedConfig.BleedImmunityEnabled");
    this.bleedImmunityDuration = config.getInt("BleedConfig.BleedImmunityDuration");
    
    //If deeper wound is unlocked and is enabled. No need to check for toggle as the event will check that
    if(UnlockedAbilities.DEEPER_WOUND.isEnabled() && user.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.DEEPER_WOUND)){
      //Check if it should activate based on % chance
      Random ran = new Random();
      int range = ran.nextInt(10000);
      int chance = (int) config.getDouble("DeeperWoundConfig.Tier" + Methods.convertToNumeral(user.getBaseAbility(UnlockedAbilities.DEEPER_WOUND).getCurrentTier())
                                            + ".ActivationChance") * 100;
      if(chance >= range){
        DeeperWoundEvent deeperWoundEvent = new DeeperWoundEvent(user, (DeeperWound) user.getBaseAbility(UnlockedAbilities.DEEPER_WOUND));
        Bukkit.getPluginManager().callEvent(deeperWoundEvent);
        if(!deeperWoundEvent.isCancelled()){
          baseDuration += deeperWoundEvent.getDurationBoost();
        }
      }
    }
    //If bleed+ is unlocked and enabled
    if(UnlockedAbilities.BLEED_PLUS.isEnabled() && user.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.BLEED_PLUS)){
      Random ran = new Random();
      int range = ran.nextInt(10000);
      int chance = (int) config.getDouble("Bleed+Config.Tier" + Methods.convertToNumeral(user.getBaseAbility(UnlockedAbilities.BLEED_PLUS).getCurrentTier())
                                            + ".ActivationChance") * 100;
      if(chance >= range){
        BleedPlusEvent bleedPlusEvent = new BleedPlusEvent(user, (BleedPlus) user.getBaseAbility(UnlockedAbilities.BLEED_PLUS));
        Bukkit.getPluginManager().callEvent(bleedPlusEvent);
        if(!bleedPlusEvent.isCancelled()){
          damage += bleedPlusEvent.getDamageBoost();
        }
      }
    }
    //If vampire is unlocked and enabled
    if(UnlockedAbilities.VAMPIRE.isEnabled() && user.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.VAMPIRE) && target instanceof Player){
      Random ran = new Random();
      int range = ran.nextInt(10000);
      int chance = (int) config.getDouble("VampireConfig.Tier" + Methods.convertToNumeral(user.getBaseAbility(UnlockedAbilities.VAMPIRE).getCurrentTier())
                                            + ".ActivationChance") * 100;
      if(chance >= range){
        VampireEvent vampireEvent = new VampireEvent(user, (Vampire) user.getBaseAbility(UnlockedAbilities.VAMPIRE));
        Bukkit.getPluginManager().callEvent(vampireEvent);
        if(!vampireEvent.isCancelled()){
          double userHealth = user.getPlayer().getHealth();
          if(userHealth < 20){
            if(userHealth + vampireEvent.getAmountToHeal() > 20){
              user.getPlayer().setHealth(20);
            }
            else{
              user.getPlayer().setHealth(userHealth + vampireEvent.getAmountToHeal());
            }
          }
        }
      }
    }
  }
}