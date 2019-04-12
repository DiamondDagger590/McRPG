package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "herbalism_data")
public class HerbalismTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column
  private int current_exp;
  @Column private int level;

  @Column(defaultValue = "1") private boolean is_too_many_plants_toggled;
  @Column(defaultValue = "1") private boolean is_farmers_diet_toggled;
  @Column(defaultValue = "1") private boolean is_diamond_flowers_toggled;
  @Column(defaultValue = "1") private boolean is_replanting_toggled;
  @Column(defaultValue = "1") private boolean is_mass_harvest_toggled;
  @Column(defaultValue = "1") private boolean is_natures_wrath_toggled;
  @Column(defaultValue = "1") private boolean is_pans_blessing_toggled;

  @Column private int farmers_diet_tier;
  @Column private int diamond_flowers_tier;
  @Column private int replanting_tier;
  @Column private int mass_harvest_tier;
  @Column private int natures_wrath_tier;
  @Column private int pans_blessing_tier;

  @Column private long mass_harvest_cooldown;
  @Column private long natures_wrath_cooldown;
  @Column private long pans_blessing_cooldown;
}
