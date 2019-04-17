package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "archery_data")
public class ArcheryTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_daze_toggled;
  @Column(defaultValue = "1") private boolean is_puncture_toggled;
  @Column(defaultValue = "1") private boolean is_tipped_arrows_toggled;
  @Column(defaultValue = "1") private boolean is_combo_toggled;
  @Column(defaultValue = "1") private boolean is_blessing_of_artemis_toggled;
  @Column(defaultValue = "1") private boolean is_blessing_of_apollo_toggled;
  @Column(defaultValue = "1") private boolean is_curse_of_hades_toggled;


  @Column(defaultValue = "0") private int puncture_tier;
  @Column(defaultValue = "0") private int tipped_arrows_tier;
  @Column(defaultValue = "0") private int combo_tier;
  @Column(defaultValue = "0") private int blessing_of_artemis_tier;
  @Column(defaultValue = "0") private int blessing_of_apollo_tier;
  @Column(defaultValue = "0") private int curse_of_hades_tier;

  @Column(defaultValue = "0") private int blessing_of_artemis_cooldown;
  @Column(defaultValue = "0") private int blessing_of_apollo_cooldown;
  @Column(defaultValue = "0") private int curse_of_hades_cooldown;

  @Column(defaultValue = "0") private boolean is_puncture_pending;
  @Column(defaultValue = "0") private boolean is_tipped_arrows_pending;
  @Column(defaultValue = "0") private boolean is_combo_pending;
  @Column(defaultValue = "0") private boolean is_blessing_of_artemis_pending;
  @Column(defaultValue = "0") private boolean is_blessing_of_apollo_pending;
  @Column(defaultValue = "0") private boolean is_curse_of_hades_pending;
}
