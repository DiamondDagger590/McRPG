package us.eunoians.mcrpg.util.configuration;

import de.articdive.enum_to_yaml.interfaces.ConfigurationEnum;

import java.util.Collections;

public enum ConfigEnum implements ConfigurationEnum {

  CONFIGURATION("Configuration", "", "##################################",
          "### Main Config For McRPG ###", "##################################", "#", "#Notes:",
          "#    Do not use ' or \" in messages as I have not yet added support for these icons.",
          "#   - Do not remove any lines as that will throw errors", "#   - This plugin only supports Minecraft color codes. Ex) &eHello World&7!",
          "#       - Use this link for a list of the codes: https://wiki.ess3.net/mc/", "#   - If you want a message to be blank, please leave it as ''",
          "#   - This plugin supports player name place holders as %Player%", "# If you receive an error when McRPG loads, ensure that:",
          "#   - No tabs are present: YAML only allows spaces", "#   - Indents are correct: YAML hierarchy is based entirely on indentation",
          "###################################"),
  MCMMO_CONVERSION_EQUATION("Configuration.McMMOConversionEquation", "((skill_exp)*0.5)", "#Converts a players level to exp and then convert that into boosted experience using the equation below"),
  BOOSTED_EXP_USAGE_RATE("Configuration.BoostedExpUsageRate", "((gained_exp)*2.25)", "#When a player gains exp, this equation is factored in and if there is remaining boosted exp,", "#then it will add this equation value to the gained amount"),
  DISABLE_TIPS("Configuration.DisableTips", false, "#If all tips should be disabled"),
  MAX_DAMAGE_CAP("Configuration.MaxDamageCap", 1000000, "#The maximum amount of damage allowed for giving experience"),
  EXP_MULTIPLIER_CAP("Configuration.ExpMultiplierCap", 3.0, "#The max amount of exp multiplier that a player should be able to get from various exp modifiers"),
  SHIELD_BLOCKING_MODIFIER("Configuration.ShieldBlockingModifier", 0.5, "#How much exp gain should be modified if the target is blocking with a shield."),
  DISABLE_ENDER_PEARL_EXP("Configuration.DisableEPearlExp", true, "#If exp should be disabled for damage from ender pearls"),
  DISABLE_BOOKS_IN_END("Configuration.DisableBooksInEnd", true, "#If skill books should be undroppable in the end"),
  ABILITY_SPY_ENABLED("Configuration.AbilitySpyEnabled", false, "#If ability spy should be on, alerting admins when abilities are unlocked and upgraded"),
  USE_REDEEM_PERM("Configuration.UseRedeemPerm", false, "#If redeem perms should be used"),
  CONSTANT_EXP_UPDATES_ENABLED("Configuration.ConstantExpUpdates.Enabled", false, "#If players should be sent an update everytime they gain exp"),
  CONSTANT_EXP_UPDATES_DISPLAY_TYPE("Configuration.ConstantExpUpdates.DisplayType", "BOSSBAR", "#What type should the display be. BOSSBAR, SCOREBOARD, or ACTIONBAR"),
  CONSTANT_EXP_UPDATES_DISPLAY_DURATION("Configuration.ConstantExpUpdates.DisplayDuration", 3, "#Duration of the reminder. Only used for scoreboard and bossbar"),
  DISABLED_WORLDS("Configuration.DisabledWorlds", Collections.singletonList("test"), "#What worlds should be disabled"),
  SAVE_INTERVAL("Configuration.SaveInterval", 1, "#This is how often the plugin saves player data (async) in minutes"),
  REPLACE_ABILITY_COOLDOWN("Configuration.ReplaceAbilityCooldown", 1440, "#How long the cooldown for replacing an ability should be in minutes"),
  REQUIRE_EMPTY_OFF_HAND("Configuration.RequireEmptyOffHand", false, "#Should a player be required to have an empty offhand to use abilities"),
  LANG_FILE("Configuration.LangFile", "en", "#What lang file you want to use. Do not include the .yml"),
  AUTO_UPDATE("Configuration.AutoUpdate", true, "#Currently unused"),
  USE_LEVEL_PERMS("Configuration.UseLevelPerms", false, "#If a player should not gain exp when they are a certain level.", "#Use mcrpg.%skill%.%level% as the perm"),
  HEALTH_BAR_ENABLED("Configuration.HealthBarEnabled", true, "#If health bars of mobs should be universally disabled"),
  HEALTH_BAR_DISPLAY_TIME("Configuration.HealthBarDisplayTime", 5, "#How long should the health bars be displayed for"),
  MODIFY_MOB_EXP_PARENT("Configuration.ModifySpawnExp", "", "#Modify the exp worth of mobs from spawners and eggs"),
  MODIFY_SPAWNER_MOB_EXP("Configuration.ModifySpawnExp.MobsFromSpawner", 0.5),
  MODIFY_EGG_MOB_EXP("Configuration.ModifySpawnExp.MobsFromEggs", 0.5),
  REDEEM_LEVELS_RESET_EXP("Configuration.Redeeming.RedeemLevelsResetExp", true, "#If when players redeem levels, should this reset the amount of exp needed to level up."),
  DISPLAY_CONFIG_PARENT("DisplayConfig", "", "#Use this to configure how various displays show up"),
  SCOREBOARD_DISPLAY_NAME("DisplayConfig.Scoreboard.DisplayName", "&a%Skill%"),
  SCOREBOARD_LINES_CURRENT_LEVEL("DisplayConfig.Scoreboard.Lines.CurrentLevel", "&bCurrent Level:"),
  SCOREBOARD_LINES_CURRENT_EXP("DisplayConfig.Scoreboard.Lines.CurrentExp", "&eExp:"),
  SCOREBOARD_LINES_EXP_NEEDED("DisplayConfig.Scoreboard.Lines.ExpNeeded", "&eExp Left:"),
  BOSSBAR_DISPLAY_NAME("DisplayConfig.BossBar.DisplayName", "&5Lv.&e%Current_Level% &7- &5%Skill%: &e%Exp_To_Level%"),
  BOSSBAR_COLOR_SWORDS("DisplayConfig.BossBar.Color.Swords", "Red"),
  BOSSBAR_COLOR_MINING("DisplayConfig.BossBar.Color.Mining", "Blue"),
  BOSSBAR_COLOR_UNARMED("DisplayConfig.BossBar.Color.Unarmed", "White"),
  BOSSBAR_COLOR_HERBALISM("DisplayConfig.BossBar.Color.Herbalism", "Green"),
  BOSSBAR_COLOR_ARCHERY("DisplayConfig.BossBar.Color.Archery", "Pink"),
  BOSSBAR_COLOR_WOODCUTTING("DisplayConfig.BossBar.Color.Woodcutting", "Green"),
  BOSSBAR_COLOR_FITNESS("DisplayConfig.BossBar.Color.Fitness", "White"),
  BOSSBAR_COLOR_EXCAVATION("DisplayConfig.BossBar.Color.Excavation", "Blue"),
  BOSSBAR_COLOR_AXES("DisplayConfig.BossBar.Color.Axes", "Red"),
  BOSSBAR_COLOR_FISHING("DisplayConfig.BossBar.Color.Fishing", "Blue"),
  BOSSBAR_COLOR_SORCERY("DisplayConfig.BossBar.Color.Sorcery", "Pink"),
  BOSSBAR_COLOR_TAMING("DisplayConfig.BossBar.Color.Taming", "Red"),
  ACTIONBAR_SWORDS_MESSAGE("DisplayConfig.ActionBar.Swords.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_MINING_MESSAGE("DisplayConfig.ActionBar.Mining.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_UNARMED_MESSAGE("DisplayConfig.ActionBar.Unarmed.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_HERBALISM_MESSAGE("DisplayConfig.ActionBar.Herbalism.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_ARCHERY_MESSAGE("DisplayConfig.ActionBar.Archery.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_WOODCUTTING_MESSAGE("DisplayConfig.ActionBar.Woodcutting.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_FITNESS_MESSAGE("DisplayConfig.ActionBar.Fitness.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_EXCAVATION_MESSAGE("DisplayConfig.ActionBar.Excavation.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_AXES_MESSAGE("DisplayConfig.ActionBar.Axes.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_FISHING_MESSAGE("DisplayConfig.ActionBar.Fishing.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_SORCERY_MESSAGE("DisplayConfig.ActionBar.Sorcery.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  ACTIONBAR_Taming_MESSAGE("DisplayConfig.ActionBar.Taming.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining"),
  PLAYER_CONFIGURATION_AMOUNT_OF_TOTAL_ABILITIES("PlayerConfiguration.AmountOfTotalAbilities", 9, "#This amount is how many ability slots are in the players loadout", "#Modifying this requires a restart. A reload will break plugin"),
  PLAYER_CONFIGURATION_ABILITY_POINT_INTERVAL("PlayerConfiguration.AbilityPointInterval", 100, "#When the power level reaches a multiple of this number an ability point is awarded"),
  PLAYER_CONFIGURATION_PLAYER_READY_DURATION("PlayerConfiguration.PlayerReadyDuration", 2, "#How many seconds a player should stay ready for"),
  GUARDIAN_DEFAULT_CHANCE("PoseidonsGuardian.DefaultSummonChance", 5.0, "#What summoning chance should players start with."),
  GUARDIAN_RANGE("PoseidonsGuardian.Range", 2, "    #If the distance between the last fish caught and the next one caught is less than or equal to this value, increase the summoning chance."),
  GUARDIAN_MAX_CHANCE("PoseidonsGuardian.MaxChance", 60.0, "#The max summoning chance that a player can have for the guardian."),
  GUARDIAN_MIN_CHANCE("PoseidonsGuardian.MinChance", 5.0, "#The min summoning chance that a player can have for the guardian."),
  GUARDIAN_INCREASE("PoseidonsGuardian.WithinRangeIncrease", 2.0, "#How much should the summon chance increase if the fish caught is inside of the range."),
  GUARDIAN_DECREASE("PoseidonsGuardian.OutsideRangeDecrease", 1.0, "#How much should the summon chance decrease if the fish caught is outside of the range."),
  GUARDIAN_TYPE("PoseidonsGuardian.GuardianType", "DROWNED", "#What type of entity should the guardian be."),
  GUARDIAN_HEALTH("PoseidonsGuardian.Health", 150, "#How much health should the guardian have."),
  GUARDIAN_WEAPON("PoseidonsGuardian.Weapon", "IRON_SWORD", "#What weapon should the guardian have."),
  GUARDIAN_ENCHANTED("PoseidonsGuardian.Enchanted", false, "#Should the weapon be enchanted."),
  GUARDIAN_ENCHANTS("PoseidonsGuardian.Enchants", Collections.singletonList("DAMAGE_ALL:1"), "#What enchantments should be on the weapon."),
  GUARDIAN_EXP("PoseidonsGuardian.RedeemableExpReward", 5000, "#How much redeemable exp should the player get when killing the guardian.");
  private String path;
  private Object defaultValue;
  private String[] comments;

  ConfigEnum(String path, Object defaultValue, String... comments) {
    this.path = path;
    this.defaultValue = defaultValue;
    this.comments = comments;
  }


  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String[] getComments() {
    return comments;
  }
}
