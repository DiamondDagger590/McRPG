package us.eunoians.mcrpg.database.tables;

import com.cyr1en.flatdb.annotations.Column;
import com.cyr1en.flatdb.annotations.Table;

@Table
public class Loadout {
  @Column(autoIncrement = true) private int id;
  @Column(primaryKey = true) private String uuid;
  @Column private String slot1;
  @Column private String slot2;
  @Column private String slot3;
  @Column private String slot4;
  @Column private String slot5;
  @Column private String slot6;
  @Column private String slot7;
  @Column private String slot8;
  @Column private String slot9;
}
