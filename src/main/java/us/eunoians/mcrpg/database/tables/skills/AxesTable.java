package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "axes_data")
public class AxesTable {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_shred_toggled;
  @Column(defaultValue = "1") private boolean is_heavy_strike_toggled;
  @Column(defaultValue = "1") private boolean is_blood_frenzy_toggled;
  @Column(defaultValue = "1") private boolean is_sharper_axe_toggled;
  @Column(defaultValue = "1") private boolean is_whirlwind_strike_toggled;
  @Column(defaultValue = "1") private boolean is_ares_blessing_toggled;
  @Column(defaultValue = "1") private boolean is_crippling_blow_toggled;


  @Column(defaultValue = "0") private int heavy_strike_tier;
  @Column(defaultValue = "0") private int blood_frenzy_tier;
  @Column(defaultValue = "0") private int sharper_axe_tier;
  @Column(defaultValue = "0") private int whirlwind_strike_tier;
  @Column(defaultValue = "0") private int ares_blessing_tier;
  @Column(defaultValue = "0") private int crippling_blow_tier;

  @Column(defaultValue = "0") private int whirlwind_strike_cooldown;
  @Column(defaultValue = "0") private int ares_blessing_cooldown;
  @Column(defaultValue = "0") private int crippling_blow_cooldown;

  @Column(defaultValue = "0") private boolean is_heavy_strike_pending;
  @Column(defaultValue = "0") private boolean is_blood_frenzy_pending;
  @Column(defaultValue = "0") private boolean is_sharper_axe_pending;
  @Column(defaultValue = "0") private boolean is_whirlwind_strike_pending;
  @Column(defaultValue = "0") private boolean is_ares_blessing_pending;
  @Column(defaultValue = "0") private boolean is_crippling_blow_pending;
}
