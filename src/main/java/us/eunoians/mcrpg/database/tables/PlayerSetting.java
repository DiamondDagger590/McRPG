package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "player_settings")
public class PlayerSetting {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column boolean keep_hand;
  @Column boolean ignore_tips;
  @Column boolean auto_deny;
  @Column String display_type;
  @Column String health_type;
}
