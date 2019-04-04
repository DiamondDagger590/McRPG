package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "archery_data")
public class ArcheryTable {

  @Column
  private int current_exp;
  @Column private int level;

  @Column private boolean is_daze_toggled;
  @Column private boolean is_puncture_toggled;
  @Column private boolean is_tipped_arrows_toggled;
  @Column private boolean is_combo_toggled;
  @Column private boolean is_blessing_of_artemis_toggled;
  @Column private boolean is_blessing_of_apollo_toggled;
  @Column private boolean is_curse_of_hades_toggled;


  @Column private boolean puncture_tier;
  @Column private boolean tipped_arrows_tier;
  @Column private boolean combo_tier;
  @Column private boolean blessing_of_artemis_tier;
  @Column private boolean blessing_of_apollo_tier;
  @Column private boolean curse_of_hades_tier;

  @Column private long blessing_of_artemis_cooldowns;
  @Column private long blessing_of_apollo_cooldowns;
  @Column private long curse_of_hades_cooldowns;
}
