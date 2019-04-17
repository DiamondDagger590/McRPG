package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "herbalism_data")
public class HerbalismTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_too_many_plants_toggled;
  @Column(defaultValue = "1") private boolean is_farmers_diet_toggled;
  @Column(defaultValue = "1") private boolean is_diamond_flowers_toggled;
  @Column(defaultValue = "1") private boolean is_replanting_toggled;
  @Column(defaultValue = "1") private boolean is_mass_harvest_toggled;
  @Column(defaultValue = "1") private boolean is_natures_wrath_toggled;
  @Column(defaultValue = "1") private boolean is_pans_blessing_toggled;

  @Column(defaultValue = "0") private int farmers_diet_tier;
  @Column(defaultValue = "0") private int diamond_flowers_tier;
  @Column(defaultValue = "0") private int replanting_tier;
  @Column(defaultValue = "0") private int mass_harvest_tier;
  @Column(defaultValue = "0") private int natures_wrath_tier;
  @Column(defaultValue = "0") private int pans_blessing_tier;

  @Column(defaultValue = "0") private int mass_harvest_cooldown;
  @Column(defaultValue = "0") private int natures_wrath_cooldown;
  @Column(defaultValue = "0") private int pans_blessing_cooldown;

  @Column(defaultValue = "0") private boolean is_farmers_diet_pending;
  @Column(defaultValue = "0") private boolean is_diamond_flowers_pending;
  @Column(defaultValue = "0") private boolean is_replanting_pending;
  @Column(defaultValue = "0") private boolean is_mass_harvest_pending;
  @Column(defaultValue = "0") private boolean is_natures_wrath_pending;
  @Column(defaultValue = "0") private boolean isPans_blessing_pending;
}
