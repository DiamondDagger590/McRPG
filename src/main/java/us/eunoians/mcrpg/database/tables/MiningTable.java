package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "mining_data")
public class MiningTable {

  @Column
  private int current_exp;
  @Column private int level;

  @Column private boolean is_double_drop_toggled;
  @Column private boolean is_richer_ores_toggled;
  @Column private boolean is_remote_transfer_toggled;
  @Column private boolean is_its_a_triple_toggled;
  @Column private boolean is_super_breaker_toggled;
  @Column private boolean is_blast_mining_toggled;
  @Column private boolean is_ore_scanner_toggled;

  @Column private boolean richer_ores_tier;
  @Column private boolean remote_transfer_tier;
  @Column private boolean its_a_triple_tier;
  @Column private boolean super_breaker_tier;
  @Column private boolean blast_mining_tier;
  @Column private boolean ore_scanner_tier;

  @Column private long super_break_cooldowns;
  @Column private long blast_mining_cooldowns;
  @Column private long ore_scanner_cooldowns;
}
