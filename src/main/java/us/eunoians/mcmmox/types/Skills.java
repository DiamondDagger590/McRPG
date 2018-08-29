package us.eunoians.mcmmox.types;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;

import java.io.File;

/*
An enum that stores a type of every skill
 */
public enum Skills {
    SWORDS("Swords"),
    MINING("Mining"),
    AXES("Axes"),
    ARCHERY("Archery"),
    REPAIR("Repair");

    @Getter private String name;
    /**
     * TODO these are being hard coded and need removed
     */
    private File skillFile;
    private FileConfiguration skillFileConfiguration;

    Skills(String name) {
        this.name = name;
        this.skillFile = new File(Mcmmox.getInstance().getDataFolder(),
                File.separator + "Skills" + File.separator + this.getName());
        this.skillFileConfiguration = YamlConfiguration.loadConfiguration(skillFile);
    }


    public FileConfiguration getSkillFile(){
        return this.skillFileConfiguration;
    }

    public void reloadSkillFile(){
        skillFileConfiguration = YamlConfiguration.loadConfiguration(skillFile);
    }


}
