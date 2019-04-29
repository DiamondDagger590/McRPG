package us.eunoians.mcrpg.database.tables.skills;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table(nameOverride = "fitness_data")
public class FitnessTable {


  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;

  @Column(defaultValue = "0") private int current_exp;
  @Column(defaultValue = "0") private int current_level;

  @Column(defaultValue = "1") private boolean is_roll_toggled;
  @Column(defaultValue = "1") private boolean is_thick_skin_toggled;
  @Column(defaultValue = "1") private boolean is_bullet_proof_toggled;
  @Column(defaultValue = "1") private boolean is_dodge_toggled;
  @Column(defaultValue = "1") private boolean is_iron_muscles_toggled;
  @Column(defaultValue = "1") private boolean is_runners_diet_toggled;
  @Column(defaultValue = "1") private boolean is_divine_escape_toggled;


  @Column(defaultValue = "0") private int thick_skin_tier;
  @Column(defaultValue = "0") private int bullet_proof_tier;
  @Column(defaultValue = "0") private int dodge_tier;
  @Column(defaultValue = "0") private int iron_muscles_tier;
  @Column(defaultValue = "0") private int runners_diet_tier;
  @Column(defaultValue = "0") private int divine_escape_tier;

  @Column(defaultValue = "0") private int divine_escape_cooldown;

  @Column(defaultValue = "0") private boolean is_thick_skin_pending;
  @Column(defaultValue = "0") private boolean is_bullet_proof_pending;
  @Column(defaultValue = "0") private boolean is_dodge_pending;
  @Column(defaultValue = "0") private boolean is_iron_muscles_pending;
  @Column(defaultValue = "0") private boolean is_runners_diet_pending;
  @Column(defaultValue = "0") private boolean is_divine_escape_pending;
}
