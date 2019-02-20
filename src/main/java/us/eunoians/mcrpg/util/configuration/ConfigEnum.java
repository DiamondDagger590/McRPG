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
  DISABLED_WORLDS("Configuration.DisabledWorlds", Collections.singletonList("test"), "# What worlds should be disabled"),
  SAVE_INTERVAL("Configuration.SaveInterval", 1, "#This is how often the plugin saves player data (async) in minutes"),
  REPLACE_ABILITY_COOLDOWN("Configuration.", 1440, "#How long the cooldown for replacing an ability should be in minutes"),
  REQUIRE_EMPTY_OFF_HAND("Configuration.RequireEmptyOffHand", false, "#Should a player be required to have an empty offhand to use abilities"),
  LANG_FILE("Configuration.LangFile", "en", "#What lang file you want to use. Do not include the .yml"),
  AUTO_UPDATE("Configuration.AutoUpdate", true, "#Currently unused"),
  USE_LEVEL_PERMS("Configuration.UseLevelPerms", false, "#If a player should not gain exp when they are a certain level.", "#Use mcrpg.%skill%.%level% as the perm"),
  HEALTH_BAR_ENABLED("Configuration.HealthBarEnabled", true, "#If health bars of mobs should be universally disabled"),
  HEALTH_BAR_DISPLAY_TIME("Configuration.HealthBarDisplayTime", 5, "#How long should the health bars be displayed for"),
  MODIFY_MOB_EXP_PARENT("Configuration.ModifySpawnExp", "", "#Modify the exp worth of mobs from spawners and eggs"),
  MODIFY_SPAWNER_MOB_EXP("Configuration.ModifySpawnExp.MobsFromSpawner", 0.5),
  MODIFY_EGG_MOB_EXP("Configuration.ModifySpawnExp.MobsFromEggs", 0.5),
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
  ACTIONBAR_SWORDS_MESSAGE("DisplayConfig.ActionBar.Swords.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining'"),
  ACTIONBAR_MINING_MESSAGE("DisplayConfig.ActionBar.Mining.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining'"),
  ACTIONBAR_UNARMED_MESSAGE("DisplayConfig.ActionBar.Unarmed.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining'"),
  ACTIONBAR_HERBALISM_MESSAGE("DisplayConfig.ActionBar.Herbalism.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining'"),
  ACTIONBAR_ARCHERY_MESSAGE("DisplayConfig.ActionBar.Archery.Message", "&a+%Exp_Gained% &eexp &a%Exp_To_Level% &eremaining'"),
  PLAYER_CONFIGURATION_AMOUNT_OF_TOTAL_ABILITIES("PlayerConfiguration.AmountOfTotalAbilities", 9, "#This amount is how many ability slots are in the players loadout", "#Currently unused"),
  PLAYER_CONFIGURATION_ABILITY_POINT_INTERVAL("PlayerConfiguration.AbilityPointInterval", 100, "#When the power level reaches a multiple of this number an ability point is awarded"),
  PLAYER_CONFIGURATION_PLAYER_READY_DURATION("PlayerConfiguration.PlayerReadyDuration", 2, "#How many seconds a player should stay ready for")  ;

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
