package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "woodcutting_data")
public class WoodcuttingTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_extra_lumber_toggled;
  @Column(defaultValue = "1") private boolean is_heavy_swing_toggled;
  @Column(defaultValue = "1") private boolean is_nymphs_vitality_toggled;
  @Column(defaultValue = "1") private boolean is_dryads_gift_toggled;
  @Column(defaultValue = "1") private boolean is_temporal_harvest_toggled;
  @Column(defaultValue = "1") private boolean is_hesperides_apples_toggled;
  @Column(defaultValue = "1") private boolean is_demeters_shrine_toggled;

  @Column(defaultValue = "0") private int heavy_swing_tier;
  @Column(defaultValue = "0") private int nymphs_vitality_tier;
  @Column(defaultValue = "0") private int dryads_gift_tier;
  @Column(defaultValue = "0") private int temporal_harvest_tier;
  @Column(defaultValue = "0") private int hesperides_apples_tier;
  @Column(defaultValue = "0") private int demeters_shrine_tier;

  @Column(defaultValue = "0") private int temporal_harvest_cooldown;
  @Column(defaultValue = "0") private int hesperides_apples_cooldown;
  @Column(defaultValue = "0") private int demeters_shrine_cooldown;

  @Column(defaultValue = "0") private boolean is_heavy_swing_pending;
  @Column(defaultValue = "0") private boolean is_nymphs_vitality_pending;
  @Column(defaultValue = "0") private boolean is_dryads_gift_pending;
  @Column(defaultValue = "0") private boolean is_temporal_harvest_pending;
  @Column(defaultValue = "0") private boolean is_hesperides_apples_pending;
  @Column(defaultValue = "0") private boolean is_demeters_shrine_pending;
}
