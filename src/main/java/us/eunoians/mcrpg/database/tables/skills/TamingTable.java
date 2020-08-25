package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "taming_data")
public class TamingTable{
  
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  
  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;
  
  @Column(defaultValue = "1") private boolean is_gore_toggled;
  @Column(defaultValue = "1") private boolean is_divine_fur_toggled;
  @Column(defaultValue = "1") private boolean is_sharpened_fangs_toggled;
  @Column(defaultValue = "1") private boolean is_linked_fangs_toggled;
  @Column(defaultValue = "1") private boolean is_comradery_toggled;
  @Column(defaultValue = "1") private boolean is_petas_wrath_toggled;
  @Column(defaultValue = "1") private boolean is_fury_of_cerberus_toggled;
  
  @Column(defaultValue = "0") private int divine_fur_tier;
  @Column(defaultValue = "0") private int sharpened_fangs_tier;
  @Column(defaultValue = "0") private int linked_fangs_tier;
  @Column(defaultValue = "0") private int comradery_tier;
  @Column(defaultValue = "0") private int petas_wrath_tier;
  @Column(defaultValue = "0") private int fury_of_cerberus_tier;
  
  @Column(defaultValue = "0") private int fury_of_cerberus_cooldown;
  
  @Column(defaultValue = "0") private boolean is_divine_fur_pending;
  @Column(defaultValue = "0") private boolean is_sharpened_fangs_pending;
  @Column(defaultValue = "0") private boolean is_linked_fangs_pending;
  @Column(defaultValue = "0") private boolean is_comradery_pending;
  @Column(defaultValue = "0") private boolean is_petas_wrath_pending;
  @Column(defaultValue = "0") private boolean is_fury_of_cerberus_pending;
}
