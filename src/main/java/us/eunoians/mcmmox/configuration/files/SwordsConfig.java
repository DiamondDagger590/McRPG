package us.eunoians.mcmmox.configuration.files;

import com.cyr1en.mcutils.config.ConfigManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import us.eunoians.mcmmox.api.configuration.Node;
import us.eunoians.mcmmox.api.configuration.SkillConfig;
import us.eunoians.mcmmox.configuration.BaseConfig;
import us.eunoians.mcmmox.configuration.annotations.Configuration;
import us.eunoians.mcmmox.configuration.enums.Config;
import us.eunoians.mcmmox.util.Parser;

@Configuration(type = Config.SWORDS_CONFIG, header = {""})
public class SwordsConfig extends BaseConfig implements SkillConfig {

    public SwordsConfig(ConfigManager configManager, String[] header) {
        super(configManager, header);
    }

    @Override
    public void initialize() {
        for (SwordsConfig.SwordsConfigNode node : SwordsConfig.SwordsConfigNode.values())
            initNode(node);
    }

    public double getWeaponMultiplier(Material material){
        return getDouble(MaterialBonusChildren.valueOf(material.name()));
    }

    public int getMobExpWorth(EntityType mob){
        for(ExpAwardedPerMobChildren testMob : ExpAwardedPerMobChildren.values()){
            if(testMob.key.equalsIgnoreCase(mob.name())){
                return getInt(testMob);
            }
        }
        return getInt(ExpAwardedPerMobChildren.OTHER);
    }

    public Parser getExpEquation(){
        return new Parser(getString(SwordsConfigNode.SWORDSEXPEQUATION));
    }

    enum SwordsConfigNode implements Node {
        ISENABLED("SwordsEnabled", new String[]{"Is the Swords skill enabled"}, true),
        SWORDSEXPEQUATION("SwordsExpEquation", new String[]{"Exp Equation for Swords. Allowed placeholders are:", "",
                " %power_level% - The power level of the player", " %skill_level% - The skill level of the player", " "}, "1000*1.2^%skill_level%"),
        MATERIALBONUS("MaterialBonus", new String[]{""}, MaterialBonusChildren.values()),
        EXPAWARDEDPERMOB("ExpAwardedPerMob", new String[]{""}, ExpAwardedPerMobChildren.values()),
        ISBLEEDENABLED("IsBleedEnabled", new String[] {" ", " Bleed is the default ability that every player has ",
                "This ability will increase every  time the players Swords level is increased", " ", "Is Bleed enabled on the server"}, true),
        BLEEDCONFIG("BleedConfig", new String[]{""}, BleedConfigChildren.values());

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        SwordsConfigNode(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return key;
        }
    }

    enum MaterialBonusChildren implements Node {
        WOODEN_SWORD("WOODEN_SWORD", new String[]{"How much of a multiplier should the exp gained", "be given based off weapon quality"}, 1.0),
        STONE_SWORD("STONE_SWORD", new String[]{""}, 1.1),
        GOLDEN_SWORD("GOLDEN_SWORD", new String[]{""}, 1.5),
        IRON_SWORD("IRON_SWORD", new String[]{""}, 1.2),
        DIAMOND_SWORD("DIAMOND_SWORD", new String[]{""}, 1.3);

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        MaterialBonusChildren(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }


        public String key() {
            return this.key;
        }
    }

