package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "unarmed_data")
public class UnarmedTable {

  @Column private int current_exp;
  @Column private int level;

  @Column private boolean is_sticky_fingers_toggled;
  @Column private boolean is_tighter_grip_toggled;
  @Column private boolean is_disarm_toggled;
  @Column private boolean is_iron_arm_toggled;
  @Column private boolean is_bersek_toggled;
  @Column private boolean is_smiting_fist_toggled;
  @Column private boolean is_dense_impact_toggled;

  @Column private boolean tighter_grip_tier;
  @Column private boolean disarm_tier;
  @Column private boolean iron_arm_tier;
  @Column private boolean bersek_tier;
  @Column private boolean smiting_fist_tier;
  @Column private boolean dense_impact_tier;

  @Column private long bersek_cooldowns;
  @Column private long smiting_fist_cooldowns;
  @Column private long dense_impact_cooldowns;
}
