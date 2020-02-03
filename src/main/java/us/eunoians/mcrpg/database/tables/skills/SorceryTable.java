package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "sorcery_data")
public class SorceryTable {

  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_hasty_brew_toggled;
  @Column(defaultValue = "1") private boolean is_circes_recipes_toggled;
  @Column(defaultValue = "1") private boolean is_potion_affinity_toggled;
  @Column(defaultValue = "1") private boolean is_mana_affinity_toggled;
  @Column(defaultValue = "1") private boolean is_circes_protection_toggled;
  @Column(defaultValue = "1") private boolean is_hades_domain_toggled;
  @Column(defaultValue = "1") private boolean is_circes_shrine_toggled;

  @Column(defaultValue = "0") private int circes_recipes_tier;
  @Column(defaultValue = "0") private int potion_affinity_tier;
  @Column(defaultValue = "0") private int mana_affinity_tier;
  @Column(defaultValue = "0") private int circes_protection_tier;
  @Column(defaultValue = "0") private int hades_domain_tier;
  @Column(defaultValue = "0") private int circes_shrine_tier;

  @Column(defaultValue = "0") private int circes_shrine_cooldown;

  @Column(defaultValue = "0") private boolean is_circes_recipes_pending;
  @Column(defaultValue = "0") private boolean is_potion_affinity_pending;
  @Column(defaultValue = "0") private boolean is_mana_affinity_pending;
  @Column(defaultValue = "0") private boolean is_circes_protection_pending;
  @Column(defaultValue = "0") private boolean is_hades_domain_pending;
  @Column(defaultValue = "0") private boolean is_circes_shrine_pending;
}
