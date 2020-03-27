package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "player_data")
public class PlayerData {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column(defaultValue = "nu") private String party_uuid;
  @Column(defaultValue = "0") int power_level;
  @Column(defaultValue = "0") int ability_points;
  @Column(defaultValue = "0") long replace_ability_cooldown_time;
  @Column(defaultValue = "0") int redeemable_exp;
  @Column(defaultValue = "0") int redeemable_levels;
  @Column(defaultValue = "0") int boosted_exp;
  @Column(defaultValue = "0") double divine_escape_exp_debuff;
  @Column(defaultValue = "0") double divine_escape_damage_debuff;
  @Column(defaultValue = "0") long divine_escape_exp_end_time;
  @Column(defaultValue = "0") long divine_escape_damage_end_time;
}
