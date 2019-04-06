package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "player_settings")
public class PlayerSetting {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column(defaultValue = "false") boolean keep_hand;
  @Column(defaultValue = "false") boolean ignore_tips;
  @Column(defaultValue = "false") boolean auto_deny;
  @Column(defaultValue = "Scoreboard") String display_type;
  @Column(defaultValue = "Bar") String health_type;
}
