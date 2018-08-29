package us.eunoians.mcmmox.players;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.configuration.files.SwordsConfig;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.skills.Swords;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class McMMOPlayer {

    /**
     * The UUID of the player
     */
    @Getter private UUID uuid;
    /**
     * The power level of a player. The total sum of all of the players skill levels
     */
    @Getter private int powerLevel;
    /**
     * The amount of ability points a player has for upgrading their abilities.
     */
    @Getter @Setter private int abilityPoints;
    /**
     * The array of skills for the player
     */
    private ArrayList<Skill> skills;
    /**
     * The abilities a player has unlocked and has not yet accepted or denied. Whenever a player next opens the mcmmo main gui they should be forced to go through these
     */
    @Getter private ArrayList<GenericAbility> pendingUnlockAbilities;
    /**
     * A map containing a enum key and a long for the end time in milis of the ability
     */
    private HashMap<GenericAbility, Long> abilitiesOnCooldown;
    /**
     * The file configuration of the player that we get to edit.
     */
    //TODO Migrate these to a database manager instead of here. These are just here for white box testing
    private FileConfiguration playerData;
    private File playerFile;

    public McMMOPlayer(UUID uuid) {
        this.uuid = uuid;
        this.playerFile = new File(Mcmmox.getInstance().getDataFolder(), "PlayerData" + File.separator + uuid.toString());
        this.playerData = YamlConfiguration.loadConfiguration(playerFile);
        boolean isNew = false;
        if(!playerFile.exists()){
            isNew = true;
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(isNew){
            for(Skills type : Skills.values()){
                playerData.set(type.getName() + ".Level", 0);
                playerData.set(type.getName() + ".CurrentExp", 0);
            }
            for(UnlockedAbilities ability : UnlockedAbilities.values()){
                playerData.set(ability.getSkill() + "." + ability.getName() + ".Tier", 0);
                playerData.set(ability.getSkill() + "." + ability.getName() + ".IsToggled", true);
            }
            playerData.set("Cooldowns.PlaceHolder", null);
            playerData.set("AbilityPoints", 0);
            playerData.set("PendingAbilitiesUnlocked.placeholder", null);
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.abilityPoints = playerData.getInt("AbilityPoints");
        updatePowerLevel();
        /*
        Arrays.stream(Skills.values()).forEach(skill -> {
            ArrayList<GenericAbility> genericAbilityArrayList = new ArrayList<>();
            genericAbilityArrayList.add(DefaultAbilities.getSkillsDefaultAbility(skill.getName()));

        });*/


    }

    public OfflinePlayer getOfflineMcMMOPlayer(){
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * Updates the power level of the player by adding together all of the levels of each skill
     * @return The power level of the player
     */
    public int updatePowerLevel(){
        if(skills.isEmpty()){
            powerLevel = 0;
        }
        else {
            final AtomicInteger powerLevelUpdater = new AtomicInteger(0);
            skills.stream().forEach(skill -> powerLevelUpdater.addAndGet(skill.getCurrentLevel()));
            this.powerLevel = powerLevelUpdater.get();
        }
        return powerLevel;
    }

    /**
     * Get the instance of the players skill
     * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist
     * @return The instance of the players skill of the type provided
     */
    public Skill getSkill(String skill) {
        return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill)).findAny().orElse(null);
    }

    /**
     * Get the instance of the players skill
     * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist. Good lucky getting null out of this xD
     * @return The instance of the players skill of the type provided
     */
    public Skill getSkill(Skills skill){
        return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill.getName())).findAny().orElse(null);
    }

    /*public void giveExp(Skills skill, int exp){
        getSkill(skill).giveExp(exp);
    }*/

    /**
     * Get the cooldown of an ability.
     * @param ability The ability type you want to check the cooldown for
     * @return The endtime of the cooldown in milis. If the cooldown doesnt exist return -1
     */
    public long getCooldown(GenericAbility ability){

        if(abilitiesOnCooldown.containsKey(ability)){
            return abilitiesOnCooldown.get(ability);
        }
        else{
            return -1;
        }

    }
}
