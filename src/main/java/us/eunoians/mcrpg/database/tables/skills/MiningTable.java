package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "mining_data")
public class MiningTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_double_drop_toggled;
  @Column(defaultValue = "1") private boolean is_richer_ores_toggled;
  @Column(defaultValue = "1") private boolean is_remote_transfer_toggled;
  @Column(defaultValue = "1") private boolean is_its_a_triple_toggled;
  @Column(defaultValue = "1") private boolean is_super_breaker_toggled;
  @Column(defaultValue = "1") private boolean is_blast_mining_toggled;
  @Column(defaultValue = "1") private boolean is_ore_scanner_toggled;

  @Column(defaultValue = "0") private int richer_ores_tier;
  @Column(defaultValue = "0") private int remote_transfer_tier;
  @Column(defaultValue = "0") private int its_a_triple_tier;
  @Column(defaultValue = "0") private int super_breaker_tier;
  @Column(defaultValue = "0") private int blast_mining_tier;
  @Column(defaultValue = "0") private int ore_scanner_tier;

  @Column(defaultValue = "0") private int super_break_cooldown;
  @Column(defaultValue = "0") private int blast_mining_cooldown;
  @Column(defaultValue = "0") private int ore_scanner_cooldown;

  @Column(defaultValue = "0") private boolean is_richer_ores_pending;
  @Column(defaultValue = "0") private boolean is_remote_transfer_pending;
  @Column(defaultValue = "0") private boolean is_its_a_triple_pending;
  @Column(defaultValue = "0") private boolean is_super_breaker_pending;
  @Column(defaultValue = "0") private boolean is_blast_mining_pending;
  @Column(defaultValue = "0") private boolean is_ore_scanner_pending;
}