    enum ExpAwardedPerMobChildren implements Node {
        OTHER("OTHER", new String[]{"How much base exp should be awarded per half heart of dmg", "OTHER is used for any mob not listed"}, 5),
        BAT("BAT", new String[]{}, 5),
        BLAZE("BLAZE", new String[]{}, 50),
        CAVE_SPIDER("CAVE_SPIDER", new String[]{}, 35),
        SPIDER("SPIDER", new String[]{}, 30),
        CHICKEN("CHICKEN", new String[]{}, 5),
        COW("COW", new String[]{}, 7),
        MUSHROOM_COW("MUSROOM_COW", new String[]{}, 15),
        PIG("PIG", new String[]{}, 7),
        SHEEP("SHEEP", new String[]{}, 7),
        RABBIT("RABBIT", new String[]{}, 7),
        COD("COD", new String[]{}, 7),
        TROPICAL_FISH("TROPICAL_FISH", new String[]{}, 7),
        SALMON("SALMON", new String[]{}, 7),
        PUFFERFISH("PUFFERFISH", new String[]{}, 7),
        DOLPHIN("DOLPHIN", new String[]{}, 15),
        SQUID("SQUID", new String[]{}, 7),
        TURTLE("TURTLE", new String[]{}, 10),
        DONKEY("DONKEY", new String[]{}, 10),
        MULE("MULE", new String[]{}, 10),
        HORSE("HORSE", new String[]{}, 10),
        LLAMA("LLAMA", new String[]{}, 10),
        CREEPER("CREEPER", new String[]{}, 40),
        GUARDIAN("GUARDIAN", new String[]{}, 50),
        ELDER_GUARDIAN("ELDER_GUARDIAN", new String[]{}, 90),
        ENDER_DRAGON("ENDER_DRAGON", new String[]{}, 90),
        ENDERMAN("ENDERMAN", new String[]{}, 55),
        SHULKER("SHULKER", new String[]{}, 60),
        EVOKER("EVOKER", new String[]{}, 80),
        VEX("VEX", new String[]{}, 7),
        ILLUSIONER("ILLUSIONER", new String[]{}, 70),
        IRON_GOLEM("IRON_GOLEM", new String[]{}, 10),
        SLIME("SLIME", new String[]{}, 15),
        MAGMA_CUBE("MAGMA_CUBE", new String[]{}, 20),
        OCELOT("OCELOT", new String[]{}, 10),
        PARROT("PARROT", new String[]{}, 12),
        WOLF("WOLF", new String[]{}, 10),
        PHANTOM("PHANTOM", new String[]{}, 50),
        PLAYER("PLAYER", new String[]{}, 20),
        POLAR_BEAR("POLAR_BEAR", new String[]{}, 12),
        SILVERFISH("SILVERFISH", new String[]{}, 10),
        ENDERMITE("ENDERMITE", new String[]{}, 12),
        SKELETON("SKELETON", new String[]{}, 40),
        STRAY("STRAY", new String[]{}, 45),
        WITHER_SKELETON("WITHER_SKELETON", new String[]{}, 50),
        SKELTON_HORSE("SKELETON_HORSE", new String[]{}, 30),
        SNOWMAN("SNOWMAN", new String[]{}, 5),
        VILLAGER("VILLAGER", new String[]{}, 15),
        WITCH("WITCH", new String[]{}, 45),
        ZOMBIE("ZOMBIE", new String[]{}, 35),
        ZOMBIE_VILLAGER("ZOMBIE_VILLAGER", new String[]{}, 35),
        DROWNED("DROWNED", new String[]{}, 40),
        PIG_ZOMBIE("PIG_ZOMBIE", new String[]{}, 50),
        HUSK("HUSK", new String[]{}, 40),
        ZOMBIE_HORSE("ZOMBIE_HORSE", new String[]{}, 20);

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        ExpAwardedPerMobChildren(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return this.key;
        }
    }

    enum BleedConfigChildren implements Node {
        BLEEDCHANCEEQUATION("BleedChanceEquation", new String[]{"Equation for bleed chance. Default gives 33% at level 1000"}, "(%Swords_Level%)*.033"),
        MINIMUMHEALTHALLOWED("MinimumHealthAllowed", new String[]{"The amount of health that bleed cant go lower than. " +
                "Ex) If set to 4, bleed wont bring the player below two hearts"}, 1),
        BASEDURATION("BaseDuration", new String[]{"How long bleed should last for without DeeperSlash in seconds"}, 6),
        FREQUENCY("Frequency", new String[]{"How often should bleed be applied in seconds"}, 2),
        BASEDAMAGE("BaseDamage", new String[]{"How much damage should bleed deal without BleedPlus"}, 1),
        BLEEDIMMUNITYENABLED("BleedImmunityEnabled", new String[]{"Grant immunity from bleed for a short bit after it has been applied"}, true),
        BLEEDIMMUNITYDURATION("BleedImmunityDuration", new String[]{"Duration of the immunity in seconds"}, 5);

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        BleedConfigChildren(String key, String[] comment, Object defaultValue) {
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {
            return this.key;
        }
    }

    enum BleedPlusConfigChildren implements Node {
        TIERAMOUNT("TierAmount", new String[]{"How many tiers there should be. Highly recommended to keep at 5. Do not exceed 5"}, 5),
        TIERUPGRADE("TierUpgrade", new String[] {}, BleedPlusTierUpgrade.values());

        @Getter private String key;
        @Getter private String[] comment;
        @Getter private Object defaultValue;

        BleedPlusConfigChildren(String key, String[] comment, Object defaultValue){
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;

        }

        public String key(){ return this.key;}
    }

    enum BleedPlusTierUpgrade implements Node{
        TIERII("TierII", new String[] {"At what level should each tier become available. Tier I is the unlock level thus not listed"}, 125),
        TIERIII("TierIII", new String[] {}, 225),
        TIERIV("TierIV", new String[] {}, 325),
        TIERV("TierV", new String[]{}, 500);

        @Getter String key;
        @Getter String[] comment;
        @Getter Object defaultValue;

        BleedPlusTierUpgrade(String key, String[] comment, Object defaultValue){
            this.key = key;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String key() {return this.key;}

    }

}