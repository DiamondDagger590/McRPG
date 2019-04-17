package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "swords_data")
public class SwordsTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_bleed_toggled;
  @Column(defaultValue = "1") private boolean is_bleed_plus_toggled;
  @Column(defaultValue = "1") private boolean is_deeper_wound_toggled;
  @Column(defaultValue = "1") private boolean is_vampire_toggled;
  @Column(defaultValue = "1") private boolean is_rage_spike_toggled;
  @Column(defaultValue = "1") private boolean is_serrated_strikes_toggled;
  @Column(defaultValue = "1") private boolean is_tainted_blade_toggled;

  @Column(defaultValue = "0") private int bleed_plus_tier;
  @Column(defaultValue = "0") private int deeper_wound_tier;
  @Column(defaultValue = "0") private int vampire_tier;
  @Column(defaultValue = "0") private int rage_spike_tier;
  @Column(defaultValue = "0") private int serrated_strikes_tier;
  @Column(defaultValue = "0") private int tainted_blade_tier;

  @Column(defaultValue = "0") private int rage_spike_cooldown;
  @Column(defaultValue = "0") private int serrated_strikes_cooldown;
  @Column(defaultValue = "0") private int tainted_blade_cooldown;

  @Column(defaultValue = "0") private boolean is_bleed_plus_pending;
  @Column(defaultValue = "0") private boolean is_deeper_wound_pending;
  @Column(defaultValue = "0") private boolean is_vampire_pending;
  @Column(defaultValue = "0") private boolean is_rage_spike_pending;
  @Column(defaultValue = "0") private boolean is_serrated_strikes_pending;
  @Column(defaultValue = "0") private boolean is_tainted_blade_pending;
}
