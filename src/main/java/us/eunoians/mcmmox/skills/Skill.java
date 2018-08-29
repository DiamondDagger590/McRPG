package us.eunoians.mcmmox.skills;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcmmox.Abilities.BaseAbility;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.util.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/*
A parent skill class that defines the basic behaviour of every skill
 */
public abstract class Skill {

    /**
     * The enum value of a skill
     */
    @Getter private Skills type;
    /**
     * The current level of the player in the skill
     */
    @Getter @Setter private int currentLevel;
    /**
     * The current exp a player has towards leveling up in this skill
     */
    @Getter @Setter private int currentExp;
    /**
     * The exp needed for a player to reach the next level in the skill
     */
    @Getter private int expToLevel;
    /**
     * The map of abilities a player has that is on cooldown. Keys are the enum values of abilities and the values are the end time of the cooldown in milis
     */
    @Setter private HashMap<GenericAbility, Long> abilitesOnCooldown = new HashMap<GenericAbility, Long>();
    /**
     * The map of all the abilities the skill has loaded. The key is the enum of abilities while the values are the corresponding instance of an ability
     */
    private HashMap<GenericAbility, BaseAbility> abilityMap = new HashMap<>();
    /**
     * idek what this is atm
     */
    //TODO move to a task manager
    @Getter private BukkitTask verifyAbilityCooldown;



    /**
     *
     * @param type The type of the Skill
     * @param abilities The abilities the skill has
     */
    public Skill(Skills type, ArrayList<BaseAbility> abilities, int currentLevel, int currentExp){
        this.type = type;
        this.currentLevel = currentLevel;
        this.currentExp = currentExp;
        /*verifyAbilityCooldown = new BukkitRunnable(){

            @Override
            public void run() {

            }
        }.runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("McMMOX"), 0, 20);*/
    }

    /**
     * Check if the ability specified is on cooldown
     * @param abilityName The ability you want to check the cooldown for
     * @return true if the ability is on cooldown and false if it isnt
     */
    public boolean isAbilityOnCooldown(String abilityName){
        for(GenericAbility ab : abilitesOnCooldown.keySet()){
            if(ab.getName().equalsIgnoreCase(abilityName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Get the cooldown time for the specified ability
     * @param abilityName
     * @return
     */
    public long getCooldownEndTime(String abilityName){
       for(GenericAbility ab : abilitesOnCooldown.keySet()){
           if(ab.getName().equalsIgnoreCase(abilityName)){
               return abilitesOnCooldown.get(ab);
           }
       }
       return -1;
    }

    public String getName(){
        return type.getName();
    }

   /* public void giveExp(int exp){
        currentExp = currentExp + exp;
        if(currentExp > expToLevel){
            currentExp = currentExp - expToLevel;
            currentLevel ++;
            expEquation.setVariable("%Level%", currentLevel);
            expToLevel = (int) expEquation.getValue();
        }
    }*/
}