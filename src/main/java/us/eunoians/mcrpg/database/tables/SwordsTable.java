package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "swords_data")
public class SwordsTable {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column private int current_exp;
  @Column private int level;

  @Column private boolean is_bleed_toggled;
  @Column private boolean is_bleed_plus_toggled;
  @Column private boolean is_deeper_wound_toggled;
  @Column private boolean is_vampire_toggled;
  @Column private boolean is_rage_spike_toggled;
  @Column private boolean is_serrated_strikes_toggled;
  @Column private boolean is_tainted_blade_toggled;

  @Column private boolean bleed_plus_tier;
  @Column private boolean deeper_wound_tier;
  @Column private boolean vampire_tier;
  @Column private boolean rage_spike_tier;
  @Column private boolean serrated_strikes_tier;
  @Column private boolean tainted_blade_tier;

  @Column private long rage_spike_cooldowns;
  @Column private long serrated_strikes_cooldowns;
  @Column private long tainted_blade_cooldowns;
}
