package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "swords_data")
public class SwordsTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column private int current_exp;
  @Column private int level;

  @Column(defaultValue = "1") private boolean is_bleed_toggled;
  @Column(defaultValue = "1") private boolean is_bleed_plus_toggled;
  @Column(defaultValue = "1") private boolean is_deeper_wound_toggled;
  @Column(defaultValue = "1") private boolean is_vampire_toggled;
  @Column(defaultValue = "1") private boolean is_rage_spike_toggled;
  @Column(defaultValue = "1") private boolean is_serrated_strikes_toggled;
  @Column(defaultValue = "1") private boolean is_tainted_blade_toggled;

  @Column private int bleed_plus_tier;
  @Column private int deeper_wound_tier;
  @Column private int vampire_tier;
  @Column private int rage_spike_tier;
  @Column private int serrated_strikes_tier;
  @Column private int tainted_blade_tier;

  @Column private long rage_spike_cooldown;
  @Column private long serrated_strikes_cooldown;
  @Column private long tainted_blade_cooldown;
}
