package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "mining_data")
public class MiningTable {

  @Column
  private int current_exp;
  @Column private int level;

  @Column(defaultValue = "true") private boolean is_double_drop_toggled;
  @Column(defaultValue = "true") private boolean is_richer_ores_toggled;
  @Column(defaultValue = "true") private boolean is_remote_transfer_toggled;
  @Column(defaultValue = "true") private boolean is_its_a_triple_toggled;
  @Column(defaultValue = "true") private boolean is_super_breaker_toggled;
  @Column(defaultValue = "true") private boolean is_blast_mining_toggled;
  @Column(defaultValue = "true") private boolean is_ore_scanner_toggled;

  @Column private int richer_ores_tier;
  @Column private int remote_transfer_tier;
  @Column private int its_a_triple_tier;
  @Column private int super_breaker_tier;
  @Column private int blast_mining_tier;
  @Column private int ore_scanner_tier;

  @Column private long super_break_cooldown;
  @Column private long blast_mining_cooldown;
  @Column private long ore_scanner_cooldown;
}
