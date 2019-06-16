package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "woodcutting_data")
public class ExcavationTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_extraction_toggled;
  @Column(defaultValue = "1") private boolean is_buried_treasure_toggled;
  @Column(defaultValue = "1") private boolean is_larger_spade_toggled;
  @Column(defaultValue = "1") private boolean is_mana_deposit_toggled;
  @Column(defaultValue = "1") private boolean is_hand_digging_toggled;
  @Column(defaultValue = "1") private boolean is_frenzy_dig_toggled;
  @Column(defaultValue = "1") private boolean is_pans_shrine_toggled;

  @Column(defaultValue = "0") private int buried_treasure_tier;
  @Column(defaultValue = "0") private int larger_spade_tier;
  @Column(defaultValue = "0") private int mana_deposit_tier;
  @Column(defaultValue = "0") private int hand_digging_tier;
  @Column(defaultValue = "0") private int frenzy_dig_tier;
  @Column(defaultValue = "0") private int pans_shrine_tier;

  @Column(defaultValue = "0") private int hand_digging_cooldown;
  @Column(defaultValue = "0") private int frenzy_dig_cooldown;
  @Column(defaultValue = "0") private int pans_shrine_cooldown;

  @Column(defaultValue = "0") private boolean is_buried_treasure_pending;
  @Column(defaultValue = "0") private boolean is_larger_spade_pending;
  @Column(defaultValue = "0") private boolean is_mana_deposit_pending;
  @Column(defaultValue = "0") private boolean is_hand_digging_pending;
  @Column(defaultValue = "0") private boolean is_frenzy_dig_pending;
  @Column(defaultValue = "0") private boolean is_pans_shrine_pending;
}