package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "player_data")
public class PlayerData {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column(defaultValue = "0") int ability_points;
  @Column(defaultValue = "0") int replace_ability_cooldown;
  @Column(defaultValue = "0") int redeemable_exp;
  @Column(defaultValue = "0") int redeemable_levels;
}
