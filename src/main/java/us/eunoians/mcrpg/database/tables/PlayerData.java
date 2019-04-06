package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "player_data")
public class PlayerData {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column int ability_points;
  @Column(defaultValue = "0") String remote_transfer_location;
  @Column int redeemable_exp;
  @Column int redeemable_levels;
}
