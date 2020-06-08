package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.gui.EditLoadoutGUI;
import us.eunoians.mcrpg.gui.EditLoadoutSelectGUI;
import us.eunoians.mcrpg.gui.GUI;
import us.eunoians.mcrpg.gui.SettingsGUI;
import us.eunoians.mcrpg.gui.SubSkillGUI;
import us.eunoians.mcrpg.players.McRPGPlayer;

import java.util.Arrays;

/**
 * @author DiamondDagger590
 *
 * Represents a hard coded gui that is opened normally using the OpenNative event
 */
public enum HardCodedGuis{
  
  EDIT_LOADOUT_GUI("EditLoadoutGUI", (McRPGPlayer mcRPGPlayer, Skills skill) -> {
    return new EditLoadoutGUI(mcRPGPlayer, EditLoadoutGUI.EditType.TOGGLE);
  }, false),
  EDIT_LOADOUT_SELECT_GUI("EditLoadoutSelectGUI", (McRPGPlayer mcRPGPlayer, Skills skill) -> {
    return new EditLoadoutSelectGUI(mcRPGPlayer);
  },false),
  SETTINGS_GUI("SettingsGUI", (McRPGPlayer mcRPGPlayer, Skills skill) -> {
    return new SettingsGUI(mcRPGPlayer);
  },false),
  UPGRADE_GUI("UpgradeAbilityGUI", (McRPGPlayer mcRPGPlayer, Skills skill) -> {
    return new EditLoadoutGUI(mcRPGPlayer, EditLoadoutGUI.EditType.ABILITY_UPGRADE);
  },false),
  SUB_SKILL_GUI("SubSkillGUI", SubSkillGUI::new,true);
  
  @Getter
  private String id; //The internal id of the gui
  
  @Getter
  private HardCodedGuiFunction hardCodedGuiFunction; //The function that builds the gui
  
  @Getter
  private boolean acceptSkill; //If the gui accepts a skill parameter
  
  HardCodedGuis(String id, HardCodedGuiFunction hardCodedGuiFunction, boolean acceptsSkill){
    this.id = id;
    this.hardCodedGuiFunction = hardCodedGuiFunction;
    this.acceptSkill = acceptsSkill;
  }
  
  /**
   * Gets the enum representation of the provided id
   * @param id The string representation of the enum
   * @return An enum value represented by the id or null if invalid
   */
  public static HardCodedGuis fromID(String id){
    return Arrays.stream(values()).filter(hardCodedGuis -> hardCodedGuis.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
  }
  
  /**
   * Builds a gui using the specified parameters
   * @param mcRPGPlayer The player to build the gui for
   * @param skill The skill that is needed as input for some guis. See {@link #isAcceptSkill()}
   * @return A {@link GUI} built from the enum value
   */
  public GUI buildGUI(McRPGPlayer mcRPGPlayer, Skills skill){
    return hardCodedGuiFunction.buildGUI(mcRPGPlayer, skill);
  }
  
  /**
   * A functional interface used to build the gui
   */
  private interface HardCodedGuiFunction{
    
    GUI buildGUI(McRPGPlayer mcRPGPlayer, Skills skill);
  }
}
