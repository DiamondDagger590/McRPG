package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "herbalism_data")
public class HerbalismTable {

  @Column
  private int current_exp;
  @Column private int level;

  @Column private boolean is_too_many_plants_toggled;
  @Column private boolean is_farmers_diet_toggled;
  @Column private boolean is_diamond_flowers_toggled;
  @Column private boolean is_replanting_toggled;
  @Column private boolean is_mass_harvest_toggled;
  @Column private boolean is_natures_wrath_toggled;
  @Column private boolean is_pans_blessing_toggled;

  @Column private boolean farmers_diet_tier;
  @Column private boolean diamond_flowers_tier;
  @Column private boolean replanting_tier;
  @Column private boolean mass_harvest_tier;
  @Column private boolean natures_wrath_tier;
  @Column private boolean pans_blessing_tier;

  @Column private long mass_harvest_cooldowns;
  @Column private long natures_wrath_cooldowns;
  @Column private long pans_blessing_cooldowns;
}
