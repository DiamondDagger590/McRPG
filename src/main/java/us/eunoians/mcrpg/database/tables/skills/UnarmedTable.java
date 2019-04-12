package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "unarmed_data")
public class UnarmedTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column private int current_exp;
  @Column private int level;

  @Column(defaultValue = "1") private boolean is_sticky_fingers_toggled;
  @Column(defaultValue = "1") private boolean is_tighter_grip_toggled;
  @Column(defaultValue = "1") private boolean is_disarm_toggled;
  @Column(defaultValue = "1") private boolean is_iron_arm_toggled;
  @Column(defaultValue = "1") private boolean is_berserk_toggled;
  @Column(defaultValue = "1") private boolean is_smiting_fist_toggled;
  @Column(defaultValue = "1") private boolean is_dense_impact_toggled;

  @Column private int tighter_grip_tier;
  @Column private int disarm_tier;
  @Column private int iron_arm_tier;
  @Column private int berserk_tier;
  @Column private int smiting_fist_tier;
  @Column private int dense_impact_tier;

  @Column private long berserk_cooldown;
  @Column private long smiting_fist_cooldown;
  @Column private long dense_impact_cooldown;
}
